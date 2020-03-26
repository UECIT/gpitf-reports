package uk.nhs.gpitf.reports.transform;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.URL;
import org.apache.xmlbeans.XmlException;
import org.hl7.fhir.dstu3.model.Consent;
import org.hl7.fhir.dstu3.model.Consent.ConsentState;
import org.hl7.fhir.dstu3.model.Encounter;
import org.hl7.fhir.dstu3.model.Reference;
import org.junit.Before;
import org.junit.Test;
import uk.nhs.connect.iucds.cda.ucr.ClinicalDocumentDocument1.Factory;
import uk.nhs.connect.iucds.cda.ucr.POCDMT000002UK01ClinicalDocument1;

public class ConsentTransformerTest {

  private ConsentTransformer consentTransformer;

  private POCDMT000002UK01ClinicalDocument1 clinicalDocument;
  private Encounter encounter;

  private Reference patientRef = new Reference("Patient/1");

  @Before
  public void setup() throws IOException, XmlException {
    consentTransformer = new ConsentTransformer();

    URL resource = getClass().getResource("/example-clinical-doc.xml");
    clinicalDocument = Factory.parse(resource).getClinicalDocument();

    encounter = new Encounter();
    encounter
        .setSubject(patientRef)
        .setId("Encounter/1");
  }


  @Test
  public void testTransform() {

    Consent consent = consentTransformer
        .transform(clinicalDocument, encounter);

    assertEquals(ConsentState.ACTIVE, consent.getStatus());

    assertTrue("data (context)",
        new Reference(encounter).equalsDeep(consent.getDataFirstRep().getReference()));
    assertTrue("patient",
        encounter.getSubject().equalsDeep(consent.getPatient()));
    assertTrue("consentingParty",
        encounter.getSubject().equalsDeep(consent.getConsentingPartyFirstRep()));
    assertTrue("organization",
        encounter.getServiceProvider().equalsDeep(consent.getOrganizationFirstRep()));

    assertTrue("dateTime", consent.hasDateTime());
    assertTrue("dataPeriod.start", consent.getDataPeriod().hasStart());
    assertTrue("dataPeriod.end", consent.getDataPeriod().hasEnd());
    assertEquals("action", "425691002",
        consent.getActionFirstRep().getCodingFirstRep().getCode());


  }
}