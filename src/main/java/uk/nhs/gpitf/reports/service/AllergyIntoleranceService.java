package uk.nhs.gpitf.reports.service;

import org.hl7.fhir.dstu3.model.AllergyIntolerance;
import org.hl7.fhir.dstu3.model.Encounter;
import org.hl7.fhir.dstu3.model.Reference;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import uk.nhs.gpitf.reports.model.InputBundle;
import uk.nhs.gpitf.reports.transform.AllergyIntoleranceTransformer;

@Service
@RequiredArgsConstructor
public class AllergyIntoleranceService {

  private final FhirStorageService fhirStorageService;
  private final AllergyIntoleranceTransformer allergyIntoleranceTransformer;

  public Reference createAllergyIntolerance(InputBundle inputBundle, Encounter encounter) {
    AllergyIntolerance allergyIntolerance = allergyIntoleranceTransformer.transform(inputBundle.getClinicalDocument(), encounter);
    if (allergyIntolerance != null) {
      inputBundle.addResource(allergyIntolerance);
      return create(allergyIntolerance);
    }
    return null;
  }
  
  public Reference create(AllergyIntolerance allergyIntolerance) {
    return fhirStorageService.create(allergyIntolerance);
  }
}
