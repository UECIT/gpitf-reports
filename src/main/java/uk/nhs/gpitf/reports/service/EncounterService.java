package uk.nhs.gpitf.reports.service;

import lombok.RequiredArgsConstructor;
import org.hl7.fhir.dstu3.model.Encounter;
import org.springframework.stereotype.Service;
import uk.nhs.gpitf.reports.model.InputBundle;
import uk.nhs.gpitf.reports.transform.EncounterTransformer;

@Service
@RequiredArgsConstructor
public class EncounterService extends TrackingResourceCreationsService {

  private final EncounterTransformer encounterTransformer;

  public Encounter createEncounter(InputBundle inputBundle) {
    Encounter encounter = encounterTransformer.transform(inputBundle);
    create(encounter, inputBundle);
    return encounter;
  }

}
