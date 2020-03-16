package uk.nhs.gpitf.reports;

import org.apache.xmlbeans.XmlException;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import uk.nhs.connect.iucds.cda.ucr.ClinicalDocumentDocument1;
import uk.nhs.itk.envelope.DistributionEnvelopeDocument;

@RestController
public class ReportController {

  private static final String CLINICAL_DOCUMENT_NODE_NAME = "urn:ClinicalDocument";

  @PostMapping(consumes = MediaType.APPLICATION_XML_VALUE, path = "create")
  public ResponseEntity<String> createEncounterReport(@RequestBody String xmlReportString) {

    ClinicalDocumentDocument1 iucdsReport;

    try {
      DistributionEnvelopeDocument envelopedDocument =
          DistributionEnvelopeDocument.Factory.parse(xmlReportString);

      iucdsReport = ClinicalDocumentDocument1.Factory.parse(findClinicalDoc(envelopedDocument));

    } catch (XmlException e) {
      return ResponseEntity.badRequest()
          .body(e.getError().getMessage());
    }

    //TRANSFORM REPORT

    return ResponseEntity.ok()
        .build();
  }

  private Node findClinicalDoc(DistributionEnvelopeDocument envelopedDocument)
      throws XmlException {
    NodeList childNodes = envelopedDocument.getDistributionEnvelope()
        .getPayloads()
        .getPayloadArray(0)
        .getDomNode()
        .getChildNodes();

    for (int i=0; i < childNodes.getLength(); i++) {
      Node node = childNodes.item(i);

      if (node.getNodeName().equals(CLINICAL_DOCUMENT_NODE_NAME)) {
        return node;
      }
    }
    throw new XmlException("No clinical document found in Envelope");
  }

}
