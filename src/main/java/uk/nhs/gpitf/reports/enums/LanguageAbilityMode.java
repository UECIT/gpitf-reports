package uk.nhs.gpitf.reports.enums;

import java.util.stream.Stream;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum LanguageAbilityMode implements Concept {
  EXPRESSED_SIGNED("ESGN", "Expressed signed"),
  EXPRESSED_SPOKEN("ESP", "Expressed spoken"),
  EXPRESSED_WRITTEN("EWR", "Expressed written"),
  RECEIVED_SIGNED("RSGN", "Received signed"),
  RECEIVED_SPOKEN("RSP", "Received spoken"),
  RECEIVED_WRITTEN("RWR", "Received written");

  private final String system = "https://fhir.hl7.org.uk/STU3/CodeSystem/CareConnect-LanguageAbilityMode-1";
  private final String value;
  private final String display;

  public static LanguageAbilityMode fromCode(String code) {
    return Stream.of(values())
        .filter(am -> code.toUpperCase().equals(am.value))
        .findFirst()
        .orElseThrow();
  }
}
