package uk.nhs.gpitf.reports.enums;

import java.util.stream.Stream;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.nhs.gpitf.reports.constants.FHIRSystems;

@Getter
@RequiredArgsConstructor
public enum MessageEvent implements Concept {

  COMMUNICATION_REQUEST(FHIRSystems.MESSAGE_EVENT, "communication-request", "communication-request"),
  ITK_GP_CONNECT_SEND(FHIRSystems.ITK_MESSAGE_EVENT, "ITK007C", "ITK GP Connect Send Document");

  private final String system;
  private final String value;
  private final String display;

  public static MessageEvent fromCode(String code) {
    return Stream.of(values())
        .filter(me -> code.toUpperCase().equals(me.value))
        .findFirst()
        .orElseThrow();
  }
}
