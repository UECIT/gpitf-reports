package uk.nhs.gpitf.reports;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.both;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;

import lombok.NoArgsConstructor;
import org.hamcrest.Matcher;
import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.Identifier;
import org.hl7.fhir.dstu3.model.NHSNumberIdentifier;
import org.hl7.fhir.dstu3.model.Reference;
import org.hl7.fhir.dstu3.model.StringType;
import org.hl7.fhir.dstu3.model.Type;
import uk.nhs.gpitf.reports.constants.FHIRSystems;
import uk.nhs.gpitf.reports.enums.Concept;

@NoArgsConstructor
public class Matchers {

  public static Matcher<Identifier> isNhsNumber(String value) {
    return org.hamcrest.Matchers.<Identifier>
        both(instanceOf(NHSNumberIdentifier.class))
        .and(hasProperty("value", equalTo(value)))
        .and(hasProperty("system", is(FHIRSystems.NHS_NUMBER)));
  }

  public static Matcher<Object> isReferenceWithDisplay(String display) {
    return both(instanceOf(Reference.class))
        .and(hasProperty("display", equalTo(display)));
  }

  public static Matcher<Type> isConcept(Concept concept) {
    return org.hamcrest.Matchers.<Type>
        both(instanceOf(CodeableConcept.class))
        .and(hasProperty("text", equalTo(concept.getDisplay())))
        .and(hasProperty("coding", contains(allOf(
            hasProperty("system", equalTo(concept.getSystem())),
            hasProperty("code", equalTo(concept.getValue())),
            hasProperty("display", equalTo(concept.getDisplay()))
        ))));
  }

  public static Matcher<Type> isStringType(String text) {
    return org.hamcrest.Matchers.<Type>
        both(instanceOf(StringType.class))
        .and(hasProperty("value", is(text)));
  }
}
