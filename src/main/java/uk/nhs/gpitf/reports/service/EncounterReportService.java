package uk.nhs.gpitf.reports.service;

import lombok.RequiredArgsConstructor;
import org.apache.commons.codec.binary.Base64;
import org.apache.xmlbeans.XmlException;
import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Encounter;
import org.hl7.fhir.dstu3.model.Reference;
import org.nhspathways.webservices.pathways.pathwayscase.PathwaysCaseDocument;
import org.springframework.stereotype.Service;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import uk.nhs.connect.iucds.cda.ucr.ClinicalDocumentDocument1;
import uk.nhs.connect.iucds.cda.ucr.POCDMT000002UK01ClinicalDocument1;
import uk.nhs.connect.iucds.cda.ucr.POCDMT000002UK01Entry;
import uk.nhs.connect.iucds.cda.ucr.POCDMT000002UK01ObservationMedia;
import uk.nhs.gpitf.reports.constants.IUCDSTemplates;
import uk.nhs.gpitf.reports.model.InputBundle;
import uk.nhs.gpitf.reports.transform.EncounterTransformer;
import uk.nhs.gpitf.reports.util.NodeUtil;
import uk.nhs.gpitf.reports.util.StructuredBodyUtil;
import uk.nhs.itk.envelope.DistributionEnvelopeDocument;

@Service
@RequiredArgsConstructor
public class EncounterReportService {

  private static final String CLINICAL_DOCUMENT_NODE_NAME = "ClinicalDocument";

  private final EncounterTransformer encounterTransformer;
  private final ReferralRequestService referralRequestService;
  private final CarePlanService carePlanService;
  private final ConsentService consentService;
  private final DeviceService deviceService;

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

    Encounter encounter = encounterTransformer.transform(inputBundle);
    Reference encounterRef = storageService.create(encounter);

    referralRequestService.createReferralRequest(inputBundle, encounter, transformerDevice);
    carePlanService.createCarePlans(inputBundle, encounterRef);
    consentService.createConsent(inputBundle, encounter);

    return encounterRef;
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
    return storageService.getEncounterReport(encounterRef);
  }
}
