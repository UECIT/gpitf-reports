package uk.nhs.gpitf.reports.service;

import static uk.nhs.gpitf.reports.util.ReferenceUtil.ofType;

import java.util.Date;
import java.util.UUID;
import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.dstu3.model.Bundle.BundleType;
import org.hl7.fhir.dstu3.model.ContactPoint;
import org.hl7.fhir.dstu3.model.ContactPoint.ContactPointSystem;
import org.hl7.fhir.dstu3.model.ContactPoint.ContactPointUse;
import org.hl7.fhir.dstu3.model.Encounter;
import org.hl7.fhir.dstu3.model.MessageHeader;
import org.hl7.fhir.dstu3.model.MessageHeader.MessageDestinationComponent;
import org.hl7.fhir.dstu3.model.MessageHeader.MessageSourceComponent;
import org.hl7.fhir.dstu3.model.Reference;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.nhs.gpitf.reports.enums.MessageEvent;

@Service
public class FhirMessageService {

  @Value("${gpfit.service}")
  private String thisService;

  public Bundle createMessage(Bundle bundle) {

    MessageHeader messageHeader = new MessageHeader()
        .setEvent(MessageEvent.ITK_GP_CONNECT_SEND.toCoding())
        .addDestination(new MessageDestinationComponent()
          .setName("Test Destination")
          .setEndpoint("test.endpoint"))
        .setTimestamp(new Date())
        .setSource(new MessageSourceComponent()
          .setName("EMS GPFIT Transformation Service")
          .setContact(new ContactPoint()
            .setSystem(ContactPointSystem.EMAIL)
            .setValue("email@address.com")
            .setUse(ContactPointUse.WORK))
          .setEndpoint(thisService));
    messageHeader.setId(UUID.randomUUID().toString());

    bundle.getEntry().stream()
        .map(BundleEntryComponent::getFullUrl)
        .map(Reference::new)
        .filter(ofType(Encounter.class))
        .forEach(messageHeader::addFocus);

    bundle.setType(BundleType.MESSAGE);
    bundle.getEntry().add(0, new BundleEntryComponent()
      .setResource(messageHeader));

    return bundle;
  }

}
