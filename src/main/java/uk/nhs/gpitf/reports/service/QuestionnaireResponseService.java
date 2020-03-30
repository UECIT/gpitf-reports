package uk.nhs.gpitf.reports.service;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.hl7.fhir.dstu3.model.Encounter;
import org.hl7.fhir.dstu3.model.Reference;
import org.nhspathways.webservices.pathways.pathwayscase.PathwaysCaseDocument.PathwaysCase.PathwayDetails.PathwayTriageDetails.PathwayTriage.TriageLineDetails.TriageLine;
import org.nhspathways.webservices.pathways.pathwayscase.PathwaysCaseDocument.PathwaysCase.PathwayDetails.PathwayTriageDetails.PathwayTriage.TriageLineDetails.TriageLine.QuestionType;
import org.springframework.stereotype.Service;
import uk.nhs.gpitf.reports.model.InputBundle;
import uk.nhs.gpitf.reports.transform.QuestionnaireResponseTransformer;

@Service
@RequiredArgsConstructor
public class QuestionnaireResponseService extends TrackingResourceCreationsService {

  private final QuestionnaireResponseTransformer questionnaireResponseTransformer;

  public Optional<Reference> createQuestionnaireResponse(
      TriageLine triageLine, Encounter encounter, InputBundle inputBundle) {
    if (triageLine.getQuestionType() != QuestionType.SINGLE_ANSWER) {
      return Optional.empty();
    }

    var questionnaireResponse =
        questionnaireResponseTransformer.transform(triageLine, encounter, inputBundle);
    return Optional.of(create(questionnaireResponse, inputBundle));
  }

}
