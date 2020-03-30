package uk.nhs.gpitf.reports.transform;

import java.util.stream.Stream;
import org.hl7.fhir.dstu3.model.Enumerations.PublicationStatus;
import org.hl7.fhir.dstu3.model.Questionnaire;
import org.hl7.fhir.dstu3.model.Questionnaire.QuestionnaireItemOptionComponent;
import org.hl7.fhir.dstu3.model.Questionnaire.QuestionnaireItemType;
import org.hl7.fhir.dstu3.model.StringType;
import org.nhspathways.webservices.pathways.pathwayscase.PathwaysCaseDocument.PathwaysCase.PathwayDetails.PathwayTriageDetails.PathwayTriage.TriageLineDetails.TriageLine.Question;
import org.nhspathways.webservices.pathways.pathwayscase.PathwaysCaseDocument.PathwaysCase.PathwayDetails.PathwayTriageDetails.PathwayTriage.TriageLineDetails.TriageLine.Question.Answers.Answer;
import org.springframework.stereotype.Component;

@Component
public class QuestionnaireTransformer {

  public Questionnaire transform(Question question) {
    var questionnaire = new Questionnaire();
    questionnaire.setStatus(PublicationStatus.ACTIVE);
    questionnaire.setExperimental(false);
    questionnaire.addSubjectType("Patient");
    questionnaire.setUrl(question.getQuestionId());
    questionnaire.setName(question.getQuestionId());
    questionnaire.setTitle(question.getQuestionText());
    var item = questionnaire.addItem();
    item.setLinkId("q");
    item.setText(question.getQuestionText());
    item.setType(QuestionnaireItemType.CHOICE);
    item.setRequired(true);
    item.setRepeats(false);

    Stream.of(question.getAnswers().getAnswerArray())
        .map(Answer::getText)
        .map(StringType::new)
        .map(QuestionnaireItemOptionComponent::new)
        .forEach(item::addOption);

    return questionnaire;
  }

}
