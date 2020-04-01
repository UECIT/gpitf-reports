package uk.nhs.gpitf.reports.service;

import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.hl7.fhir.dstu3.model.Encounter;
import org.hl7.fhir.dstu3.model.Reference;
import org.nhspathways.webservices.pathways.pathwayscase.PathwaysCaseDocument.PathwaysCase.PathwayDetails.PathwayTriageDetails.PathwayTriage.TriageLineDetails.TriageLine.QuestionType;
import org.springframework.stereotype.Service;
import uk.nhs.gpitf.reports.model.InputBundle;
import uk.nhs.gpitf.reports.transform.QuestionnaireResponseTransformer;
import uk.nhs.gpitf.reports.util.PathwaysUtils;

@Service
@RequiredArgsConstructor
public class QuestionnaireResponseService extends TrackingResourceCreationsService {

  private final QuestionnaireResponseTransformer questionnaireResponseTransformer;

  public List<Reference> createQuestionnaireResponse(Encounter encounter, InputBundle inputBundle) {

    var triageLines = PathwaysUtils.getAllTriageLines(inputBundle.getPathwaysCase());

    return triageLines.stream()
        .filter(line -> QuestionType.SINGLE_ANSWER.equals(line.getQuestionType()))
        .map(line -> questionnaireResponseTransformer.transform(line, encounter, inputBundle))
        .map(qr -> create(qr, inputBundle))
        .collect(Collectors.toUnmodifiableList());
  }

}
