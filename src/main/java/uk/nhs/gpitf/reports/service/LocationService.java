package uk.nhs.gpitf.reports.service;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.hl7.fhir.dstu3.model.Location;
import org.hl7.fhir.dstu3.model.Reference;
import org.springframework.stereotype.Service;
import uk.nhs.connect.iucds.cda.ucr.POCDMT000002UK01ClinicalDocument1;
import uk.nhs.connect.iucds.cda.ucr.POCDMT000002UK01IntendedRecipient;
import uk.nhs.gpitf.reports.transform.LocationTransformer;

@Service
@RequiredArgsConstructor
public class LocationService {

  private final LocationTransformer locationTransformer;
  private final FhirStorageService storageService;

  public Optional<Reference> createFromEncompassingEncounter(
      POCDMT000002UK01ClinicalDocument1 clinicalDocument) {

    return Optional.ofNullable(
        clinicalDocument.getComponentOf()
            .getEncompassingEncounter()
            .getLocation())
        .map(locationTransformer::transformLocation)
        .map(this::create);
  }

  public Optional<Reference> createFromIntendedRecipient(
      POCDMT000002UK01IntendedRecipient intendedRecipient) {
    Location location = locationTransformer.transformIntendedRecipientLocation(intendedRecipient);
    return location.isEmpty() ? Optional.empty() : Optional.of(create(location));
  }

  public Reference create(Location location) {
    return storageService.create(location);
  }

}
