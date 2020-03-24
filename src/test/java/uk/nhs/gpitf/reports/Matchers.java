package uk.nhs.gpitf.reports;

import static org.hamcrest.Matchers.*;

import lombok.experimental.UtilityClass;
import org.hamcrest.Matcher;
import org.hl7.fhir.dstu3.model.Identifier;
import org.hl7.fhir.dstu3.model.NHSNumberIdentifier;
import org.hl7.fhir.dstu3.model.Reference;
import org.hl7.fhir.dstu3.model.Type;
import uk.nhs.gpitf.reports.constants.FHIRSystems;
import uk.nhs.gpitf.reports.enums.Concept;

@UtilityClass
public class Matchers {

  public Matcher<Identifier> isNhsNumber(String value) {
    return org.hamcrest.Matchers.<Identifier>
        both(instanceOf(NHSNumberIdentifier.class))
        .and(hasProperty("value", equalTo(value)))
        .and(hasProperty("system", is(FHIRSystems.NHS_NUMBER)));
  }

  public Matcher<Object> isDisplayReference(String display) {
    return both(instanceOf(Reference.class))
        .and(hasProperty("display", equalTo(display)));
  }

  public Matcher<Type> isConcept(Concept concept) {
    return org.hamcrest.Matchers.<Type>
        both(hasProperty("text", equalTo(concept.getDisplay())))
        .and(hasProperty("coding", contains(allOf(
            hasProperty("system", equalTo(concept.getSystem())),
            hasProperty("code", equalTo(concept.getValue())),
            hasProperty("display", equalTo(concept.getDisplay()))
        ))));
  }
}
