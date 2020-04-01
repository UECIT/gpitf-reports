package uk.nhs.gpitf.reports.transform;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.Condition;
import org.hl7.fhir.dstu3.model.Condition.ConditionClinicalStatus;
import org.hl7.fhir.dstu3.model.Condition.ConditionVerificationStatus;
import org.hl7.fhir.dstu3.model.Encounter;
import org.hl7.fhir.dstu3.model.Narrative;
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
import uk.nhs.connect.iucds.cda.ucr.POCDMT000002UK01Entry;
import uk.nhs.connect.iucds.cda.ucr.POCDMT000002UK01InformationRecipient;
import uk.nhs.gpitf.reports.constants.IUCDSSystems;
import uk.nhs.gpitf.reports.constants.IUCDSTemplates;
import uk.nhs.gpitf.reports.model.InputBundle;
import uk.nhs.gpitf.reports.service.ConditionService;
import uk.nhs.gpitf.reports.service.HealthcareServiceService;
import uk.nhs.gpitf.reports.service.NarrativeService;
import uk.nhs.gpitf.reports.util.CodeUtil;
import uk.nhs.gpitf.reports.util.PathwaysUtils;
import uk.nhs.gpitf.reports.util.StructuredBodyUtil;

@Component
@RequiredArgsConstructor
public class ReferralRequestTransformer {

  private final ConditionService conditionService;
  private final HealthcareServiceService healthcareServiceService;
  private final NarrativeService narrativeService;

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
        .setOccurrence(new Period()
            .setStart(now)
            .setEnd(Date.from(now.toInstant().plusSeconds(60 * 60))))
        .setAuthoredOn(now)
        .setRequester(new ReferralRequestRequesterComponent()
            .setAgent(transformerDevice)
            .setOnBehalfOf(encounter.getServiceProvider()));

    for (CodeableConcept code : getClinicalDiscriminatorCodes(inputBundle.getClinicalDocument())) {
      referralRequest.addReasonReference(createReasonCondition(encounter, code));
    }

    for (POCDMT000002UK01InformationRecipient recipient :
        clinicalDocument.getInformationRecipientArray()) {
      referralRequest.addRecipient(healthcareServiceService.createHealthcareService(recipient));
    }

    PathwaysUtils.getOutcome(inputBundle.getPathwaysCase())
        .ifPresent(outcome -> {
          referralRequest.setDescription(outcome.getTitle());
          Narrative narrative = narrativeService
              .buildNarrative(outcome.getCode() + " - " + outcome.getTitle());
          referralRequest.setText(narrative);
        });

    // TODO supportingInfo MUST be populated with a ProcedureRequest inc. secondary concerns if any - pathways
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

  private List<CodeableConcept> getClinicalDiscriminatorCodes(
      POCDMT000002UK01ClinicalDocument1 clinicalDocument) {

    var structuredBody = StructuredBodyUtil.getStructuredBody(clinicalDocument);
    if (structuredBody == null) {
      return Collections.emptyList();
    }
    return StructuredBodyUtil
        .getEntriesOfType(structuredBody, IUCDSTemplates.CLINICAL_DISCRIMINATOR)
        .stream()
        .filter(POCDMT000002UK01Entry::isSetObservation)
        .map(POCDMT000002UK01Entry::getObservation)
        .map(obs -> (CV) obs.getValueArray(0))
        .filter(cv -> IUCDSSystems.SNOMED.equals(cv.getCodeSystem()))
        .map(CodeUtil::createCodeableConceptFromCE)
        .collect(Collectors.toUnmodifiableList());
  }

}
