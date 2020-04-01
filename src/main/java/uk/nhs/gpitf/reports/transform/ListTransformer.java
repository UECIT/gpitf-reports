package uk.nhs.gpitf.reports.transform;

import java.util.Calendar;
import java.util.Optional;
import org.hl7.fhir.dstu3.model.Encounter;
import org.hl7.fhir.dstu3.model.ListResource;
import org.hl7.fhir.dstu3.model.ListResource.ListEntryComponent;
import org.hl7.fhir.dstu3.model.ListResource.ListStatus;
import org.hl7.fhir.dstu3.model.Reference;
import org.hl7.fhir.dstu3.model.Resource;
import org.springframework.stereotype.Component;
import uk.nhs.gpitf.reports.enums.ListOrder;
import uk.nhs.gpitf.reports.model.InputBundle;
import uk.nhs.gpitf.reports.util.PathwaysUtils;

@Component
public class ListTransformer {

  public ListResource transform(InputBundle inputBundle, Reference device, Encounter encounter) {

    Optional<Calendar> date = PathwaysUtils.getLastTriageLineFinishDate(inputBundle.getPathwaysCase());
    ListResource list = new ListResource()
        .setDate(date.map(Calendar::getTime).orElse(null))
        .setOrderedBy(ListOrder.ENTRY_DATE.toCodeableConcept()) //No way to sort by event date accurately?
        .setStatus(ListStatus.CURRENT)
        .setSource(device)
        .setEncounter(new Reference(encounter.getIdElement()))
        .setSubject(encounter.getSubject());

    inputBundle.getResourcesCreated().stream()
        .map(Resource::getIdElement)
        .map(Reference::new)
        .map(ListEntryComponent::new)
        .forEach(list::addEntry);

    return list;
  }

}
