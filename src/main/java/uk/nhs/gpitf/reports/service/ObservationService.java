package uk.nhs.gpitf.reports.service;

import org.hl7.fhir.dstu3.model.Encounter;
import org.hl7.fhir.dstu3.model.Observation;
import org.hl7.fhir.dstu3.model.Reference;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import uk.nhs.gpitf.reports.model.InputBundle;
import uk.nhs.gpitf.reports.transform.ObservationTransformer;

@Service
@RequiredArgsConstructor
public class ObservationService {

  private final FhirStorageService fhirStorageService;
  private final ObservationTransformer observationTransformer;

  public Reference createObservation(InputBundle inputBundle, Reference transformerDevice, Encounter encounter) {
    Observation observation = observationTransformer.transform(inputBundle.getClinicalDocument(), transformerDevice, encounter);
    if (observation != null) {
      inputBundle.addResource(observation);
      return create(observation);
    }
    return null;
  }
  
  public Reference create(Observation observation) {
    return fhirStorageService.create(observation);
  }
}
