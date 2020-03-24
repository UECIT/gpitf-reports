package uk.nhs.gpitf.reports.transform;

import java.util.stream.Stream;
import org.hl7.fhir.dstu3.model.HumanName;
import org.springframework.stereotype.Component;
import uk.nhs.connect.iucds.cda.ucr.PN;
import uk.nhs.gpitf.reports.util.NodeUtil;

@Component
public class HumanNameTransformer {
  public HumanName transform(PN personName) {
    var name = new HumanName();
    if (!NodeUtil.hasSubNodes(personName)) {
      return name.setText(NodeUtil.getNodeValueString(personName));
    }

    Stream.of(personName.getGivenArray())
        .map(NodeUtil::getNodeValueString)
        .forEach(name::addGiven);

    Stream.of(personName.getPrefixArray())
        .map(NodeUtil::getNodeValueString)
        .forEach(name::addPrefix);

    if (personName.sizeOfFamilyArray() >= 1) {
      name.setFamily(NodeUtil.getNodeValueString(personName.getFamilyArray(0)));
    }

    return name;
  }
}
