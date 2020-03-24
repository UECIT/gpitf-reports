package uk.nhs.gpitf.reports.util;

import java.util.Optional;
import org.apache.xmlbeans.XmlObject;
import lombok.experimental.UtilityClass;
import org.w3c.dom.Node;

@UtilityClass
public class NodeUtil {
  public String getNodeValueString(XmlObject xmlObject) {
    return Optional.ofNullable(xmlObject)
        .map(XmlObject::getDomNode)
        .map(Node::getFirstChild)
        .map(Node::getNodeValue)
        .orElse(null);
  }

  public boolean hasSubNodes(XmlObject xmlObject) {
    var node = xmlObject.getDomNode();
    return node.getChildNodes().getLength() != 1
        || node.getFirstChild().getNodeType() != Node.TEXT_NODE;
  }
}

