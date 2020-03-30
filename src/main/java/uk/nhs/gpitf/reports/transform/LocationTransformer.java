package uk.nhs.gpitf.reports.transform;

import java.util.function.Consumer;
import lombok.RequiredArgsConstructor;
import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.Coding;
import org.hl7.fhir.dstu3.model.Location;
import org.springframework.stereotype.Component;
import uk.nhs.connect.iucds.cda.ucr.CE;
import uk.nhs.connect.iucds.cda.ucr.POCDMT000002UK01HealthCareFacility;
import uk.nhs.connect.iucds.cda.ucr.POCDMT000002UK01IntendedRecipient;
import uk.nhs.connect.iucds.cda.ucr.POCDMT000002UK01Location;
import uk.nhs.connect.iucds.cda.ucr.POCDMT000002UK01ParticipantRole;
import uk.nhs.connect.iucds.cda.ucr.POCDMT000002UK01Place;
import uk.nhs.connect.iucds.cda.ucr.POCDMT000002UK01PlayingEntity;
import uk.nhs.gpitf.reports.constants.FHIRSystems;
import uk.nhs.gpitf.reports.util.IdUtil;
import uk.nhs.gpitf.reports.util.NodeUtil;

@Component
@RequiredArgsConstructor
public class LocationTransformer {

  private final AddressTransformer addressTransformer;

  public Location transformLocation(POCDMT000002UK01Location documentLocation) {

    POCDMT000002UK01HealthCareFacility healthCareFacility = documentLocation
        .getHealthCareFacility();

    Location location = new Location();

    //TODO: This isn't valid?
    IdUtil.getOdsSite(healthCareFacility.getIdArray())
        .ifPresent(addOdsSite(location));

    location.setType(getType(healthCareFacility));

    if (healthCareFacility.isSetLocation()) {
      POCDMT000002UK01Place place = healthCareFacility.getLocation();
      if (place.isSetAddr()) {
        location.setAddress(addressTransformer.transform(place.getAddr()));
      }
      if (place.isSetName()) {
        location.setName(NodeUtil.getNodeValueString(place.getName()));
      }
    }

    return location;
  }

  public Location transformIntendedRecipientLocation(
      POCDMT000002UK01IntendedRecipient intendedRecipient) {
    Location location = new Location();
    if (intendedRecipient.sizeOfAddrArray() > 0) {
      location.setAddress(addressTransformer.transform(intendedRecipient.getAddrArray(0)));
    }
    return location;
  }

  public Location transformParticipantRole(POCDMT000002UK01ParticipantRole participantRole) {

    Location location = new Location();
    if (participantRole.sizeOfAddrArray() > 0) {
      location.setAddress(addressTransformer.transform(participantRole.getAddrArray(0)));
    }

    if (participantRole.isSetPlayingEntity()) {
      POCDMT000002UK01PlayingEntity playingEntity = participantRole.getPlayingEntity();
      location.setName(NodeUtil.getNodeValueString(playingEntity.getNameArray(0)));
      location.setDescription(NodeUtil.getNodeValueString(playingEntity.getDesc()));
    }

    return location;
  }

  private Consumer<String> addOdsSite(Location location) {
    return site -> location.addIdentifier()
        .setSystem(FHIRSystems.ODS_SITE)
        .setValue(site);
  }

  private CodeableConcept getType(POCDMT000002UK01HealthCareFacility healthCareFacility) {
    CE code = healthCareFacility.getCode();
    return new CodeableConcept()
        .addCoding(
            new Coding(
                FHIRSystems.SERVICE_DELIVERY_LOCATION_ROLE_TYPE,
                code.getCode(),
                code.getDisplayName()));
  }

}
