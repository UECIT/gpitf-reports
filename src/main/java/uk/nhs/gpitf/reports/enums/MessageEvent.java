package uk.nhs.gpitf.reports.enums;

import java.util.stream.Stream;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MessageEvent implements Concept {

  COMMUNICATION_REQUEST("communication-request");

  private final String system = "http://hl7.org/fhir/STU3/codesystem-message-events.html";
  private final String value;
  private final String display = getValue();

  public static MessageEvent fromCode(String code) {
    return Stream.of(values())
        .filter(me -> code.toUpperCase().equals(me.value))
        .findFirst()
        .orElseThrow();
  }
}
