package uk.nhs.gpitf.reports;

import lombok.RequiredArgsConstructor;
import org.apache.xmlbeans.XmlException;
import org.hl7.fhir.dstu3.model.Reference;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import uk.nhs.connect.iucds.cda.ucr.ClinicalDocumentDocument1;
import uk.nhs.gpitf.reports.service.EncounterReportService;
import uk.nhs.itk.envelope.DistributionEnvelopeDocument;

@RestController
@RequiredArgsConstructor
public class ReportController {

  private static final String CLINICAL_DOCUMENT_NODE_NAME = "ClinicalDocument";

  private final EncounterReportService encounterReportService;

  @PostMapping(consumes = MediaType.APPLICATION_XML_VALUE, path = "create")
  public ResponseEntity<String> createEncounterReport(@RequestBody String xmlReportString) {

    ClinicalDocumentDocument1 iucdsReport;

    try {
      DistributionEnvelopeDocument envelopedDocument =
          DistributionEnvelopeDocument.Factory.parse(xmlReportString);

      iucdsReport = ClinicalDocumentDocument1.Factory.parse(findClinicalDoc(envelopedDocument));

    } catch (XmlException e) {
      return ResponseEntity.badRequest()
          .body(e.getMessage());
    }

    Reference encounterRef = encounterReportService.createEncounterReport(iucdsReport);

    return ResponseEntity.ok(encounterRef.getReference());
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

      if (node.getNodeName().contains(CLINICAL_DOCUMENT_NODE_NAME)) {
        return node;
      }
    }
    throw new XmlException("No clinical document found in Envelope");
  }

}
