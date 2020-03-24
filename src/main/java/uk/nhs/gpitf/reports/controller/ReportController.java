package uk.nhs.gpitf.reports.controller;

import lombok.RequiredArgsConstructor;
import org.apache.xmlbeans.XmlException;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import uk.nhs.gpitf.reports.service.EncounterReportService;

@RestController
@RequiredArgsConstructor
public class ReportController {

  private final EncounterReportService encounterReportService;

  @PostMapping(consumes = {MediaType.APPLICATION_XML_VALUE}, path = "create")
  public ResponseEntity<String> createEncounterReport(@RequestBody String xmlReportString) {

    try {
      return ResponseEntity.ok(
          encounterReportService.createEncounterReport(xmlReportString).getReference());
    } catch (XmlException e) {
      return ResponseEntity.badRequest().body(e.getMessage());
    }

  }

}
