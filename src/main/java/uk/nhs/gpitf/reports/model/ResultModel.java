package uk.nhs.gpitf.reports.model;

import lombok.Data;

@Data
public class ResultModel {

  private String encounterId;
  private String bundleXml;
  private String errorMessage;
}
