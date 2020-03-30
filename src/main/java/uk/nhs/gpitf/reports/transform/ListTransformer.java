package uk.nhs.gpitf.reports.transform;

import java.util.Calendar;
import java.util.List;
import java.util.ListIterator;
import java.util.Optional;
import org.hl7.fhir.dstu3.model.Encounter;
import org.hl7.fhir.dstu3.model.ListResource;
import org.hl7.fhir.dstu3.model.ListResource.ListEntryComponent;
import org.hl7.fhir.dstu3.model.ListResource.ListStatus;
import org.hl7.fhir.dstu3.model.Reference;
import org.nhspathways.webservices.pathways.pathwayscase.PathwaysCaseDocument.PathwaysCase;
import org.nhspathways.webservices.pathways.pathwayscase.PathwaysCaseDocument.PathwaysCase.PathwayDetails.PathwayTriageDetails.PathwayTriage.TriageLineDetails.TriageLine;
import org.springframework.stereotype.Component;
import uk.nhs.gpitf.reports.enums.ListOrder;
import uk.nhs.gpitf.reports.model.InputBundle;
import uk.nhs.gpitf.reports.util.PathwaysUtils;

@Component
public class ListTransformer {

  public ListResource transform(InputBundle inputBundle, Reference device, Encounter encounter) {

    Optional<Calendar> date = getLastTriageLineFinishDate(inputBundle.getPathwaysCase());
    ListResource list = new ListResource()
        .setDate(date.map(Calendar::getTime).orElse(null))
        .setOrderedBy(ListOrder.ENTRY_DATE.toCodeableConcept()) //No way to sort by event date accurately?
        .setStatus(ListStatus.CURRENT)
        .setSource(device)
        .setEncounter(new Reference(encounter.getIdElement()))
        .setSubject(encounter.getSubject());

    inputBundle.getResourcesCreated().stream()
        .map(ListEntryComponent::new)
        .forEach(list::addEntry);

    return list;
  }

  private Optional<Calendar> getLastTriageLineFinishDate(PathwaysCase pathwaysCase) {
    List<TriageLine> triageLines = PathwaysUtils.getAllTriageLines(pathwaysCase);

    ListIterator<TriageLine> iterator = triageLines.listIterator(triageLines.size());

    while (iterator.hasPrevious()) {
      TriageLine previous = iterator.previous();
      if (previous.getFinish() != null) {
        return Optional.of(previous.getFinish());
      }
    }
    return Optional.empty();
  }

}
