package uk.nhs.gpitf.reports.transform;

import lombok.RequiredArgsConstructor;
import org.hl7.fhir.dstu3.model.EpisodeOfCare;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
import org.springframework.stereotype.Component;
import uk.nhs.connect.iucds.cda.ucr.POCDMT000002UK01AssignedEntity;
import uk.nhs.connect.iucds.cda.ucr.POCDMT000002UK01Organization;
import uk.nhs.connect.iucds.cda.ucr.POCDMT000002UK01Person;
import uk.nhs.gpitf.reports.service.PractitionerService;
import uk.nhs.gpitf.reports.model.InputBundle;
import uk.nhs.gpitf.reports.service.OrganizationService;

@Component
@RequiredArgsConstructor
public class EpisodeOfCareTransformer {

  private final PractitionerService practitionerService;
  private final OrganizationService organizationService;

  public EpisodeOfCare transformEpisodeOfCare(
      InputBundle inputBundle, POCDMT000002UK01AssignedEntity assignedEntity) {

    EpisodeOfCare episodeOfCare = new EpisodeOfCare();
    if (assignedEntity.isSetAssignedPerson()) {
      POCDMT000002UK01Person assignedPerson = assignedEntity.getAssignedPerson();
      String displayName = assignedEntity.getCode() != null ? assignedEntity.getCode().getDisplayName() : null;
      String repOrg = assignedEntity.isSetRepresentedOrganization() ? assignedEntity.getRepresentedOrganization().xmlText() : "";
      Document doc = Jsoup.parse(repOrg, "", Parser.xmlParser());
      episodeOfCare.setCareManager(practitionerService.createPractitioner(inputBundle, assignedPerson, displayName, doc.select("name").text()));
    }

    if (assignedEntity.isSetRepresentedOrganization()) {
      POCDMT000002UK01Organization representedOrganization = assignedEntity
          .getRepresentedOrganization();

      episodeOfCare.setManagingOrganization(
          organizationService.createOrganization(inputBundle, representedOrganization));
    }

    return episodeOfCare;
  }

}
