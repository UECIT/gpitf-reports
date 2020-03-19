package uk.nhs.gpitf.reports.service;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.Coding;
import org.springframework.stereotype.Service;
import uk.nhs.connect.iucds.cda.ucr.CV;
import uk.nhs.connect.iucds.cda.ucr.POCDMT000002UK01ClinicalDocument1;
import uk.nhs.connect.iucds.cda.ucr.POCDMT000002UK01Component2;
import uk.nhs.connect.iucds.cda.ucr.POCDMT000002UK01Component3;
import uk.nhs.connect.iucds.cda.ucr.POCDMT000002UK01Entry;
import uk.nhs.connect.iucds.cda.ucr.POCDMT000002UK01Observation;
import uk.nhs.gpitf.reports.constants.FHIRSystems;
import uk.nhs.gpitf.reports.constants.IUCDSSystems;

@Service
public class ReasonService {

  public List<CodeableConcept> createReason(POCDMT000002UK01ClinicalDocument1 clinicalDocument) {

    POCDMT000002UK01Component2 component = clinicalDocument.getComponent();
    if (component == null || !component.isSetStructuredBody()) {
      return Collections.emptyList();
    }

    return getReasonComponents(component.getStructuredBody().getComponentArray());
  }

  private List<CodeableConcept> getReasonComponents(POCDMT000002UK01Component3[] componentArray) {
    POCDMT000002UK01Entry[] entryArray = componentArray[0]
        .getSection()
        .getEntryArray();

    return Arrays.stream(entryArray)
        .filter(POCDMT000002UK01Entry::isSetObservation)
        .map(POCDMT000002UK01Entry::getObservation)
        .filter(isReasonObs())
        .map(obs -> (CV)obs.getValueArray(0))
        .map(this::createCodeableConcept)
        .collect(Collectors.toUnmodifiableList());
  }

  private CodeableConcept createCodeableConcept(CV cv) {
      CodeableConcept codeableConcept = new CodeableConcept();
      codeableConcept.addCoding(
          new Coding(
              FHIRSystems.SNOMED,
              cv.getCode(),
              cv.getDisplayName()
          )
      );
      return codeableConcept;
  }

  private Predicate<POCDMT000002UK01Observation> isReasonObs() {
    return obs -> Arrays.stream(obs.getTemplateIdArray())
        .anyMatch(id -> id.getExtension().equals(IUCDSSystems.CLINICAL_DISCRIMINATOR));
  }

}
