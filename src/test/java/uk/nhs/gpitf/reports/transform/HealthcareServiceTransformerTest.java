package uk.nhs.gpitf.reports.transform;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import org.apache.xmlbeans.XmlException;
import org.hl7.fhir.dstu3.model.HealthcareService;
import org.hl7.fhir.dstu3.model.Reference;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.nhs.connect.iucds.cda.ucr.ClinicalDocumentDocument1;
import uk.nhs.connect.iucds.cda.ucr.ClinicalDocumentDocument1.Factory;
import uk.nhs.connect.iucds.cda.ucr.POCDMT000002UK01InformationRecipient;
import uk.nhs.connect.iucds.cda.ucr.POCDMT000002UK01IntendedRecipient;
import uk.nhs.gpitf.reports.service.LocationService;
import uk.nhs.gpitf.reports.service.OrganizationService;

@RunWith(MockitoJUnitRunner.class)
public class HealthcareServiceTransformerTest {

  @Mock
  private LocationService locationService;

  @Mock
  private OrganizationService organizationService;

  @InjectMocks
  private HealthcareServiceTransformer HealthcareServiceTransformer;

  private ClinicalDocumentDocument1 clinicalDocument;

  private Reference organizationRef = new Reference("Organization/1");
  private Reference locationRef = new Reference("Location/1");

  @Before
  public void setup() throws IOException, XmlException {
    URL resource = getClass().getResource("/example-clinical-doc.xml");
    clinicalDocument = Factory.parse(resource);

    POCDMT000002UK01IntendedRecipient intendedRecipient = clinicalDocument.getClinicalDocument()
        .getInformationRecipientArray(0).getIntendedRecipient();
    when(locationService.createFromIntendedRecipient(intendedRecipient))
        .thenReturn(Optional.of(locationRef));
    when(organizationService.createOrganization(intendedRecipient.getReceivedOrganization()))
        .thenReturn(organizationRef);
  }

  @Test
  public void testTransform() {
    POCDMT000002UK01InformationRecipient recipient =
        clinicalDocument.getClinicalDocument().getInformationRecipientArray(0);

    HealthcareService healthcareService = HealthcareServiceTransformer
        .transformRecipient(recipient);

    verify(organizationService)
        .createOrganization(recipient.getIntendedRecipient().getReceivedOrganization());
    verify(locationService).createFromIntendedRecipient(recipient.getIntendedRecipient());

    assertEquals("name", "Thames Medical Practice", healthcareService.getName());
    assertEquals("active", true, healthcareService.getActive());
    assertTrue("providedBy", organizationRef.equalsDeep(healthcareService.getProvidedBy()));
    assertTrue("location", locationRef.equalsDeep(healthcareService.getLocationFirstRep()));
    assertEquals("telecom", "tel:0123476895", healthcareService.getTelecomFirstRep().getValue());
  }
}
