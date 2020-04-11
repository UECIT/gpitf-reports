package uk.nhs.gpitf.reports.service;

import lombok.RequiredArgsConstructor;
import org.hl7.fhir.dstu3.model.Consent;
import org.hl7.fhir.dstu3.model.Encounter;
import org.hl7.fhir.dstu3.model.Reference;
import org.springframework.stereotype.Service;
import uk.nhs.gpitf.reports.model.InputBundle;
import uk.nhs.gpitf.reports.transform.ConsentTransformer;

@Service
@RequiredArgsConstructor
public class ConsentService {

  private final FhirStorageService fhirStorageService;
  private final ConsentTransformer consentTransformer;

  public Reference createConsent(
      InputBundle inputBundle, Encounter encounter) {
    Consent consent = consentTransformer.transform(inputBundle.getClinicalDocument(), encounter);
    inputBundle.addResource(consent);
    return create(consent);
  }

  public Reference create(Consent consent) {
    return fhirStorageService.create(consent);
  }
}
