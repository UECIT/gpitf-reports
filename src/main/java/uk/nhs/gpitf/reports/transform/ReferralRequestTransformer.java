package uk.nhs.gpitf.reports.transform;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.Coding;
import org.hl7.fhir.dstu3.model.Condition;
import org.hl7.fhir.dstu3.model.Condition.ConditionClinicalStatus;
import org.hl7.fhir.dstu3.model.Condition.ConditionVerificationStatus;
import org.hl7.fhir.dstu3.model.Encounter;
import org.hl7.fhir.dstu3.model.Period;
import org.hl7.fhir.dstu3.model.Reference;
import org.hl7.fhir.dstu3.model.ReferralRequest;
import org.hl7.fhir.dstu3.model.ReferralRequest.ReferralCategory;
import org.hl7.fhir.dstu3.model.ReferralRequest.ReferralPriority;
import org.hl7.fhir.dstu3.model.ReferralRequest.ReferralRequestRequesterComponent;
import org.hl7.fhir.dstu3.model.ReferralRequest.ReferralRequestStatus;
import org.springframework.stereotype.Component;
import uk.nhs.connect.iucds.cda.ucr.CV;
import uk.nhs.connect.iucds.cda.ucr.POCDMT000002UK01ClinicalDocument1;
import uk.nhs.connect.iucds.cda.ucr.POCDMT000002UK01Component2;
import uk.nhs.connect.iucds.cda.ucr.POCDMT000002UK01Component3;
import uk.nhs.connect.iucds.cda.ucr.POCDMT000002UK01Entry;
import uk.nhs.connect.iucds.cda.ucr.POCDMT000002UK01InformationRecipient;
import uk.nhs.connect.iucds.cda.ucr.POCDMT000002UK01Observation;
import uk.nhs.gpitf.reports.constants.FHIRSystems;
import uk.nhs.gpitf.reports.constants.IUCDSSystems;
import uk.nhs.gpitf.reports.constants.IUCDSTemplates;
import uk.nhs.gpitf.reports.model.InputBundle;
import uk.nhs.gpitf.reports.service.ConditionService;
import uk.nhs.gpitf.reports.service.HealthcareServiceService;

@Component
@RequiredArgsConstructor
public class ReferralRequestTransformer {

  private final ConditionService conditionService;
  private final HealthcareServiceService healthcareServiceService;

  public ReferralRequest transform(InputBundle inputBundle,
      Encounter encounter, Reference transformerDevice) {

    POCDMT000002UK01ClinicalDocument1 clinicalDocument = inputBundle.getClinicalDocument();
    ReferralRequest referralRequest = new ReferralRequest();

    // definition MAY be populated - ActivityDefinition not currently part of ER guidance
    // basedOn MUST be populated in ER, but no CDS interaction so no generic ReferralRequest
    // replaces - not replacing anything
    // type MUST NOT be populated
    // serviceRequested MUST NOT be populated
    // reasonCode MUST NOT be populated
    // specialty MAY be populated - no mapping

    Date now = new Date();
    referralRequest
        .setStatus(ReferralRequestStatus.ACTIVE)
        .setIntent(ReferralCategory.PLAN)
        .setPriority(ReferralPriority.ROUTINE)
        .setSubject(encounter.getSubject())
        .setContext(new Reference(encounter))
        .addReasonReference(createReasonCondition(encounter,
            transformClinicalDiscriminator(inputBundle.getClinicalDocument())))
        .setOccurrence(new Period()
            .setStart(now)
            .setEnd(Date.from(now.toInstant().plusSeconds(60 * 60))))
        .setAuthoredOn(now)
        .setRequester(new ReferralRequestRequesterComponent()
            .setAgent(transformerDevice)
            .setOnBehalfOf(encounter.getServiceProvider()));

    if (clinicalDocument.sizeOfInformationRecipientArray() > 0) {
      for (POCDMT000002UK01InformationRecipient recipient :
          clinicalDocument.getInformationRecipientArray()) {
        referralRequest.addRecipient(healthcareServiceService.createHealthcareService(recipient));
      }
    }

    // TODO description SHOULD be populated by the CDSS - pathways
    // TODO supportingInfo MUST be populated with a ProcedureRequest inc. secondary concerns - pathways
    // TODO note MAY be populated with additional notes - pathways
    // TODO relevantHistory SHOULD be populated (Provenance) - not currently used in EMS

    return referralRequest;
  }

  private Reference createReasonCondition(Encounter encounter, CodeableConcept reason) {
    // severity SHOULD be populated where available - no mapping
    // bodySite SHOULD be populated where available - no mapping
    // onset SHOULD be populated where available - no mapping
    // abatement SHOULD - no mapping
    // assertedDate MUST NOT be populated
    // asserter MUST NOT
    // stage - no mapping
    // evidence.code MUST NOT
    // TODO evidence.detail MUST -> Observations, QRs - pathways
    // note MUST NOT

    Condition condition = new Condition()
        .setClinicalStatus(ConditionClinicalStatus.ACTIVE)
        .setVerificationStatus(ConditionVerificationStatus.CONFIRMED)
        .setCode(reason)
        .setSubject(encounter.getSubject())
        .setContext(new Reference(encounter));
    return conditionService.create(condition);
  }

  private CodeableConcept transformClinicalDiscriminator(
      POCDMT000002UK01ClinicalDocument1 clinicalDocument) {

    POCDMT000002UK01Component2 component = clinicalDocument.getComponent();
    if (component == null || !component.isSetStructuredBody()) {
      return null;
    }

    List<Coding> reasonComponents = getReasonComponents(
        component.getStructuredBody().getComponentArray());

    return new CodeableConcept()
        .setCoding(reasonComponents)
        .setText(reasonComponents.stream()
            .findFirst()
            .map(Coding::getDisplay)
            .orElse(null)
        );
  }

  private List<Coding> getReasonComponents(POCDMT000002UK01Component3[] componentArray) {
    POCDMT000002UK01Entry[] entryArray = componentArray[0]
        .getSection()
        .getEntryArray();

    return Arrays.stream(entryArray)
        .filter(POCDMT000002UK01Entry::isSetObservation)
        .map(POCDMT000002UK01Entry::getObservation)
        .filter(isClinicalDiscriminator())
        .map(obs -> (CV) obs.getValueArray(0))
        .map(this::createCoding)
        .collect(Collectors.toUnmodifiableList());
  }

  private Coding createCoding(CV cv) {
    return new Coding(
        mapSystem(cv.getCodeSystem()),
        cv.getCode(),
        cv.getDisplayName()
    );
  }

  private String mapSystem(String codeSystem) {
    switch (codeSystem) {
      case IUCDSSystems.SNOMED:
        return FHIRSystems.SNOMED;
      default:
        return codeSystem;
    }
  }

  private Predicate<POCDMT000002UK01Observation> isClinicalDiscriminator() {
    return obs -> Arrays.stream(obs.getTemplateIdArray())
        .anyMatch(id -> IUCDSTemplates.CLINICAL_DISCRIMINATOR.equals(id.getExtension()));
  }
}
