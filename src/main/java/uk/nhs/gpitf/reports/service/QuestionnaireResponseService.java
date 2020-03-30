package uk.nhs.gpitf.reports.service;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.hl7.fhir.dstu3.model.Encounter;
import org.hl7.fhir.dstu3.model.Reference;
import org.nhspathways.webservices.pathways.pathwayscase.PathwaysCaseDocument.PathwaysCase.PathwayDetails.PathwayTriageDetails.PathwayTriage.TriageLineDetails.TriageLine;
import org.nhspathways.webservices.pathways.pathwayscase.PathwaysCaseDocument.PathwaysCase.PathwayDetails.PathwayTriageDetails.PathwayTriage.TriageLineDetails.TriageLine.QuestionType;
import org.springframework.stereotype.Service;
import uk.nhs.gpitf.reports.transform.QuestionnaireResponseTransformer;

@Service
@RequiredArgsConstructor
public class QuestionnaireResponseService {

  private final QuestionnaireResponseTransformer questionnaireResponseTransformer;

  private final FhirStorageService storageService;

  public Optional<Reference> createQuestionnaireResponse(TriageLine triageLine, Encounter encounter) {
    if (triageLine.getQuestionType() != QuestionType.SINGLE_ANSWER) {
      return Optional.empty();
    }

    var questionnaireResponse = questionnaireResponseTransformer.transform(triageLine, encounter);
    return Optional.of(storageService.create(questionnaireResponse));
  }

}
