package uk.nhs.gpitf.reports;

import lombok.experimental.UtilityClass;
import org.apache.xmlbeans.XmlString.Factory;
import uk.nhs.connect.iucds.cda.ucr.AD;
import uk.nhs.connect.iucds.cda.ucr.CE;
import uk.nhs.connect.iucds.cda.ucr.CV;
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

  public PN fullPersonName() {
    PN name = PN.Factory.newInstance();

    name.addNewGiven().set(Factory.newValue("Homer"));
    name.addNewFamily().set(Factory.newValue("Simpson"));

    return name;
  }

  public PN simplePersonName() {
    PN name = PN.Factory.newInstance();

    name.set(Factory.newValue("Stewie Griffin"));

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
    return createII(FHIRSystems.ODS_SITE, "SITE");
  }

  public II odsCode() {
    return createII(IUCDSSystems.ODS_ORGANIZATION, "ODS_CODE");
  }

  public TEL tel() {
    TEL tel = TEL.Factory.newInstance();
    tel.setValue("012345678");
    return tel;
  }

  public II createII(String root, String extension, String assigner) {
    II ii = createII(root, extension);
    ii.setAssigningAuthorityName(assigner);
    return ii;
  }

  public II createII(String root, String extension) {
    II ii = II.Factory.newInstance();
    ii.setRoot(root);
    ii.setExtension(extension);
    return ii;
  }

  public CV codedValue(String system, String code, String display) {
    CV cv = CV.Factory.newInstance();
    cv.setCodeSystem(system);
    cv.setCode(code);
    cv.setDisplayName(display);
    return cv;
  }
}
