package uk.nhs.gpitf.reports.transform;

import static org.junit.Assert.assertEquals;
import java.io.IOException;
import java.net.URL;
import org.apache.xmlbeans.XmlException;
import org.hl7.fhir.dstu3.model.Encounter;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.Observation;
import org.hl7.fhir.dstu3.model.Observation.ObservationStatus;
import org.hl7.fhir.dstu3.model.Reference;
import org.junit.Before;
import org.junit.Test;
import uk.nhs.connect.iucds.cda.ucr.ClinicalDocumentDocument1.Factory;
import uk.nhs.connect.iucds.cda.ucr.POCDMT000002UK01ClinicalDocument1;

public class ObservationTransformerTest {

  private ObservationTransformer observationTransformer;

  private POCDMT000002UK01ClinicalDocument1 clinicalDocument;
  private Encounter encounter;

  private Reference patientRef = new Reference("Patient/1");
  private Reference deviceRef = new Reference("Device/1");

  @Before
  public void setup() throws IOException, XmlException {
    observationTransformer = new ObservationTransformer();

    URL resource = getClass().getResource("/example-clinical-doc.xml");
    clinicalDocument = Factory.parse(resource).getClinicalDocument();

    encounter = new Encounter();
    encounter
        .setSubject(patientRef)
        .setId("Encounter/1")
        .setIdElement(new IdType().setValue("Encounter/1"));
  }


  @Test
  public void testTransform() {

    Observation observation = observationTransformer
        .transform(clinicalDocument, deviceRef, encounter);

    assertEquals(ObservationStatus.PRELIMINARY, observation.getStatus());

  }
}