package uk.nhs.gpitf.reports.service;

import lombok.RequiredArgsConstructor;
import org.hl7.fhir.dstu3.model.Encounter;
import org.hl7.fhir.dstu3.model.Reference;
import org.springframework.stereotype.Service;
import uk.nhs.connect.iucds.cda.ucr.ClinicalDocumentDocument1;
import uk.nhs.gpitf.reports.transform.EncounterTransformer;

@Service
@RequiredArgsConstructor
public class EncounterReportService {

  private final EncounterTransformer encounterTransformer;

  private final FhirStorageService storageService;
  private final CarePlanService carePlanService;

  public Reference createEncounterReport(ClinicalDocumentDocument1 document) {

    Encounter encounter = encounterTransformer.transform(document);
    Reference encounterRef = storageService.create(encounter);

    carePlanService.createCarePlans(document, encounterRef);

    return encounterRef;
  }

}
