package uk.nhs.gpitf.reports.transform;

import java.util.List;
import org.hl7.fhir.dstu3.model.DiagnosticReport;
import org.hl7.fhir.dstu3.model.DiagnosticReport.DiagnosticReportStatus;
import org.hl7.fhir.dstu3.model.Encounter;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;
import uk.nhs.connect.iucds.cda.ucr.POCDMT000002UK01ClinicalDocument1;
import uk.nhs.connect.iucds.cda.ucr.POCDMT000002UK01Section;
import uk.nhs.connect.iucds.cda.ucr.POCDMT000002UK01StructuredBody;
import uk.nhs.gpitf.reports.constants.IUCDSSystems;
import uk.nhs.gpitf.reports.util.StructuredBodyUtil;

@Component
@RequiredArgsConstructor
public class TriageReportTransformer {
  
  public DiagnosticReport transform(POCDMT000002UK01ClinicalDocument1 clinicalDocument, Encounter encounter) {
    
    POCDMT000002UK01StructuredBody structuredBody = StructuredBodyUtil
        .getStructuredBody(clinicalDocument);
    
    List<POCDMT000002UK01Section> triageReportSection = StructuredBodyUtil
        .getSectionsOfType(structuredBody, IUCDSSystems.SNOMED, "4281000179108");
    DiagnosticReport diagnosticReport = null;
    if (triageReportSection.size() > 0) {
      diagnosticReport = new DiagnosticReport()
          .setSubject(encounter.getSubject())
          .setStatus(DiagnosticReportStatus.UNKNOWN)
          .setConclusion(triageReportSection.get(0).getText().getContentArray().toString());
    }
    return diagnosticReport;
  }
}
