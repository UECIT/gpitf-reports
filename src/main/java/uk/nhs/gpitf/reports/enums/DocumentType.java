package uk.nhs.gpitf.reports.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.nhs.gpitf.reports.constants.FHIRSystems;

@Getter
@RequiredArgsConstructor
public enum  DocumentType implements Concept {

  INVESTIGATION_RESULT("24641000000107", "Investigation result"),
  OUTPATIENT_MEDICAL_NOTE("820491000000108", "Outpatient medical note"),
  REPORT("229059009 ", "Report");

  private final String system = FHIRSystems.SNOMED;
  private final String value;
  private final String display;

}
