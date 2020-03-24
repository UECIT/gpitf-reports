package uk.nhs.gpitf.reports.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.apache.xmlbeans.XmlObject;
import org.junit.Test;
import org.w3c.dom.Node;

public class NodeUtilTest {
  @Test
  public void getNodeString_nullNode() {
    var xmlObject = mock(XmlObject.class);

    assertNull(NodeUtil.getNodeValueString(xmlObject));
  }

  @Test
  public void getNodeString_nullChild() {
    var xmlObject = mock(XmlObject.class);
    var innerNode = mock(Node.class);
    when(xmlObject.getDomNode()).thenReturn(innerNode);

    assertNull(NodeUtil.getNodeValueString(xmlObject));
  }

  @Test
  public void getNodeString_nullValue() {
    var xmlObject = mock(XmlObject.class);
    var innerNode = mock(Node.class);
    when(xmlObject.getDomNode()).thenReturn(innerNode);
    var childNode = mock(Node.class);
    when(innerNode.getFirstChild()).thenReturn(childNode);

    assertNull(NodeUtil.getNodeValueString(xmlObject));
  }

  @Test
  public void getNodeString_happy() {
    var xmlObject = mock(XmlObject.class);
    var innerNode = mock(Node.class);
    when(xmlObject.getDomNode()).thenReturn(innerNode);
    var childNode = mock(Node.class);
    when(innerNode.getFirstChild()).thenReturn(childNode);
    when(childNode.getNodeValue()).thenReturn("happy path");

    assertEquals(NodeUtil.getNodeValueString(xmlObject), "happy path");
  }
}
