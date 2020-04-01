package uk.nhs.gpitf.reports.transform;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static uk.nhs.gpitf.reports.Matchers.containsRefsTo;
import static uk.nhs.gpitf.reports.Matchers.isConcept;
import static uk.nhs.gpitf.reports.Matchers.isReferenceTo;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.Composition;
import org.hl7.fhir.dstu3.model.Composition.CompositionStatus;
import org.hl7.fhir.dstu3.model.Composition.DocumentConfidentiality;
import org.hl7.fhir.dstu3.model.Composition.SectionComponent;
import org.hl7.fhir.dstu3.model.DomainResource;
import org.hl7.fhir.dstu3.model.Encounter;
import org.hl7.fhir.dstu3.model.Narrative;
import org.hl7.fhir.dstu3.model.Reference;
import org.hl7.fhir.utilities.xhtml.XhtmlNode;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.nhspathways.webservices.pathways.pathwayscase.PathwaysCaseDocument.PathwaysCase;
import org.nhspathways.webservices.pathways.pathwayscase.PathwaysCaseDocument.PathwaysCase.Outcome;
import uk.nhs.gpitf.reports.FhirStub;
import uk.nhs.gpitf.reports.enums.DocumentSectionCode;
import uk.nhs.gpitf.reports.enums.DocumentType;
import uk.nhs.gpitf.reports.model.InputBundle;
import uk.nhs.gpitf.reports.service.NarrativeService;

@RunWith(MockitoJUnitRunner.class)
public class CompositionTransformerTest {

  @InjectMocks
  private CompositionTransformer compositionTransformer;

  @Mock
  private NarrativeService narrativeService;

  private PathwaysCase pathwaysCase;

  @Before
  public void setup() {
    pathwaysCase = PathwaysCase.Factory.newInstance();
  }

  @Test
  public void transformComposition() {
    InputBundle inputBundle = new InputBundle();
    Outcome outcome = pathwaysCase.addNewOutcome();
    outcome.setTitle("Some referral");
    outcome.setCode("DXCODE");
    inputBundle.setPathwaysCase(pathwaysCase);
    List<DomainResource> resourceList = Arrays.asList(
        FhirStub.observation(),
        FhirStub.questionnaire(),
        FhirStub.questionnaireResponse(),
        FhirStub.referralRequest(),
        FhirStub.carePlan()
    );
    inputBundle.setResourcesCreated(resourceList);

    Reference device = new Reference("Device/123");
    Reference patient = new Reference("Patient/444");
    Encounter encounter = new Encounter().setSubject(patient);
    encounter.setId("Encounter/777");

    Narrative narrative = new Narrative();
    narrative.setDiv(new XhtmlNode().setValue("DXCODE - Some Referral"));

    Composition composition = compositionTransformer.transform(inputBundle, device, encounter);

    assertThat(composition.getStatus(), is(CompositionStatus.FINAL));
    assertThat(composition.getType(), isConcept(DocumentType.OUTPATIENT_MEDICAL_NOTE));
    assertThat(composition.getConfidentiality(), is(DocumentConfidentiality.N));
    assertThat(composition.getEncounter(), isReferenceTo(encounter));
    assertThat(composition.getSubject(), is(patient));
    assertThat(composition.getSection(), hasSize(2));

    List<CodeableConcept> sectionCodes = composition.getSection().stream()
        .map(SectionComponent::getCode)
        .collect(Collectors.toUnmodifiableList());

    assertThat(sectionCodes, contains(
        isConcept(DocumentSectionCode.OBSERVATIONS),
        isConcept(DocumentSectionCode.PLAN_AND_REQUESTED_ACTIONS)
    ));

    SectionComponent evaluateSection = composition.getSection().get(0);
    SectionComponent resultSection = composition.getSection().get(1);

    assertThat(evaluateSection.getEntry(),
        containsRefsTo(FhirStub.observation(), FhirStub.questionnaire(), FhirStub.questionnaireResponse()));
    assertThat(resultSection.getEntry(),
        containsRefsTo(FhirStub.referralRequest(), FhirStub.carePlan()));
    verify(narrativeService, Mockito.times(2))
        .buildCombinedNarrative(anyList());
  }


}