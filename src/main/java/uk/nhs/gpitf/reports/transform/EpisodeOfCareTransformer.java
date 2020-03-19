package uk.nhs.gpitf.reports.transform;

import lombok.RequiredArgsConstructor;
import org.hl7.fhir.dstu3.model.EpisodeOfCare;
import org.springframework.stereotype.Component;
import uk.nhs.connect.iucds.cda.ucr.POCDMT000002UK01AssignedEntity;
import uk.nhs.connect.iucds.cda.ucr.POCDMT000002UK01Organization;
import uk.nhs.connect.iucds.cda.ucr.POCDMT000002UK01Person;
import uk.nhs.gpitf.reports.service.PractitionerService;
import uk.nhs.gpitf.reports.service.OrganizationService;

@Component
@RequiredArgsConstructor
public class EpisodeOfCareTransformer {

  private final PractitionerService practitionerService;
  private final OrganizationService organizationService;

  public EpisodeOfCare transformEpisodeOfCare(
      POCDMT000002UK01AssignedEntity assignedEntity) {

    EpisodeOfCare episodeOfCare = new EpisodeOfCare();
    if (assignedEntity.isSetAssignedPerson()) {
      POCDMT000002UK01Person assignedPerson = assignedEntity.getAssignedPerson();
      episodeOfCare.setCareManager(practitionerService.createPractitioner(assignedPerson));
    }

    if (assignedEntity.isSetRepresentedOrganization()) {
      POCDMT000002UK01Organization representedOrganization = assignedEntity
          .getRepresentedOrganization();

      episodeOfCare.setManagingOrganization(
          organizationService.createOrganization(representedOrganization));
    }

    return episodeOfCare;
  }

}
