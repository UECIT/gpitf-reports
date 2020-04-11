package uk.nhs.gpitf.reports.controller;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.apache.xmlbeans.XmlException;
import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Reference;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import uk.nhs.gpitf.reports.model.InputForm;
import uk.nhs.gpitf.reports.model.ResultModel;
import uk.nhs.gpitf.reports.service.EncounterReportService;

@Controller
@RequiredArgsConstructor
public class ViewController {

  private final EncounterReportService encounterReportService;
  private final FhirContext fhirContext;

  @GetMapping
  public String getForm(Model model) {
    model.addAttribute("inputForm", new InputForm());
    return "input";
  }

  @PostMapping
  public String postXML(Model model, @ModelAttribute("inputForm") InputForm input) {
    ResultModel result = new ResultModel();
    model.addAttribute("result", result);

    try {
      Reference encounterRef = encounterReportService
          .createEncounterReport(input.getReportXml());
      fetchEncounterReport(encounterRef, result);
    } catch (XmlException e) {
      result.setErrorMessage(e.getMessage());
    }
    return "encounter";
  }

  @GetMapping(path = "view")
  public String viewReport(Model model, @RequestParam String encounterId) {
    ResultModel result = new ResultModel();
    model.addAttribute("result", result);
    Reference encounterRef = new Reference(encounterId);
    fetchEncounterReport(encounterRef, result);
    return "encounter";
  }

  private void fetchEncounterReport(Reference encounterRef, ResultModel result) {
    try {
      Bundle report = encounterReportService.getEncounterReport(encounterRef);
      result.setEncounterId(encounterRef.getReference());
      result.setBundleXml(fhirContext.newXmlParser()
          .setPrettyPrint(true)
          .encodeResourceToString(report));
    } catch (ResourceNotFoundException e) {
      result.setErrorMessage("Unable to fetch encounter report: " + e.getMessage());
    }
  }

}
