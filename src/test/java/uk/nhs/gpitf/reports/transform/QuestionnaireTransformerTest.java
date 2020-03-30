package uk.nhs.gpitf.reports.transform;

import static org.junit.Assert.assertThat;
import static org.hamcrest.Matchers.*;

import org.hamcrest.Matcher;
import org.hl7.fhir.dstu3.model.Enumerations.PublicationStatus;
import org.hl7.fhir.dstu3.model.Questionnaire.QuestionnaireItemOptionComponent;
import org.junit.Before;
import org.junit.Test;
import org.nhspathways.webservices.pathways.pathwayscase.PathwaysCaseDocument.PathwaysCase.PathwayDetails.PathwayTriageDetails.PathwayTriage.TriageLineDetails.TriageLine.Question;

public class QuestionnaireTransformerTest {

  private QuestionnaireTransformer questionnaireTransformer;

  @Before
  public void setup() {
    questionnaireTransformer = new QuestionnaireTransformer();
  }

  @Test
  public void transformFull() {
    var question = Question.Factory.newInstance();
    question.setQuestionId("qId");
    question.setQuestionText("Are you a [test] runner?");

    var answers = question.addNewAnswers();

    var answer1 = answers.addNewAnswer();
    answer1.setText("Yes");
    answer1.setSelected(true);

    var answer2 = answers.addNewAnswer();
    answer2.setText("No");
    answer2.setSelected(false);

    var questionnaire = questionnaireTransformer.transform(question);

    assertThat(questionnaire.getStatus(), is(PublicationStatus.ACTIVE));
    assertThat(questionnaire.getUrl(), containsString("qId"));
    assertThat(questionnaire.getName(), is("qId"));
    assertThat(questionnaire.getTitle(), is("Are you a [test] runner?"));
    assertThat(questionnaire.getItem(), hasSize(1));

    var item = questionnaire.getItemFirstRep();
    assertThat(item.getLinkId(), is("q"));
    assertThat(item.getText(), is("Are you a [test] runner?"));
    //noinspection unchecked
    assertThat(item.getOption(), contains(isOption("Yes"), isOption("No")));
  }

  private Matcher<QuestionnaireItemOptionComponent> isOption(String text) {
    return hasProperty("value", hasProperty("value", is(text)));
  }
}
