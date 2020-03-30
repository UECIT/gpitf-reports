package uk.nhs.gpitf.reports.util;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.experimental.UtilityClass;
import org.nhspathways.webservices.pathways.pathwayscase.PathwaysCaseDocument.PathwaysCase;
import org.nhspathways.webservices.pathways.pathwayscase.PathwaysCaseDocument.PathwaysCase.PathwayDetails;
import org.nhspathways.webservices.pathways.pathwayscase.PathwaysCaseDocument.PathwaysCase.PathwayDetails.PathwayTriageDetails;
import org.nhspathways.webservices.pathways.pathwayscase.PathwaysCaseDocument.PathwaysCase.PathwayDetails.PathwayTriageDetails.PathwayTriage;
import org.nhspathways.webservices.pathways.pathwayscase.PathwaysCaseDocument.PathwaysCase.PathwayDetails.PathwayTriageDetails.PathwayTriage.TriageLineDetails;
import org.nhspathways.webservices.pathways.pathwayscase.PathwaysCaseDocument.PathwaysCase.PathwayDetails.PathwayTriageDetails.PathwayTriage.TriageLineDetails.TriageLine;

@UtilityClass
public class PathwaysUtils {

  public List<TriageLine> getAllTriageLines(PathwaysCase pathwaysCase) {
    return Optional.ofNullable(pathwaysCase)
        .map(PathwaysCase::getPathwayDetails)
        .map(PathwayDetails::getPathwayTriageDetails)
        .map(PathwayTriageDetails::getPathwayTriageArray)
        .stream()
        .flatMap(Stream::of)
        .map(PathwayTriage::getTriageLineDetails)
        .map(TriageLineDetails::getTriageLineArray)
        .flatMap(Stream::of)
        .collect(Collectors.toUnmodifiableList());
  }

}
