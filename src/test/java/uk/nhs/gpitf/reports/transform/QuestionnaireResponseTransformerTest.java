package uk.nhs.gpitf.reports.transform;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.hamcrest.MockitoHamcrest.argThat;
import static uk.nhs.gpitf.reports.Matchers.isReferenceWithDisplay;
import static uk.nhs.gpitf.reports.Matchers.isStringType;

import java.util.Calendar;
import org.hl7.fhir.dstu3.model.Encounter;
import org.hl7.fhir.dstu3.model.Narrative;
import org.hl7.fhir.dstu3.model.QuestionnaireResponse.QuestionnaireResponseStatus;
import org.hl7.fhir.dstu3.model.Reference;
import org.hl7.fhir.utilities.xhtml.XhtmlNode;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.nhspathways.webservices.pathways.pathwayscase.PathwaysCaseDocument.PathwaysCase.PathwayDetails.PathwayTriageDetails.PathwayTriage.TriageLineDetails.TriageLine;
import org.nhspathways.webservices.pathways.pathwayscase.PathwaysCaseDocument.PathwaysCase.PathwayDetails.PathwayTriageDetails.PathwayTriage.TriageLineDetails.TriageLine.Question;
import uk.nhs.gpitf.reports.model.InputBundle;
import uk.nhs.gpitf.reports.service.NarrativeService;
import uk.nhs.gpitf.reports.service.QuestionnaireService;

@RunWith(MockitoJUnitRunner.class)
public class QuestionnaireResponseTransformerTest {

  @Mock
  private QuestionnaireService questionnaireService;

  @Mock
  private NarrativeService narrativeService;

  @InjectMocks
  private QuestionnaireResponseTransformer responseTransformer;

  private static final String QUESTIONNAIRE_REFERENCE = "QuestionnaireReference";

  @Before
  public void setup() {
    var questionnaireRef = new Reference().setDisplay(QUESTIONNAIRE_REFERENCE);
    when(questionnaireService.createQuestionnaire(any(Question.class), any(InputBundle.class)))
        .thenReturn(questionnaireRef);
  }

  @Test
  public void transformFull() {
    var triageLine = TriageLine.Factory.newInstance();
    var date = Calendar.getInstance();
    triageLine.setFinish(date);

    var question = triageLine.addNewQuestion();
    question.setQuestionId("qId");
    question.setQuestionText("Are you a [test] runner?");

    var answers = question.addNewAnswers();

    var answer1 = answers.addNewAnswer();
    answer1.setText("Yes");
    answer1.setSelected(true);

    var answer2 = answers.addNewAnswer();
    answer2.setText("No");
    answer2.setSelected(false);

    var encounter = new Encounter();
    final var SUBJECT_REFERENCE = "SubjectReference";
    encounter.setSubject(new Reference().setDisplay(SUBJECT_REFERENCE));

    var narrative = new Narrative()
        .setDiv(new XhtmlNode().setValue("Question - Are you a [test] runner?"));
    when(narrativeService.buildNarrative(argThat(containsString("Are you a [test] runner?"))))
        .thenReturn(narrative);


    var response = responseTransformer.transform(triageLine, encounter, new InputBundle());

    assertThat(response.getStatus(), is(QuestionnaireResponseStatus.COMPLETED));
    assertThat(response.getQuestionnaire(), isReferenceWithDisplay(QUESTIONNAIRE_REFERENCE));
    assertThat(response.getContext().getResource(), sameInstance(encounter));
    assertThat(response.getSubject(), isReferenceWithDisplay(SUBJECT_REFERENCE));
    assertThat(response.getSource(), isReferenceWithDisplay(SUBJECT_REFERENCE));
    assertThat(response.getAuthored(), is(date.getTime()));
    assertThat(response.getItem(), hasSize(1));
    assertThat(response.getText(), is(narrative));

    var item = response.getItemFirstRep();
    assertThat(item.getLinkId(), is("q"));
    assertThat(item.getText(), is("Are you a [test] runner?"));
    assertThat(item.getSubject(), isReferenceWithDisplay(SUBJECT_REFERENCE));
    assertThat(item.getAnswer(), hasSize(1));

    var answer = item.getAnswerFirstRep();
    assertThat(answer.getValue(), isStringType("Yes"));
  }

}
