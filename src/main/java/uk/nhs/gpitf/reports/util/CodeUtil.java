package uk.nhs.gpitf.reports.util;

import java.util.List;
import java.util.stream.Collectors;
import lombok.experimental.UtilityClass;
import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.Coding;
import uk.nhs.connect.iucds.cda.ucr.CE;
import uk.nhs.gpitf.reports.constants.FHIRSystems;
import uk.nhs.gpitf.reports.constants.IUCDSSystems;

@UtilityClass
public class CodeUtil {

  public CodeableConcept createCodeableConcept(List<Coding> codings) {
    return new CodeableConcept()
        .setCoding(codings)
        .setText(codings.stream()
            .findFirst()
            .map(Coding::getDisplay)
            .orElse(null)
        );
  }

  public CodeableConcept createCodeableConceptFromCE(CE... codings) {
    return createCodeableConceptFromCE(List.of(codings));
  }

  public CodeableConcept createCodeableConceptFromCE(List<? extends CE> codings) {
    return createCodeableConcept(codings.stream()
        .map(CodeUtil::createCoding)
        .collect(Collectors.toUnmodifiableList()));
  }

  public Coding createCoding(CE code) {
    return new Coding(
        mapSystem(code.getCodeSystem()),
        code.getCode(),
        code.getDisplayName()
    );
  }

  public String mapSystem(String codeSystem) {
    switch (codeSystem) {
      case IUCDSSystems.SNOMED:
        return FHIRSystems.SNOMED;
      default:
        return codeSystem;
    }
  }
}
