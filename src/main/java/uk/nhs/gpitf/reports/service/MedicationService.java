package uk.nhs.gpitf.reports.service;

import org.hl7.fhir.dstu3.model.Encounter;
import org.hl7.fhir.dstu3.model.MedicationStatement;
import org.hl7.fhir.dstu3.model.Reference;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import uk.nhs.gpitf.reports.model.InputBundle;
import uk.nhs.gpitf.reports.transform.MedicationTransformer;

@Service
@RequiredArgsConstructor
public class MedicationService {

  private final FhirStorageService fhirStorageService;
  private final MedicationTransformer medicationTransformer;

  public Reference createMedication(InputBundle inputBundle, Encounter encounter) {
    MedicationStatement medication = medicationTransformer.transform(inputBundle.getClinicalDocument(), encounter);
    if (medication != null) {
      inputBundle.addResource(medication);
      return create(medication);
    }
    return null;
  }
  
  public Reference create(MedicationStatement medication) {
    return fhirStorageService.create(medication);
  }
}
