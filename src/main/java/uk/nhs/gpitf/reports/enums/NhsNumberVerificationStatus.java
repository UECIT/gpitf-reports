package uk.nhs.gpitf.reports.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.nhs.gpitf.reports.constants.FHIRSystems;

@Getter
@RequiredArgsConstructor
public enum NhsNumberVerificationStatus implements Concept {
  VERIFIED("01", "Number present and verified"),
  UNVERIFIED("02", "Number present but not traced");

  private final String system = FHIRSystems.NHS_NUMBER_VERIFICATION_STATUS;
  private final String value;
  private final String display;
}
