package uk.nhs.gpitf.reports.service;

import org.hl7.fhir.dstu3.model.DiagnosticReport;
import org.hl7.fhir.dstu3.model.Encounter;
import org.hl7.fhir.dstu3.model.Reference;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import uk.nhs.gpitf.reports.model.InputBundle;
import uk.nhs.gpitf.reports.transform.TriageReportTransformer;

@Service
@RequiredArgsConstructor
public class TriageReportService {

  private final FhirStorageService fhirStorageService;
  private final TriageReportTransformer triageReportTransformer;

  public Reference createTriageReport(InputBundle inputBundle, Encounter encounter) {
    DiagnosticReport diagnosticReport = triageReportTransformer.transform(inputBundle.getClinicalDocument(), encounter);
    if (diagnosticReport != null) {
      inputBundle.addResource(diagnosticReport);
      return create(diagnosticReport);
    }
    return null;
  }
  
  public Reference create(DiagnosticReport diagnosticReport) {
    return fhirStorageService.create(diagnosticReport);
  }
}
