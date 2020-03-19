package uk.nhs.gpitf.reports.service;

import lombok.RequiredArgsConstructor;
import org.hl7.fhir.dstu3.model.Practitioner;
import org.hl7.fhir.dstu3.model.Reference;
import org.springframework.stereotype.Service;
import uk.nhs.connect.iucds.cda.ucr.POCDMT000002UK01Person;
import uk.nhs.gpitf.reports.transform.PractitionerTransformer;

@Service
@RequiredArgsConstructor
public class PractitionerService {

  private final PractitionerTransformer practitionerTransformer;

  private final FhirStorageService storageService;

  public Reference createPractitioner(POCDMT000002UK01Person assignedPerson) {
    Practitioner practitioner = practitionerTransformer.transform(assignedPerson);
    return storageService.create(practitioner);
  }
}
