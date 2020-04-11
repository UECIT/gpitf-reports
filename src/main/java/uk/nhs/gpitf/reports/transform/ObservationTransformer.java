package uk.nhs.gpitf.reports.transform;

import java.util.Date;
import java.util.List;
import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.Coding;
import org.hl7.fhir.dstu3.model.Encounter;
import org.hl7.fhir.dstu3.model.Observation;
import org.hl7.fhir.dstu3.model.Observation.ObservationStatus;
import org.hl7.fhir.dstu3.model.Reference;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;
import uk.nhs.connect.iucds.cda.ucr.POCDMT000002UK01ClinicalDocument1;
import uk.nhs.connect.iucds.cda.ucr.POCDMT000002UK01Section;
import uk.nhs.connect.iucds.cda.ucr.POCDMT000002UK01StructuredBody;
import uk.nhs.gpitf.reports.constants.IUCDSSystems;
import uk.nhs.gpitf.reports.util.StructuredBodyUtil;

@Component
@RequiredArgsConstructor
public class ObservationTransformer {

  public Observation transform(POCDMT000002UK01ClinicalDocument1 clinicalDocument, Reference transformerDevice,
      Encounter encounter) {
    POCDMT000002UK01StructuredBody structuredBody = StructuredBodyUtil
        .getStructuredBody(clinicalDocument);
    
    List<POCDMT000002UK01Section> observationSections = StructuredBodyUtil
        .getSectionsOfType(structuredBody, IUCDSSystems.SNOMED, "886891000000102");
    Observation observation = null;
    if (observationSections.size() > 0) {
      observation = new Observation()
          .setSubject(transformerDevice).setDevice(transformerDevice)
          .setCode(new CodeableConcept(new Coding().setCode("33962009")))
          .setStatus(ObservationStatus.PRELIMINARY).setIssued(new Date())
          .setComment(observationSections.get(0).getText().getContentArray().toString())
          .setContext(new Reference(encounter.getIdElement()));
    }
    
    return observation;
  }
}
