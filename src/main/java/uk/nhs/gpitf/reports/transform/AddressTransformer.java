package uk.nhs.gpitf.reports.transform;

import java.util.Arrays;
import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.dstu3.model.Address;
import org.springframework.stereotype.Component;
import uk.nhs.connect.iucds.cda.ucr.AD;
import uk.nhs.gpitf.reports.util.NodeUtil;

@Component
public class AddressTransformer {

  public Address transform(AD addr) {
    Address address = new Address();

    Arrays.stream(addr.getStreetAddressLineArray())
        .map(NodeUtil::getNodeValueString)
        .forEach(address::addLine);

    if (addr.sizeOfPostalCodeArray() > 0) {
      address.setPostalCode(NodeUtil.getNodeValueString(addr.getPostalCodeArray(0)));
    }

    if (addr.sizeOfCityArray() > 0) {
      address.setCity(NodeUtil.getNodeValueString(addr.getCityArray(0)));
    }

    if (addr.sizeOfDescArray() > 0) {
      address.setText(NodeUtil.getNodeValueString(addr.getDescArray(0)));
    }

    if (addr.sizeOfCountryArray() > 0) {
      address.setCountry(NodeUtil.getNodeValueString(addr.getCountryArray(0)));
    }

    return address;
  }
}
