package uk.nhs.gpitf.reports.transform;

import java.util.Date;
import org.hl7.fhir.dstu3.model.Encounter;
import org.hl7.fhir.dstu3.model.Encounter.EncounterStatus;
import org.hl7.fhir.dstu3.model.Period;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;
import uk.nhs.connect.iucds.cda.ucr.IVLTS;
import uk.nhs.connect.iucds.cda.ucr.POCDMT000002UK01ClinicalDocument1;
import uk.nhs.gpitf.reports.model.InputBundle;
import uk.nhs.gpitf.reports.service.AllergyIntoleranceService;
import uk.nhs.gpitf.reports.service.ClinicalImpressionService;
import uk.nhs.gpitf.reports.service.EncounterParticipantService;
import uk.nhs.gpitf.reports.service.EpisodeOfCareService;
import uk.nhs.gpitf.reports.service.LocationService;
import uk.nhs.gpitf.reports.service.MedicationService;
import uk.nhs.gpitf.reports.service.OrganizationService;
import uk.nhs.gpitf.reports.service.PatientService;
import uk.nhs.gpitf.reports.service.RelatedPersonService;
import uk.nhs.gpitf.reports.service.TriageReportService;
import uk.nhs.gpitf.reports.util.DateUtil;

@Component
@RequiredArgsConstructor
public class EncounterTransformer {

  private final LocationService locationService;
  private final OrganizationService organizationService;
  private final EncounterParticipantService encounterParticipantService;
  private final EpisodeOfCareService episodeOfCareService;
  private final PatientService patientService;
  private final RelatedPersonService relatedPersonService;
  private final MedicationService medicationService;
  private final AllergyIntoleranceService allergyIntoleranceService;
  private final TriageReportService triageReportService;
  private final ClinicalImpressionService clinicalImpressionService;

  public Encounter transform(InputBundle inputBundle) {

    POCDMT000002UK01ClinicalDocument1 clinicalDocument = inputBundle.getClinicalDocument();

    Encounter encounter = new Encounter();
    encounter.setStatus(EncounterStatus.FINISHED);
    episodeOfCareService.createEpisodeOfCare(inputBundle, clinicalDocument)
        .ifPresent(encounter::addEpisodeOfCare);
    encounterParticipantService.createParticipants(inputBundle, clinicalDocument)
        .forEach(encounter::addParticipant);
    encounter.setPeriod(getEncounterPeriod(clinicalDocument));
    locationService.createFromEncompassingEncounter(clinicalDocument)
        .ifPresent(locationRef -> encounter.addLocation().setLocation(locationRef));
    organizationService.createServiceProvider(inputBundle, clinicalDocument)
        .ifPresent(encounter::setServiceProvider);
    relatedPersonService.createRelatedPerson(inputBundle, encounter);
    medicationService.createMedication(inputBundle, encounter);
    allergyIntoleranceService.createAllergyIntolerance(inputBundle, encounter);
    triageReportService.createTriageReport(inputBundle, encounter);
    clinicalImpressionService.createClinicalImpression(inputBundle, encounter);
//    encounter.addType(); //TODO: No mapping exists.
    encounter.setSubject(patientService.createPatient(inputBundle, clinicalDocument));
//    encounter.setAppointment() TODO: NCTH-395
//    encounter.addDiagnosis(); //TODO: No mapping exists.

    return encounter;
  }

  private Period getEncounterPeriod(POCDMT000002UK01ClinicalDocument1 clinicalDocument) {

    IVLTS effectiveTime = clinicalDocument.getComponentOf()
        .getEncompassingEncounter()
        .getEffectiveTime();

    Date high = DateUtil.parse(effectiveTime.getHigh().getValue());
    Date low = DateUtil.parse(effectiveTime.getLow().getValue());

    return new Period()
        .setStart(low)
        .setEnd(high);
  }

}
