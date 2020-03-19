package uk.nhs.gpitf.reports;

import lombok.experimental.UtilityClass;
import org.apache.xmlbeans.XmlString.Factory;
import uk.nhs.connect.iucds.cda.ucr.AD;
import uk.nhs.connect.iucds.cda.ucr.CE;
import uk.nhs.connect.iucds.cda.ucr.EN;
import uk.nhs.connect.iucds.cda.ucr.II;
import uk.nhs.connect.iucds.cda.ucr.ON;
import uk.nhs.connect.iucds.cda.ucr.PN;
import uk.nhs.connect.iucds.cda.ucr.TEL;
import uk.nhs.gpitf.reports.constants.FHIRSystems;
import uk.nhs.gpitf.reports.constants.IUCDSSystems;

@UtilityClass
public class Stub {

  public CE code() {
    CE code = CE.Factory.newInstance();
    code.setCode("CODE");
    code.setDisplayName("DISPLAY");

    return code;
  }

  public EN name() {
    EN name = EN.Factory.newInstance();

    name.set(Factory.newValue("Name of location"));

    return name;
  }

  public ON orgName() {
    ON name = ON.Factory.newInstance();

    name.set(Factory.newValue("Name of organization"));

    return name;
  }

  public PN personName() {
    PN name = PN.Factory.newInstance();

    name.set(Factory.newValue("Name of person"));
    name.addNewGiven().set(Factory.newValue("Homer"));
    name.addNewFamily().set(Factory.newValue("Simpson"));

    return name;
  }

  public AD addr() {
    AD addr = AD.Factory.newInstance();

    addr.addNewStreetAddressLine().set(Factory.newValue("1 Main Street"));
    addr.addNewCity().set(Factory.newValue("City"));
    addr.addNewPostalCode().set(Factory.newValue("NE1 4HH"));
    addr.addNewCountry().set(Factory.newValue("UK"));
    addr.addNewDesc().set(Factory.newValue("The Address of the Location"));
    return addr;
  }

  public II odsSite() {
    II ii = II.Factory.newInstance();

    ii.setRoot(FHIRSystems.ODS_SITE);
    ii.setExtension("SITE");
    return ii;
  }

  public static II odsCode() {
    II ii = II.Factory.newInstance();

    ii.setRoot(IUCDSSystems.ODS_ORGANIZATION);
    ii.setExtension("ODS_CODE");
    return ii;
  }

  public static TEL tel() {
    TEL tel = TEL.Factory.newInstance();
    tel.setValue("012345678");
    return tel;
  }
}
