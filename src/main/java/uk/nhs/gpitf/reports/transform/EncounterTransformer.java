package uk.nhs.gpitf.reports.transform;

import java.util.Date;
import lombok.RequiredArgsConstructor;
import org.hl7.fhir.dstu3.model.Encounter;
import org.hl7.fhir.dstu3.model.Encounter.EncounterStatus;
import org.hl7.fhir.dstu3.model.Period;
import org.springframework.stereotype.Component;
import uk.nhs.connect.iucds.cda.ucr.ClinicalDocumentDocument1;
import uk.nhs.connect.iucds.cda.ucr.IVLTS;
import uk.nhs.connect.iucds.cda.ucr.POCDMT000002UK01ClinicalDocument1;
import uk.nhs.gpitf.reports.service.EncounterParticipantService;
import uk.nhs.gpitf.reports.service.EpisodeOfCareService;
import uk.nhs.gpitf.reports.service.LocationService;
import uk.nhs.gpitf.reports.service.OrganizationService;
import uk.nhs.gpitf.reports.service.ReasonService;
import uk.nhs.gpitf.reports.util.DateUtil;

@Component
@RequiredArgsConstructor
public class EncounterTransformer {

  private final LocationService locationService;
  private final OrganizationService organizationService;
  private final EncounterParticipantService encounterParticipantService;
  private final EpisodeOfCareService episodeOfCareService;
  private final ReasonService reasonService;

  public Encounter transform(ClinicalDocumentDocument1 document) {

    POCDMT000002UK01ClinicalDocument1 clinicalDocument = document.getClinicalDocument();

    Encounter encounter = new Encounter();
    encounter.setStatus(EncounterStatus.FINISHED);
    episodeOfCareService.createEpisodeOfCare(clinicalDocument)
        .ifPresent(encounter::addEpisodeOfCare);
    encounterParticipantService.createParticipants(clinicalDocument)
        .forEach(encounter::addParticipant);
    encounter.setPeriod(getEncounterPeriod(clinicalDocument));
    locationService.createLocation(clinicalDocument)
        .ifPresent(locationRef -> encounter.addLocation().setLocation(locationRef));
    organizationService.createServiceProvider(clinicalDocument)
        .ifPresent(encounter::setServiceProvider);
    reasonService.createReason(clinicalDocument)
        .forEach(encounter::addReason);
//    encounter.addType(); //TODO: No mapping exists.
//    encounter.setSubject() TODO: NCTH-380
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
