package uk.nhs.gpitf.reports.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.nhs.gpitf.reports.constants.FHIRSystems;

@Getter
@RequiredArgsConstructor
public enum DeviceKind implements Concept {

  APPLICATION_SOFTWARE("706689003", "Application program software");

  private final String system = FHIRSystems.SNOMED;
  private final String value;
  private final String display;

}
