package uk.nhs.gpitf.reports.service;

import lombok.RequiredArgsConstructor;
import org.apache.xmlbeans.XmlException;
import org.hl7.fhir.dstu3.model.Encounter;
import org.hl7.fhir.dstu3.model.Reference;
import org.springframework.stereotype.Service;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import uk.nhs.connect.iucds.cda.ucr.ClinicalDocumentDocument1;
import uk.nhs.gpitf.reports.transform.EncounterTransformer;
import uk.nhs.itk.envelope.DistributionEnvelopeDocument;

@Service
@RequiredArgsConstructor
public class EncounterReportService {

  private static final String CLINICAL_DOCUMENT_NODE_NAME = "ClinicalDocument";

  private final EncounterTransformer encounterTransformer;

  private final FhirStorageService storageService;
  private final CarePlanService carePlanService;

  public Reference createEncounterReport(String xmlReportString) throws XmlException {

    ClinicalDocumentDocument1 document;

    DistributionEnvelopeDocument envelopedDocument =
        DistributionEnvelopeDocument.Factory.parse(xmlReportString);

    document = ClinicalDocumentDocument1.Factory.parse(findClinicalDoc(envelopedDocument));

    Encounter encounter = encounterTransformer.transform(document);
    Reference encounterRef = storageService.create(encounter);

    carePlanService.createCarePlans(document, encounterRef);

    return encounterRef;
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
