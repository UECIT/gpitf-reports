package uk.nhs.gpitf.reports.service;

import lombok.RequiredArgsConstructor;
import org.hl7.fhir.dstu3.model.HealthcareService;
import org.hl7.fhir.dstu3.model.Reference;
import org.springframework.stereotype.Service;
import uk.nhs.connect.iucds.cda.ucr.POCDMT000002UK01InformationRecipient;
import uk.nhs.gpitf.reports.transform.HealthcareServiceTransformer;

@Service
@RequiredArgsConstructor
public class HealthcareServiceService {

  private final FhirStorageService storageService;
  private final HealthcareServiceTransformer healthcareServiceTransformer;

  public Reference createHealthcareService(POCDMT000002UK01InformationRecipient recipient) {
    HealthcareService healthcareService =
        healthcareServiceTransformer.transformRecipient(recipient);

    return createHealthcareService(healthcareService);
  }

  public Reference createHealthcareService(HealthcareService healthcareService) {
    return storageService.create(healthcareService);
  }
}
