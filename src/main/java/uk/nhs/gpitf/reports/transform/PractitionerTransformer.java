package uk.nhs.gpitf.reports.transform;

import org.hl7.fhir.dstu3.model.HumanName;
import org.hl7.fhir.dstu3.model.Practitioner;
import org.springframework.stereotype.Component;
import uk.nhs.connect.iucds.cda.ucr.PN;
import uk.nhs.connect.iucds.cda.ucr.POCDMT000002UK01Person;
import uk.nhs.gpitf.reports.util.NodeUtil;

@Component
public class PractitionerTransformer {

  public Practitioner transform(POCDMT000002UK01Person person) {
    Practitioner practitioner = new Practitioner();
    PN name = person.getNameArray(0);

    HumanName humanName = new HumanName();
    if (name.sizeOfGivenArray() > 0) {
      humanName.addGiven(NodeUtil.getNodeValueString(name.getGivenArray(0).getDomNode()));
    }
    if (name.sizeOfFamilyArray() > 0) {
      humanName.setFamily(NodeUtil.getNodeValueString(name.getFamilyArray(0).getDomNode()));
    }

    practitioner.addName(humanName);
    return practitioner;
  }

}
