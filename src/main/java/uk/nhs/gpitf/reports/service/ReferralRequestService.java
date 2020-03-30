package uk.nhs.gpitf.reports.service;

import lombok.RequiredArgsConstructor;
import org.hl7.fhir.dstu3.model.Encounter;
import org.hl7.fhir.dstu3.model.Reference;
import org.hl7.fhir.dstu3.model.ReferralRequest;
import org.springframework.stereotype.Service;
import uk.nhs.gpitf.reports.model.InputBundle;
import uk.nhs.gpitf.reports.transform.ReferralRequestTransformer;

@Service
@RequiredArgsConstructor
public class ReferralRequestService extends TrackingResourceCreationsService {

  private final ReferralRequestTransformer referralRequestTransformer;

  public Reference createReferralRequest(
      InputBundle inputBundle,
      Encounter encounter,
      Reference transformerDevice) {
    ReferralRequest referralRequest = referralRequestTransformer.transform(
        inputBundle, encounter, transformerDevice);
    return create(referralRequest, inputBundle);
  }
}
