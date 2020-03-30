package uk.nhs.gpitf.reports.transform;

import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.hl7.fhir.dstu3.model.Encounter;
import org.hl7.fhir.dstu3.model.QuestionnaireResponse;
import org.hl7.fhir.dstu3.model.QuestionnaireResponse.QuestionnaireResponseItemAnswerComponent;
import org.hl7.fhir.dstu3.model.QuestionnaireResponse.QuestionnaireResponseStatus;
import org.hl7.fhir.dstu3.model.Reference;
import org.hl7.fhir.dstu3.model.StringType;
import org.nhspathways.webservices.pathways.pathwayscase.PathwaysCaseDocument.PathwaysCase.PathwayDetails.PathwayTriageDetails.PathwayTriage.TriageLineDetails.TriageLine;
import org.nhspathways.webservices.pathways.pathwayscase.PathwaysCaseDocument.PathwaysCase.PathwayDetails.PathwayTriageDetails.PathwayTriage.TriageLineDetails.TriageLine.Question.Answers.Answer;
import org.springframework.stereotype.Component;
import uk.nhs.gpitf.reports.model.InputBundle;
import uk.nhs.gpitf.reports.service.QuestionnaireService;

@Component
@RequiredArgsConstructor
public class QuestionnaireResponseTransformer {

  private final QuestionnaireService questionnaireService;

  public QuestionnaireResponse transform(TriageLine triageLine, Encounter encounter, InputBundle inputBundle) {
    var question = triageLine.getQuestion();

    var response = new QuestionnaireResponse();
    response.setStatus(QuestionnaireResponseStatus.COMPLETED);
    response.setQuestionnaire(questionnaireService.createQuestionnaire(question, inputBundle));
    response.setContext(new Reference(encounter));
    response.setSubject(encounter.getSubject());
    response.setSource(encounter.getSubject());
    response.setAuthored(triageLine.getFinish().getTime());

    var item = response.addItem();
    item.setLinkId("q");
    item.setText(question.getQuestionText());
    item.setSubject(encounter.getSubject());

    Stream.of(question.getAnswers().getAnswerArray())
        .filter(Answer::getSelected)
        .findFirst()
        .map(Answer::getText)
        .map(StringType::new)
        .map(new QuestionnaireResponseItemAnswerComponent()::setValue)
        .ifPresent(item::addAnswer);

    return response;
  }

}
