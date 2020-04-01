package uk.nhs.gpitf.reports;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import lombok.NoArgsConstructor;
import org.hamcrest.CustomTypeSafeMatcher;
import org.hamcrest.Matcher;
import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.DomainResource;
import org.hl7.fhir.dstu3.model.Identifier;
import org.hl7.fhir.dstu3.model.NHSNumberIdentifier;
import org.hl7.fhir.dstu3.model.Reference;
import org.hl7.fhir.dstu3.model.Resource;
import org.hl7.fhir.dstu3.model.StringType;
import org.hl7.fhir.dstu3.model.Type;
import org.springframework.util.ObjectUtils;
import uk.nhs.gpitf.reports.constants.FHIRSystems;
import uk.nhs.gpitf.reports.enums.Concept;

@NoArgsConstructor
public class Matchers {

  public static Matcher<Identifier> isNhsNumber(String value) {
    return new FunctionMatcher<>(identifier ->
        identifier instanceof NHSNumberIdentifier
            && identifier.getValue().equals(value)
            && identifier.getSystem().equals(FHIRSystems.NHS_NUMBER),
        "NHS Number " + value
    );
  }

  public static Matcher<Object> isReferenceWithDisplay(String display) {
    return new FunctionMatcher<>(obj ->
        obj instanceof Reference
            && ((Reference) obj).getDisplay().equals(display),
        "Reference with " + display);
  }

  public static Matcher<Reference> isReferenceTo(DomainResource resource) {
    return new FunctionMatcher<>(ref -> resource.getId().equals(ref.getReference()),
        "Reference to " + resource.getId());
  }


  public static Matcher<List<Reference>> containsRefsTo(DomainResource... resources) {
    List<DomainResource> resourceList = Arrays.asList(resources);
    String ids = resourceList.stream()
        .map(Resource::getId)
        .collect(Collectors.joining(" and "));

    return new FunctionMatcher<>(refList -> {
      if (ObjectUtils.isEmpty(resourceList) || resourceList.size() != refList.size()) {
        return false;
      }

      return IntStream.range(0, resourceList.size())
          .allMatch(i -> isReferenceTo(resourceList.get(i)).matches(refList.get(i)));

    }, "References to " + ids);
  }

  public static Matcher<Type> isConcept(Concept concept) {
    return new FunctionMatcher<>(type -> {
      if (!(type instanceof CodeableConcept)) {
        return false;
      }
      CodeableConcept cc = (CodeableConcept) type;
      return cc.getText().equals(concept.getDisplay())
          && cc.getCoding().stream()
          .anyMatch(coding ->
              coding.getDisplay().equals(concept.getDisplay())
                  && coding.getSystem().equals(concept.getSystem())
                  && coding.getCode().equals(concept.getValue()));
    }, concept.getDisplay());
  }

  public static Matcher<Type> isStringType(String text) {
    return new FunctionMatcher<>(type ->
        type instanceof StringType && ((StringType) type).getValue().equals(text), text);
  }

  static class FunctionMatcher<T> extends CustomTypeSafeMatcher<T> {

    private final Function<T, Boolean> matcher;

    public FunctionMatcher(Function<T, Boolean> matcher, String desc) {
      super(desc);
      this.matcher = matcher;
    }

    @Override
    protected boolean matchesSafely(T t) {
      return matcher.apply(t);
    }
  }

}
