package uk.nhs.gpitf.reports.transform;

import java.util.Arrays;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import org.hl7.fhir.dstu3.model.Address;
import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.Coding;
import org.hl7.fhir.dstu3.model.Location;
import org.hl7.fhir.dstu3.model.StringType;
import org.springframework.stereotype.Component;
import uk.nhs.connect.iucds.cda.ucr.AD;
import uk.nhs.connect.iucds.cda.ucr.CE;
import uk.nhs.connect.iucds.cda.ucr.POCDMT000002UK01HealthCareFacility;
import uk.nhs.connect.iucds.cda.ucr.POCDMT000002UK01Location;
import uk.nhs.connect.iucds.cda.ucr.POCDMT000002UK01Place;
import uk.nhs.gpitf.reports.constants.FHIRSystems;
import uk.nhs.gpitf.reports.util.IdUtil;
import uk.nhs.gpitf.reports.util.NodeUtil;

@Component
public class LocationTransformer {

  public Location transform(POCDMT000002UK01Location documentLocation) {

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
        location.setAddress(getAddress(place.getAddr()));
      }
      if (place.isSetName()) {
        location.setName(NodeUtil.getNodeValueString(place.getName().getDomNode()));
      }
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

  private Address getAddress(AD addr) {

    Address address = new Address();

    address.setLine(Arrays.stream(addr.getStreetAddressLineArray())
          .map(line -> NodeUtil.getNodeValueString(line.getDomNode()))
          .map(StringType::new)
          .collect(Collectors.toList()));

    if (addr.sizeOfPostalCodeArray() > 0) {
      address.setPostalCode(NodeUtil.getNodeValueString(addr.getPostalCodeArray(0).getDomNode()));
    }

    if (addr.sizeOfCityArray() > 0) {
      address.setCity(NodeUtil.getNodeValueString(addr.getCityArray(0).getDomNode()));
    }

    if (addr.sizeOfDescArray() > 0) {
      address.setText(NodeUtil.getNodeValueString(addr.getDescArray(0).getDomNode()));
    }

    if (addr.sizeOfCountryArray() > 0) {
      address.setCountry(NodeUtil.getNodeValueString(addr.getCountryArray(0).getDomNode()));
    }

    return address;
  }

}
