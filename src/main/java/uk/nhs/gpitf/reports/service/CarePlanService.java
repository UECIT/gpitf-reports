package uk.nhs.gpitf.reports.service;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ArrayUtils;
import org.hl7.fhir.dstu3.model.Encounter;
import org.hl7.fhir.dstu3.model.Reference;
import org.springframework.stereotype.Service;
import uk.nhs.connect.iucds.cda.ucr.CE;
import uk.nhs.connect.iucds.cda.ucr.POCDMT000002UK01Component3;
import uk.nhs.connect.iucds.cda.ucr.POCDMT000002UK01Component5;
import uk.nhs.connect.iucds.cda.ucr.POCDMT000002UK01Section;
import uk.nhs.connect.iucds.cda.ucr.POCDMT000002UK01StructuredBody;
import uk.nhs.gpitf.reports.constants.IUCDSSystems;
import uk.nhs.gpitf.reports.constants.SnomedCodes;
import uk.nhs.gpitf.reports.model.InputBundle;
import uk.nhs.gpitf.reports.transform.CarePlanTransformer;
import uk.nhs.gpitf.reports.util.PathwaysUtils;
import uk.nhs.gpitf.reports.util.StructuredBodyUtil;

@Service
@RequiredArgsConstructor
public class CarePlanService extends TrackingResourceCreationsService {

  private final CarePlanTransformer carePlanTransformer;

  public List<Reference> createCarePlans(InputBundle inputBundle, Encounter encounter) {

    POCDMT000002UK01StructuredBody structuredBody =
        StructuredBodyUtil.getStructuredBody(inputBundle.getClinicalDocument());

    var triageLines = PathwaysUtils.getAllTriageLines(inputBundle.getPathwaysCase());

    return Arrays.stream(structuredBody.getComponentArray())
        .map(POCDMT000002UK01Component3::getSection)
        .map(this::findCarePlanSections)
        .flatMap(List::stream)
        .map(section ->
            carePlanTransformer.transformCarePlan(section, encounter, triageLines, inputBundle))
        .map(cp -> create(cp, inputBundle))
        .collect(Collectors.toUnmodifiableList());
  }

  private List<POCDMT000002UK01Section> findCarePlanSections(POCDMT000002UK01Section section) {

    // Base case: If there are no nested sub components
    if (ArrayUtils.isEmpty(section.getComponentArray())) {
      return Collections.emptyList();
    }

    // Find care plans at this level.
    List<POCDMT000002UK01Section> subSections = Arrays.stream(section.getComponentArray())
        .map(POCDMT000002UK01Component5::getSection)
        .collect(Collectors.toUnmodifiableList());

    List<POCDMT000002UK01Section> carePlanSections = subSections.stream()
        .filter(this::isCareAdvice)
        .collect(Collectors.toList());

    // Recursively find care plans in nested subsections.
    subSections.stream()
        .map(this::findCarePlanSections)
        .forEach(carePlanSections::addAll);

    return carePlanSections;
  }

  private boolean isCareAdvice(POCDMT000002UK01Section section) {
    CE code = section.getCode();

    return code != null
        && IUCDSSystems.SNOMED.equals(code.getCodeSystem())
        && SnomedCodes.INFORMATION_ADVICE_GIVEN.equals(code.getCode());
  }

}
