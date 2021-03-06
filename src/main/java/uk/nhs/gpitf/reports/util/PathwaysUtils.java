package uk.nhs.gpitf.reports.util;

import java.util.Calendar;
import java.util.List;
import java.util.ListIterator;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.experimental.UtilityClass;
import org.nhspathways.webservices.pathways.pathwayscase.PathwaysCaseDocument.PathwaysCase;
import org.nhspathways.webservices.pathways.pathwayscase.PathwaysCaseDocument.PathwaysCase.Outcome;
import org.nhspathways.webservices.pathways.pathwayscase.PathwaysCaseDocument.PathwaysCase.PathwayDetails;
import org.nhspathways.webservices.pathways.pathwayscase.PathwaysCaseDocument.PathwaysCase.PathwayDetails.PathwayTriageDetails;
import org.nhspathways.webservices.pathways.pathwayscase.PathwaysCaseDocument.PathwaysCase.PathwayDetails.PathwayTriageDetails.PathwayTriage;
import org.nhspathways.webservices.pathways.pathwayscase.PathwaysCaseDocument.PathwaysCase.PathwayDetails.PathwayTriageDetails.PathwayTriage.TriageLineDetails;
import org.nhspathways.webservices.pathways.pathwayscase.PathwaysCaseDocument.PathwaysCase.PathwayDetails.PathwayTriageDetails.PathwayTriage.TriageLineDetails.TriageLine;

@UtilityClass
public class PathwaysUtils {

  public Optional<Outcome> getOutcome(PathwaysCase pathwaysCase) {
    if (pathwaysCase == null) {
      return Optional.empty();
    }
    return Optional.ofNullable(pathwaysCase.getOutcome());
  }

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

  public Optional<Calendar> getLastTriageLineFinishDate(PathwaysCase pathwaysCase) {
    List<TriageLine> triageLines = PathwaysUtils.getAllTriageLines(pathwaysCase);

    ListIterator<TriageLine> iterator = triageLines.listIterator(triageLines.size());

    while (iterator.hasPrevious()) {
      TriageLine previous = iterator.previous();
      if (previous.getFinish() != null) {
        return Optional.of(previous.getFinish());
      }
    }
    return Optional.empty();
  }

}
