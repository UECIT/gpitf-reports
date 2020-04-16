package uk.nhs.gpitf.reports.transform;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import java.io.IOException;
import java.net.URL;
import org.apache.xmlbeans.XmlException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import uk.nhs.connect.iucds.cda.ucr.CE;
import uk.nhs.connect.iucds.cda.ucr.ClinicalDocumentDocument1.Factory;
import uk.nhs.connect.iucds.cda.ucr.POCDMT000002UK01AssignedEntity;
import uk.nhs.connect.iucds.cda.ucr.POCDMT000002UK01Organization;
import uk.nhs.connect.iucds.cda.ucr.POCDMT000002UK01Person;
import uk.nhs.gpitf.reports.model.InputBundle;
import uk.nhs.gpitf.reports.service.OrganizationService;
import uk.nhs.gpitf.reports.service.PractitionerService;

@RunWith(MockitoJUnitRunner.class)
public class EpisodeOfCareTransformerTest {

  @InjectMocks
  private EpisodeOfCareTransformer episodeOfCareTransformer;

  @Mock
  private PractitionerService practitionerService;

  @Mock
  private OrganizationService organizationService;

  private POCDMT000002UK01AssignedEntity assignedEntity;
  private InputBundle inputBundle;
  
  @Before
  public void setup() throws XmlException, IOException {
    assignedEntity = POCDMT000002UK01AssignedEntity.Factory.newInstance();
    URL resource = getClass().getResource("/example-clinical-doc.xml");
    inputBundle = new InputBundle();
    inputBundle.setClinicalDocument(Factory.parse(resource).getClinicalDocument());
  }

  @Test
  public void testWithAssignedPerson() {
    POCDMT000002UK01Person person = assignedEntity.addNewAssignedPerson();

    episodeOfCareTransformer.transformEpisodeOfCare(inputBundle, assignedEntity);

    verify(practitionerService).createPractitioner(any(), any(), any(), any());
    verify(organizationService, never()).createOrganization(any(), any());
  }

  @Test
  public void testWithRepOrg() {
    POCDMT000002UK01Organization org = assignedEntity.addNewRepresentedOrganization();

    episodeOfCareTransformer.transformEpisodeOfCare(inputBundle, assignedEntity);

    verify(practitionerService, never()).createPractitioner(any(), any(), any(), any());
    verify(organizationService).createOrganization(inputBundle, org);
  }

  @Test
  public void testWithAssignedPersonAndRepOrg() {
    POCDMT000002UK01Person person = assignedEntity.addNewAssignedPerson();
    POCDMT000002UK01Organization org = assignedEntity.addNewRepresentedOrganization();

    episodeOfCareTransformer.transformEpisodeOfCare(inputBundle, assignedEntity);

    verify(practitionerService).createPractitioner(any(), any(), any(), any());
    verify(organizationService).createOrganization(inputBundle, org);
  }

  @Test
  public void testEmptyAssignedEntity() {
    episodeOfCareTransformer.transformEpisodeOfCare(inputBundle, assignedEntity);

    verify(practitionerService, never()).createPractitioner(any(), any(), any(), any());
    verify(organizationService, never()).createOrganization(any(), any());
  }

}