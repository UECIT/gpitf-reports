package uk.nhs.gpitf.reports.transform;

import static org.exparity.hamcrest.date.DateMatchers.sameInstant;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.core.Is.is;
import static uk.nhs.gpitf.reports.Matchers.isConcept;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.stream.Collectors;
import org.hl7.fhir.dstu3.model.CarePlan;
import org.hl7.fhir.dstu3.model.DomainResource;
import org.hl7.fhir.dstu3.model.Encounter;
import org.hl7.fhir.dstu3.model.ListResource;
import org.hl7.fhir.dstu3.model.ListResource.ListEntryComponent;
import org.hl7.fhir.dstu3.model.ListResource.ListStatus;
import org.hl7.fhir.dstu3.model.Observation;
import org.hl7.fhir.dstu3.model.Questionnaire;
import org.hl7.fhir.dstu3.model.QuestionnaireResponse;
import org.hl7.fhir.dstu3.model.Reference;
import org.hl7.fhir.dstu3.model.ReferralRequest;
import org.junit.Before;
import org.junit.Test;
import org.nhspathways.webservices.pathways.pathwayscase.PathwaysCaseDocument.PathwaysCase;
import org.nhspathways.webservices.pathways.pathwayscase.PathwaysCaseDocument.PathwaysCase.PathwayDetails.PathwayTriageDetails.PathwayTriage.TriageLineDetails.TriageLine;
import uk.nhs.gpitf.reports.enums.ListOrder;
import uk.nhs.gpitf.reports.model.InputBundle;

public class ListTransformerTest {

  private ListTransformer listTransformer;

  private PathwaysCase pathwaysCase;

  @Before
  public void setup() {
    listTransformer = new ListTransformer();
    pathwaysCase = PathwaysCase.Factory.newInstance();
  }

  @Test
  public void createList() {
    InputBundle inputBundle = new InputBundle();
    List<DomainResource> resources = Arrays.asList(
        (DomainResource)new CarePlan().setId("CarePlan/123"),
        (DomainResource)new ReferralRequest().setId("ReferralRequest/421"),
        (DomainResource)new Questionnaire().setId("Questionnaire/33"),
        (DomainResource)new QuestionnaireResponse().setId("QuestionnaireResponse/4444"),
        (DomainResource)new Observation().setId("Observation/33322")
    );

    inputBundle.setResourcesCreated(resources);

    TriageLine triageLine = TriageLine.Factory.newInstance();
    GregorianCalendar calendar = GregorianCalendar
        .from(ZonedDateTime.of(2011, 3, 3, 3, 3, 3, 3, ZoneId.systemDefault()));
    triageLine.setFinish(calendar);
    pathwaysCase.addNewPathwayDetails()
        .addNewPathwayTriageDetails()
        .addNewPathwayTriage()
        .addNewTriageLineDetails()
        .addNewTriageLine()
        .set(triageLine);
    inputBundle.setPathwaysCase(pathwaysCase);

    Reference device = new Reference("Device/332");
    Encounter encounter = new Encounter();
    Reference patientRef = new Reference("Patient/999");
    encounter.setSubject(patientRef);
    encounter.setId("Encounter/445");

    ListResource list = listTransformer.transform(inputBundle, device, encounter);

    assertThat(list.getEncounter().getReference(), is(encounter.getId()));
    assertThat(list.getSubject(), is(patientRef));
    assertThat(list.getSource(), is(device));
    assertThat(list.getOrderedBy(), isConcept(ListOrder.ENTRY_DATE));
    assertThat(list.getStatus(), is(ListStatus.CURRENT));
    assertThat(list.getDate(), sameInstant(calendar.toInstant()));

    List<String> entryRefs = list.getEntry().stream()
        .map(ListEntryComponent::getItem)
        .map(Reference::getReference)
        .collect(Collectors.toUnmodifiableList());

    assertThat(entryRefs,
        contains("CarePlan/123", "ReferralRequest/421", "Questionnaire/33", "QuestionnaireResponse/4444", "Observation/33322"));
  }

}