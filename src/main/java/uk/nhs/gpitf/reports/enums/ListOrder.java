package uk.nhs.gpitf.reports.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ListOrder implements Concept {
  EVENT_DATE("event-date", "Sorted by Event Date"),
  ENTRY_DATE("entry-date", "Sorted by Item Date");

  private final String system = "http://hl7.org/fhir/list-order";
  private final String value;
  private final String display;
}
