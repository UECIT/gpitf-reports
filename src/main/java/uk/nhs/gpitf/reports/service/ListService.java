package uk.nhs.gpitf.reports.service;

import lombok.RequiredArgsConstructor;
import org.hl7.fhir.dstu3.model.Encounter;
import org.hl7.fhir.dstu3.model.ListResource;
import org.hl7.fhir.dstu3.model.Reference;
import org.springframework.stereotype.Service;
import uk.nhs.gpitf.reports.model.InputBundle;
import uk.nhs.gpitf.reports.transform.ListTransformer;

@Service
@RequiredArgsConstructor
public class ListService {

  private final ListTransformer listTransformer;
  private final FhirStorageService storageService;

  public Reference createList(InputBundle inputBundle, Reference device, Encounter encounter) {
    ListResource list = listTransformer.transform(inputBundle, device, encounter);
    return storageService.create(list);
  }

}
