package uk.nhs.gpitf.reports.transform;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.nhs.connect.iucds.cda.ucr.POCDMT000002UK01AssignedEntity;
import uk.nhs.connect.iucds.cda.ucr.POCDMT000002UK01Organization;
import uk.nhs.connect.iucds.cda.ucr.POCDMT000002UK01Person;
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

  @Before
  public void setup() {
    assignedEntity = POCDMT000002UK01AssignedEntity.Factory.newInstance();
  }

  @Test
  public void testWithAssignedPerson() {
    POCDMT000002UK01Person person = assignedEntity.addNewAssignedPerson();

    episodeOfCareTransformer.transformEpisodeOfCare(assignedEntity);

    verify(practitionerService).createPractitioner(person);
    verify(organizationService, never()).createOrganization(any());
  }

  @Test
  public void testWithRepOrg() {
    POCDMT000002UK01Organization org = assignedEntity.addNewRepresentedOrganization();

    episodeOfCareTransformer.transformEpisodeOfCare(assignedEntity);

    verify(practitionerService, never()).createPractitioner(any());
    verify(organizationService).createOrganization(org);
  }

  @Test
  public void testWithAssignedPersonAndRepOrg() {
    POCDMT000002UK01Person person = assignedEntity.addNewAssignedPerson();
    POCDMT000002UK01Organization org = assignedEntity.addNewRepresentedOrganization();

    episodeOfCareTransformer.transformEpisodeOfCare(assignedEntity);

    verify(practitionerService).createPractitioner(person);
    verify(organizationService).createOrganization(org);
  }

  @Test
  public void testEmptyAssignedEntity() {
    episodeOfCareTransformer.transformEpisodeOfCare(assignedEntity);

    verify(practitionerService, never()).createPractitioner(any());
    verify(organizationService, never()).createOrganization(any());
  }

}