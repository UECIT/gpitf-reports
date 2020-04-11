package uk.nhs.gpitf.reports.transform;

import java.util.ArrayList;
import java.util.List;
import org.hl7.fhir.dstu3.model.Dosage;
import org.hl7.fhir.dstu3.model.Encounter;
import org.hl7.fhir.dstu3.model.MedicationStatement;
import org.hl7.fhir.dstu3.model.MedicationStatement.MedicationStatementStatus;
import org.hl7.fhir.dstu3.model.MedicationStatement.MedicationStatementTaken;
import org.hl7.fhir.dstu3.model.Reference;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;
import uk.nhs.connect.iucds.cda.ucr.POCDMT000002UK01ClinicalDocument1;
import uk.nhs.connect.iucds.cda.ucr.POCDMT000002UK01Section;
import uk.nhs.connect.iucds.cda.ucr.POCDMT000002UK01StructuredBody;
import uk.nhs.connect.iucds.cda.ucr.StrucDocContent;
import uk.nhs.gpitf.reports.constants.IUCDSSystems;
import uk.nhs.gpitf.reports.util.StructuredBodyUtil;

@Component
@RequiredArgsConstructor
public class MedicationTransformer {

  public MedicationStatement transform(POCDMT000002UK01ClinicalDocument1 clinicalDocument, Encounter encounter) {
    POCDMT000002UK01StructuredBody structuredBody = StructuredBodyUtil
        .getStructuredBody(clinicalDocument);
    
    List<POCDMT000002UK01Section> medicationSection = StructuredBodyUtil
        .getSectionsOfType(structuredBody, IUCDSSystems.SNOMED, "933361000000108");
    
    List<Dosage> dosages = new ArrayList<Dosage>();
    for (POCDMT000002UK01Section pocdmt000002uk01Section : medicationSection) {
      StrucDocContent[] contentArray = pocdmt000002uk01Section.getText().getContentArray();
      dosages.add(new Dosage().setText(contentArray.toString()));
    }
    
    MedicationStatement medication = null;
    if (medicationSection.size() > 0) {
      medication = new MedicationStatement()
          .setSubject(encounter.getSubject())
          .setStatus(MedicationStatementStatus.ACTIVE)
          .setTaken(MedicationStatementTaken.UNK)
          .setDosage(dosages)
          .setContext(new Reference(encounter.getIdElement()));
    }
    return medication;
  }
}
