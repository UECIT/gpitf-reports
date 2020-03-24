package uk.nhs.gpitf.reports.enums;

import java.util.stream.Stream;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MaritalStatus implements Concept {
  ANNULLED("A", "Annulled"),
  DIVORCED("D", "Divorced"),
  INTERLOCUTORY("I", "Interlocutory"),
  LEGALLY("L", "Legally separated"),
  MARRIED("M", "Married"),
  POLYGAMOUS("P", "Polygamous"),
  NEVER_MARRIED("S", "Never married"),
  DOMESTIC_PARTNER("T", "Domestic partner"),
  UNMARRIED("U", "Unmarried"),
  WIDOWED("W", "Widowed");

  private final String system = "http://hl7.org/fhir/v3/MaritalStatus";
  private final String value;
  private final String display;

  public static MaritalStatus fromCode(String code) {
    return Stream.of(values())
        .filter(am -> code.toUpperCase().equals(am.value))
        .findFirst()
        .orElseThrow();
  }
}
