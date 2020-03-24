package uk.nhs.gpitf.reports.controller;

import lombok.RequiredArgsConstructor;
import org.apache.xmlbeans.XmlException;
import org.hl7.fhir.dstu3.model.Reference;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import uk.nhs.gpitf.reports.model.InputForm;
import uk.nhs.gpitf.reports.service.EncounterReportService;

@Controller
@RequiredArgsConstructor
public class ViewController {

  private final EncounterReportService encounterReportService;

  @GetMapping
  public String getForm(Model model) {
    model.addAttribute("inputForm", new InputForm());
    return "input";
  }

  @PostMapping
  public String postXML(@ModelAttribute("inputForm") InputForm input) {
    try {
      Reference encounterRef = encounterReportService
          .createEncounterReport(input.getReportXml());
      input.setEncounterId(encounterRef.getReference());
    } catch (XmlException e) {
      input.setErrorMessage(e.getMessage());
    }
    return "encounter";
  }

}
