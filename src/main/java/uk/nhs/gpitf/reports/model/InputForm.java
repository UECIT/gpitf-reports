package uk.nhs.gpitf.reports.model;

import lombok.Data;

@Data
public class InputForm {

  private String reportXml;
  private String encounterId;
  private String errorMessage;

}
