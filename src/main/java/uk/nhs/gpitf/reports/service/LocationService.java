package uk.nhs.gpitf.reports.service;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.hl7.fhir.dstu3.model.Location;
import org.hl7.fhir.dstu3.model.Reference;
import org.springframework.stereotype.Service;
import uk.nhs.connect.iucds.cda.ucr.POCDMT000002UK01ClinicalDocument1;
import uk.nhs.connect.iucds.cda.ucr.POCDMT000002UK01Location;
import uk.nhs.gpitf.reports.transform.LocationTransformer;

@Service
@RequiredArgsConstructor
public class LocationService {

  private final LocationTransformer locationTransformer;

  private final FhirStorageService storageService;

  public Optional<Reference> createLocation(POCDMT000002UK01ClinicalDocument1 clinicalDocument) {

    POCDMT000002UK01Location documentLocation = clinicalDocument.getComponentOf()
        .getEncompassingEncounter()
        .getLocation();

    if (documentLocation == null) {
      return Optional.empty();
    }

    Location location = locationTransformer.transform(documentLocation);

    return Optional.of(storageService.create(location));
  }

}
