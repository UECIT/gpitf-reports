package uk.nhs.gpitf.reports.service;

import lombok.RequiredArgsConstructor;
import org.hl7.fhir.dstu3.model.Composition;
import org.hl7.fhir.dstu3.model.Encounter;
import org.hl7.fhir.dstu3.model.Reference;
import org.springframework.stereotype.Service;
import uk.nhs.gpitf.reports.model.InputBundle;
import uk.nhs.gpitf.reports.transform.CompositionTransformer;

@Service
@RequiredArgsConstructor
public class CompositionService {

  private final FhirStorageService storageService;
  private final CompositionTransformer compositionTransformer;

  public Reference createList(InputBundle inputBundle, Reference device, Encounter encounter) {
    Composition composition = compositionTransformer.transform(inputBundle, device, encounter);
    return storageService.create(composition);
  }
}
