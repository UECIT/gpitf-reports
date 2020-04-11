package uk.nhs.gpitf.reports.service;

import org.hl7.fhir.dstu3.model.ClinicalImpression;
import org.hl7.fhir.dstu3.model.Encounter;
import org.hl7.fhir.dstu3.model.Reference;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import uk.nhs.gpitf.reports.model.InputBundle;
import uk.nhs.gpitf.reports.transform.ClinicalImpressionTransformer;

@Service
@RequiredArgsConstructor
public class ClinicalImpressionService {

  private final FhirStorageService fhirStorageService;
  private final ClinicalImpressionTransformer clinicalImpressionTransformer;

  public Reference createClinicalImpression(InputBundle inputBundle, Encounter encounter) {
    ClinicalImpression clinicalImpression = clinicalImpressionTransformer.transform(inputBundle.getClinicalDocument(), encounter);
    if (clinicalImpression != null) {
      inputBundle.addResource(clinicalImpression);
      return create(clinicalImpression);
    }
    return null;
  }
  
  public Reference create(ClinicalImpression clinicalImpression) {
    return fhirStorageService.create(clinicalImpression);
  }
}
