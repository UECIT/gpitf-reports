package uk.nhs.gpitf.reports.service;

import lombok.RequiredArgsConstructor;
import org.hl7.fhir.dstu3.model.Practitioner;
import org.hl7.fhir.dstu3.model.Reference;
import org.springframework.stereotype.Service;
import uk.nhs.connect.iucds.cda.ucr.POCDMT000002UK01Person;
import uk.nhs.gpitf.reports.model.InputBundle;
import uk.nhs.gpitf.reports.transform.PractitionerTransformer;

@Service
@RequiredArgsConstructor
public class PractitionerService {

  private final PractitionerTransformer practitionerTransformer;

  private final FhirStorageService storageService;

  public Reference createPractitioner(InputBundle inputBundle, POCDMT000002UK01Person assignedPerson, String displayName, String name) {
    Practitioner practitioner = practitionerTransformer.transform(assignedPerson, displayName, name);
    inputBundle.addResource(practitioner);
    return storageService.create(practitioner);
  }
}
