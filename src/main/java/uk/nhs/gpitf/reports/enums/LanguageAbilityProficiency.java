package uk.nhs.gpitf.reports.enums;

import java.util.stream.Stream;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum LanguageAbilityProficiency implements Concept {
  EXCELLENT("E", "Excellent"),
  FAIR("F", "Fair"),
  GOOD("G", "Good"),
  POOR("P", "Poor");

  private final String system = "https://fhir.hl7.org.uk/STU3/CodeSystem/CareConnect-LanguageAbilityProficiency-1";
  private final String value;
  private final String display;
  
  public static LanguageAbilityProficiency fromCode(String code) {
    return Stream.of(values())
        .filter(am -> code.toUpperCase().equals(am.value))
        .findFirst()
        .orElseThrow();
  }
}
