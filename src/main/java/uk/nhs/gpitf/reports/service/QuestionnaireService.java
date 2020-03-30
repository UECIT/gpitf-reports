package uk.nhs.gpitf.reports.service;

import lombok.RequiredArgsConstructor;
import org.hl7.fhir.dstu3.model.Reference;
import org.nhspathways.webservices.pathways.pathwayscase.PathwaysCaseDocument.PathwaysCase.PathwayDetails.PathwayTriageDetails.PathwayTriage.TriageLineDetails.TriageLine.Question;
import org.springframework.stereotype.Service;
import uk.nhs.gpitf.reports.model.InputBundle;
import uk.nhs.gpitf.reports.transform.QuestionnaireTransformer;

@Service
@RequiredArgsConstructor
public class QuestionnaireService extends TrackingResourceCreationsService {

  private final QuestionnaireTransformer questionnaireTransformer;

  public Reference createQuestionnaire(Question question, InputBundle inputBundle) {
    return create(questionnaireTransformer.transform(question), inputBundle);
  }

}
