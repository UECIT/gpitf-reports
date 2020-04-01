package uk.nhs.gpitf.reports.transform;

import static uk.nhs.gpitf.reports.util.ReferenceUtil.ofTypes;

import java.util.Calendar;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.hl7.fhir.dstu3.model.CarePlan;
import org.hl7.fhir.dstu3.model.Composition;
import org.hl7.fhir.dstu3.model.Composition.CompositionStatus;
import org.hl7.fhir.dstu3.model.Composition.DocumentConfidentiality;
import org.hl7.fhir.dstu3.model.Composition.SectionComponent;
import org.hl7.fhir.dstu3.model.Composition.SectionMode;
import org.hl7.fhir.dstu3.model.DomainResource;
import org.hl7.fhir.dstu3.model.Encounter;
import org.hl7.fhir.dstu3.model.Narrative;
import org.hl7.fhir.dstu3.model.Observation;
import org.hl7.fhir.dstu3.model.Questionnaire;
import org.hl7.fhir.dstu3.model.QuestionnaireResponse;
import org.hl7.fhir.dstu3.model.Reference;
import org.hl7.fhir.dstu3.model.ReferralRequest;
import org.hl7.fhir.dstu3.model.Resource;
import org.springframework.stereotype.Component;
import uk.nhs.gpitf.reports.enums.DocumentSectionCode;
import uk.nhs.gpitf.reports.enums.DocumentType;
import uk.nhs.gpitf.reports.enums.ListOrder;
import uk.nhs.gpitf.reports.model.InputBundle;
import uk.nhs.gpitf.reports.service.NarrativeService;
import uk.nhs.gpitf.reports.util.PathwaysUtils;

@Component
@RequiredArgsConstructor
public class CompositionTransformer {

  private final NarrativeService narrativeService;

  public Composition transform(InputBundle inputBundle, Reference device, Encounter encounter) {
    List<DomainResource> resources = inputBundle.getResourcesCreated();
    Optional<Calendar> date = PathwaysUtils.getLastTriageLineFinishDate(inputBundle.getPathwaysCase());
    Composition composition = new Composition()
        .setTitle("111 Report")
        .setStatus(CompositionStatus.FINAL)
        .setDate(date.map(Calendar::getTime).orElse(null))
        .setType(DocumentType.OUTPATIENT_MEDICAL_NOTE.toCodeableConcept())
        .setEncounter(new Reference(encounter.getIdElement()))
        .setSubject(encounter.getSubject())
        .addAuthor(device)
        .setConfidentiality(DocumentConfidentiality.N);

    composition.setText(narrativeService.buildNarrative("Created from transformed 111"));

    // Observations/Questionnaires/QuestionnaireResponses
    composition.addSection(buildEvaluateSection(resources));

    // ReferralRequests/CarePlans
    composition.addSection(buildResultsSection(resources));

    return composition;
  }

  private SectionComponent buildEvaluateSection(List<DomainResource> resources) {
    SectionComponent sectionComponent = new SectionComponent()
        .setTitle("Observations, Questionnaires and QuestionnaireResponses")
        .setCode(DocumentSectionCode.OBSERVATIONS.toCodeableConcept())
        .setMode(SectionMode.CHANGES)
        .setOrderedBy(ListOrder.ENTRY_DATE.toCodeableConcept());

    List<DomainResource> evaluateResources = resources.stream()
        .filter(ofTypes(Observation.class, Questionnaire.class, QuestionnaireResponse.class))
        .collect(Collectors.toUnmodifiableList());

    evaluateResources.stream()
        .map(Resource::getIdElement)
        .map(Reference::new)
        .forEach(sectionComponent::addEntry);

    sectionComponent.setText(buildSectionNarrative(evaluateResources));
    return sectionComponent;
  }

  private SectionComponent buildResultsSection(List<DomainResource> resources) {
    SectionComponent sectionComponent = new SectionComponent()
        .setTitle("ReferralRequests, CarePlans")
        .setCode(DocumentSectionCode.PLAN_AND_REQUESTED_ACTIONS.toCodeableConcept())
        .setMode(SectionMode.CHANGES)
        .setOrderedBy(ListOrder.ENTRY_DATE.toCodeableConcept());

    List<DomainResource> resultResources = resources.stream()
        .filter(ofTypes(ReferralRequest.class, CarePlan.class))
        .collect(Collectors.toUnmodifiableList());

    resultResources.stream()
        .map(Resource::getIdElement)
        .map(Reference::new)
        .forEach(sectionComponent::addEntry);

    sectionComponent.setText(buildSectionNarrative(resultResources));
    return sectionComponent;
  }

  private Narrative buildSectionNarrative(List<DomainResource> resources) {
    List<Narrative> narratives = resources.stream()
        .map(DomainResource::getText)
        .filter(Objects::nonNull)
        .collect(Collectors.toUnmodifiableList());
    return narrativeService.buildCombinedNarrative(narratives);
  }

}
