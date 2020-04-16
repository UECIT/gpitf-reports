package uk.nhs.gpitf.reports.transform;

import static uk.nhs.gpitf.reports.util.ReferenceUtil.ofTypes;
import java.util.ArrayList;
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
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
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
import uk.nhs.connect.iucds.cda.ucr.POCDMT000002UK01ClinicalDocument1;
import uk.nhs.connect.iucds.cda.ucr.POCDMT000002UK01Component3;
import uk.nhs.connect.iucds.cda.ucr.POCDMT000002UK01Component5;
import uk.nhs.connect.iucds.cda.ucr.POCDMT000002UK01Section;
import uk.nhs.connect.iucds.cda.ucr.POCDMT000002UK01StructuredBody;
import uk.nhs.gpitf.reports.enums.DocumentSectionCode;
import uk.nhs.gpitf.reports.enums.DocumentType;
import uk.nhs.gpitf.reports.enums.ListOrder;
import uk.nhs.gpitf.reports.model.InputBundle;
import uk.nhs.gpitf.reports.service.NarrativeService;
import uk.nhs.gpitf.reports.util.PathwaysUtils;
import uk.nhs.gpitf.reports.util.StructuredBodyUtil;

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

    // Observation
    List<SectionComponent> buildComponentSection = buildComponentSection(inputBundle);
    buildComponentSection.forEach(composition::addSection);
    //composition.addSection();
    
    return composition;
  }

  private List<SectionComponent> buildComponentSection(InputBundle inputBundle) {
    List<SectionComponent> components =  new ArrayList<Composition.SectionComponent>();
    POCDMT000002UK01ClinicalDocument1 clinicalDocument = inputBundle.getClinicalDocument();
    POCDMT000002UK01StructuredBody structuredBody = StructuredBodyUtil
        .getStructuredBody(clinicalDocument);
    if (structuredBody != null) {
      POCDMT000002UK01Component3[] componentArray = structuredBody.getComponentArray();
      POCDMT000002UK01Section section = componentArray[0].getSection();
      POCDMT000002UK01Component5[] componentArray2 = section.getComponentArray();
      for (POCDMT000002UK01Component5 pocdmt000002uk01Component5 : componentArray2) {
        POCDMT000002UK01Section section2 = pocdmt000002uk01Component5.getSection();
        SectionComponent sectionComponent = new SectionComponent()
                .setTitle(getTitle(section2.getTitle().xmlText()))
                .setCode(DocumentSectionCode.OBSERVATIONS.toCodeableConcept())
                .setText(getNarrative(section2.getText().xmlText()))
                .setMode(SectionMode.CHANGES)
                .setOrderedBy(ListOrder.ENTRY_DATE.toCodeableConcept());
        components.add(sectionComponent);
      } 
    }
    return components;
  }

  private Narrative getNarrative(String xmlText) {
    Document text= Jsoup.parse(xmlText, "", Parser.xmlParser());
    Narrative narrative = new Narrative();
    narrative.setDivAsString(text.select("table").html());
    if (text.select("table").isEmpty()) {
      narrative.setDivAsString(text.select("content").text());
    }
    if (text.select("table").isEmpty() && text.select("content").isEmpty()) {
      narrative.setDivAsString(text.text());
    }
    return narrative;
  }

  private String getTitle(String xmlText) {
    Document tt = Jsoup.parse(xmlText, "", Parser.xmlParser());
    return tt.select("xml-fragment").text();
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
