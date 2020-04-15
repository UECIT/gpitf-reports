package uk.nhs.gpitf.reports.service;

import java.util.List;
import org.apache.commons.codec.binary.Base64;
import org.apache.xmlbeans.XmlException;
import org.hl7.fhir.dstu3.model.AllergyIntolerance;
import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.dstu3.model.ClinicalImpression;
import org.hl7.fhir.dstu3.model.Consent;
import org.hl7.fhir.dstu3.model.DiagnosticReport;
import org.hl7.fhir.dstu3.model.Encounter;
import org.hl7.fhir.dstu3.model.ListResource;
import org.hl7.fhir.dstu3.model.ListResource.ListEntryComponent;
import org.hl7.fhir.dstu3.model.MedicationStatement;
import org.hl7.fhir.dstu3.model.Observation;
import org.hl7.fhir.dstu3.model.Reference;
import org.hl7.fhir.dstu3.model.RelatedPerson;
import org.nhspathways.webservices.pathways.pathwayscase.PathwaysCaseDocument;
import org.springframework.stereotype.Service;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import lombok.RequiredArgsConstructor;
import uk.nhs.connect.iucds.cda.ucr.ClinicalDocumentDocument1;
import uk.nhs.connect.iucds.cda.ucr.POCDMT000002UK01ClinicalDocument1;
import uk.nhs.connect.iucds.cda.ucr.POCDMT000002UK01Entry;
import uk.nhs.connect.iucds.cda.ucr.POCDMT000002UK01ObservationMedia;
import uk.nhs.gpitf.reports.constants.IUCDSTemplates;
import uk.nhs.gpitf.reports.model.InputBundle;
import uk.nhs.gpitf.reports.util.NodeUtil;
import uk.nhs.gpitf.reports.util.StructuredBodyUtil;
import uk.nhs.itk.envelope.DistributionEnvelopeDocument;

@Service
@RequiredArgsConstructor
public class EncounterReportService {

  private static final String CLINICAL_DOCUMENT_NODE_NAME = "ClinicalDocument";

  private final EncounterService encounterService;
  private final ReferralRequestService referralRequestService;
  private final CarePlanService carePlanService;
  private final ConsentService consentService;
  private final DeviceService deviceService;
  private final AppointmentService appointmentService;
  private final ListService listService;
  private final CompositionService compositionService;
  private final QuestionnaireResponseService questionnaireResponseService;
  private final FhirMessageService fhirMessageService;
  private final ObservationService observationService;

  private final FhirStorageService storageService;

  public Reference createEncounterReport(String xmlReportString) throws XmlException {
    var envelopedDocument = DistributionEnvelopeDocument.Factory.parse(xmlReportString);

    var inputBundle = new InputBundle();
    inputBundle.setClinicalDocument(ClinicalDocumentDocument1.Factory
        .parse(findClinicalDoc(envelopedDocument))
        .getClinicalDocument());
    var pathwaysXml = findPathwaysCase(inputBundle.getClinicalDocument());
    if (pathwaysXml != null) {
      inputBundle.setPathwaysCase(
          PathwaysCaseDocument.Factory.parse(pathwaysXml).getPathwaysCase());
    }

    Reference transformerDevice = deviceService.createTransformerDevice();

    Encounter encounter = encounterService.createEncounter(inputBundle);
    questionnaireResponseService.createQuestionnaireResponse(encounter, inputBundle);
    Reference referralRequest = referralRequestService
        .createReferralRequest(inputBundle, encounter, transformerDevice);
    carePlanService.createCarePlans(inputBundle, encounter);
    consentService.createConsent(inputBundle, encounter);
    appointmentService.createAppointment(inputBundle, referralRequest, encounter.getSubject());
    observationService.createObservation(inputBundle, transformerDevice, encounter);
    
    listService.createList(inputBundle, transformerDevice, encounter);
    compositionService.createList(inputBundle, transformerDevice, encounter);

    return new Reference(encounter.getIdElement());
  }

  private String findPathwaysCase(POCDMT000002UK01ClinicalDocument1 document) {
    return StructuredBodyUtil.getEntriesOfType(
        StructuredBodyUtil.getStructuredBody(document),
        IUCDSTemplates.OBSERVATION_MEDIA)
        .stream()
        .findFirst()
        .map(POCDMT000002UK01Entry::getObservationMedia)
        .map(POCDMT000002UK01ObservationMedia::getValue)
        .map(NodeUtil::getNodeValueString)
        .map(String::strip)
        .map(String::getBytes)
        .map(Base64::decodeBase64)
        .map(String::new)
        .orElse(null);
  }

  private Node findClinicalDoc(DistributionEnvelopeDocument envelopedDocument)
      throws XmlException {
    NodeList childNodes = envelopedDocument.getDistributionEnvelope()
        .getPayloads()
        .getPayloadArray(0)
        .getDomNode()
        .getChildNodes();

    for (int i = 0; i < childNodes.getLength(); i++) {
      Node node = childNodes.item(i);

      if (node.getNodeName().contains(CLINICAL_DOCUMENT_NODE_NAME)) {
        return node;
      }
    }
    throw new XmlException("No clinical document found in Envelope");
  }

  public Bundle getEncounterReport(Reference encounterRef) {
    Bundle encounterReport = storageService.getEncounterReport(encounterRef);
    Bundle bundle = fhirMessageService.createMessage(encounterReport);
    addObservationToBundle(bundle);
    return bundle;
  }

  private Bundle addObservationToBundle(Bundle bundle) {
    String observationRef = null;
    Observation observation = null;
    String consentRef = null;
    Consent consent = null;
    String clinicalImpressionRef = null;
    ClinicalImpression clinicalImpression = null;
    String allergyIntoleranceRef = null;
    AllergyIntolerance allergyIntolerance = null;
    String diagnosticReportRef = null;
    DiagnosticReport diagnosticReport = null;
    String medicationStatementRef = null;
    MedicationStatement medicationStatement = null;
    String relatedPersonRef = null;
    RelatedPerson relatedPerson = null;
    List<BundleEntryComponent> entry = bundle.getEntry();
    for (BundleEntryComponent bundleEntryComponent : entry) {
      if (bundleEntryComponent.getResource().getResourceType().name().equals("List")) {
        ListResource resource = (ListResource) bundleEntryComponent.getResource();
        List<ListEntryComponent> ListEntry = resource.getEntry();
        for (ListEntryComponent ListEntryComponent : ListEntry) {
          if (ListEntryComponent.getItem().getReference().contains("Observation")) {
            observationRef = ListEntryComponent.getItem().getReference();
            observation = (Observation) storageService.fetchResourceFromUrl(observationRef, "Observation");
          } else if (ListEntryComponent.getItem().getReference().contains("Consent")) {
            consentRef = ListEntryComponent.getItem().getReference();
            consent = (Consent) storageService.fetchResourceFromUrl(consentRef, "Consent");
          } else if (ListEntryComponent.getItem().getReference().contains("ClinicalImpression")) {
            clinicalImpressionRef = ListEntryComponent.getItem().getReference();
            clinicalImpression = (ClinicalImpression) storageService.fetchResourceFromUrl(clinicalImpressionRef, "ClinicalImpression");
          } else if (ListEntryComponent.getItem().getReference().contains("AllergyIntolerance")) {
            allergyIntoleranceRef = ListEntryComponent.getItem().getReference();
            allergyIntolerance = (AllergyIntolerance) storageService.fetchResourceFromUrl(allergyIntoleranceRef, "AllergyIntolerance");
          } else if (ListEntryComponent.getItem().getReference().contains("DiagnosticReport")) {
            diagnosticReportRef = ListEntryComponent.getItem().getReference();
            diagnosticReport = (DiagnosticReport) storageService.fetchResourceFromUrl(diagnosticReportRef, "DiagnosticReport");
          } else if (ListEntryComponent.getItem().getReference().contains("MedicationStatement")) {
            medicationStatementRef = ListEntryComponent.getItem().getReference();
            medicationStatement = (MedicationStatement) storageService.fetchResourceFromUrl(medicationStatementRef, "MedicationStatement");
          } else if (ListEntryComponent.getItem().getReference().contains("RelatedPerson")) {
            relatedPersonRef = ListEntryComponent.getItem().getReference();
            relatedPerson = (RelatedPerson) storageService.fetchResourceFromUrl(relatedPersonRef, "RelatedPerson");
          } else {}
      }
    }
  }
  bundle.getEntry().add(new BundleEntryComponent().setFullUrl(observationRef).setResource(observation));
  bundle.getEntry().add(new BundleEntryComponent().setFullUrl(consentRef).setResource(consent));
  bundle.getEntry().add(new BundleEntryComponent().setFullUrl(clinicalImpressionRef).setResource(clinicalImpression));
  bundle.getEntry().add(new BundleEntryComponent().setFullUrl(allergyIntoleranceRef).setResource(allergyIntolerance));
  bundle.getEntry().add(new BundleEntryComponent().setFullUrl(diagnosticReportRef).setResource(diagnosticReport));
  bundle.getEntry().add(new BundleEntryComponent().setFullUrl(medicationStatementRef).setResource(medicationStatement));
  bundle.getEntry().add(new BundleEntryComponent().setFullUrl(relatedPersonRef).setResource(relatedPerson));
  return bundle;
}
}