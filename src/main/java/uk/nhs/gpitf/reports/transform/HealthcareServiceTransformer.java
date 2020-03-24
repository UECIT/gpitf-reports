package uk.nhs.gpitf.reports.transform;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.hl7.fhir.dstu3.model.ContactPoint;
import org.hl7.fhir.dstu3.model.ContactPoint.ContactPointUse;
import org.hl7.fhir.dstu3.model.HealthcareService;
import org.springframework.stereotype.Component;
import uk.nhs.connect.iucds.cda.ucr.ON;
import uk.nhs.connect.iucds.cda.ucr.POCDMT000002UK01InformationRecipient;
import uk.nhs.connect.iucds.cda.ucr.POCDMT000002UK01IntendedRecipient;
import uk.nhs.connect.iucds.cda.ucr.POCDMT000002UK01Organization;
import uk.nhs.connect.iucds.cda.ucr.TEL;
import uk.nhs.gpitf.reports.service.LocationService;
import uk.nhs.gpitf.reports.service.OrganizationService;
import uk.nhs.gpitf.reports.util.NodeUtil;

@Component
@RequiredArgsConstructor
public class HealthcareServiceTransformer {

  private final LocationService locationService;
  private final OrganizationService organizationService;

  public HealthcareService transformRecipient(
      POCDMT000002UK01InformationRecipient informationRecipient) {

    POCDMT000002UK01IntendedRecipient intendedRecipient =
        informationRecipient.getIntendedRecipient();

    HealthcareService healthcareService = new HealthcareService()
        .setActive(true);

    locationService
        .createFromIntendedRecipient(intendedRecipient)
        .ifPresent(healthcareService::addLocation);

    if (intendedRecipient.sizeOfTelecomArray() > 0) {
      for (TEL tel : intendedRecipient.getTelecomArray()) {
        healthcareService.addTelecom(transformTelecom(tel));
      }
    }

    if (intendedRecipient.isSetReceivedOrganization()) {
      POCDMT000002UK01Organization receivedOrganization =
          intendedRecipient.getReceivedOrganization();

      healthcareService.setProvidedBy(organizationService.createOrganization(receivedOrganization));
      if (receivedOrganization.sizeOfNameArray() > 0) {
        ON name = receivedOrganization.getNameArray(0);
        healthcareService.setName(NodeUtil.getAllText(name.getDomNode()));
      }
    }

    return healthcareService;
  }

  public ContactPoint transformTelecom(TEL tel) {
    return new ContactPoint()
        .setValue(tel.getValue())
        .setUse(mapContactPointUse(tel.getUse()));
  }

  private ContactPointUse mapContactPointUse(List use) {
    // TODO
    return ContactPointUse.WORK;
  }
}
