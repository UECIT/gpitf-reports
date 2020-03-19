package uk.nhs.gpitf.reports.util;

import lombok.experimental.UtilityClass;
import org.w3c.dom.Node;

@UtilityClass
public class NodeUtil {

  public String getNodeValueString(Node node) {
    if (node == null) {
      return null;
    }
    Node firstChild = node.getFirstChild();
    if (firstChild == null) {
      return null;
    }
    return firstChild.getNodeValue();
  }

}

