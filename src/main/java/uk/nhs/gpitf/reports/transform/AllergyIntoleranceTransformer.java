package uk.nhs.gpitf.reports.transform;

import java.util.List;
import org.hl7.fhir.dstu3.model.AllergyIntolerance;
import org.hl7.fhir.dstu3.model.AllergyIntolerance.AllergyIntoleranceCategory;
import org.hl7.fhir.dstu3.model.AllergyIntolerance.AllergyIntoleranceClinicalStatus;
import org.hl7.fhir.dstu3.model.AllergyIntolerance.AllergyIntoleranceCriticality;
import org.hl7.fhir.dstu3.model.AllergyIntolerance.AllergyIntoleranceReactionComponent;
import org.hl7.fhir.dstu3.model.AllergyIntolerance.AllergyIntoleranceSeverity;
import org.hl7.fhir.dstu3.model.AllergyIntolerance.AllergyIntoleranceVerificationStatus;
import org.hl7.fhir.dstu3.model.Annotation;
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
public class AllergyIntoleranceTransformer {

  public AllergyIntolerance transform(POCDMT000002UK01ClinicalDocument1 clinicalDocument, Encounter encounter) {
    POCDMT000002UK01StructuredBody structuredBody = StructuredBodyUtil
        .getStructuredBody(clinicalDocument);
    
    List<POCDMT000002UK01Section> allergyIntoleranceSection = StructuredBodyUtil
        .getSectionsOfType(structuredBody, IUCDSSystems.SNOMED, "886921000000105");
    
    AllergyIntoleranceReactionComponent allergyIntoleranceReactionComponent = new AllergyIntolerance.AllergyIntoleranceReactionComponent();
    allergyIntoleranceReactionComponent.setDescription(AllergyIntolerance.SP_CRITICALITY);
    allergyIntoleranceReactionComponent.setSeverity(AllergyIntoleranceSeverity.MODERATE);
    AllergyIntolerance allergyIntolerance = null;
    if (allergyIntoleranceSection.size() > 0) {
      allergyIntolerance = new AllergyIntolerance()
          .setPatient(encounter.getSubject())
          .setClinicalStatus(AllergyIntoleranceClinicalStatus.ACTIVE)
          .addCategory(AllergyIntoleranceCategory.MEDICATION)
          .setVerificationStatus(AllergyIntoleranceVerificationStatus.UNCONFIRMED)
          .setCriticality(AllergyIntoleranceCriticality.UNABLETOASSESS)
          .addNote(new Annotation().setText(allergyIntoleranceSection.get(0).getText().getContentArray().toString()))
          .addReaction(allergyIntoleranceReactionComponent);
    }
    return allergyIntolerance;
  }
}
