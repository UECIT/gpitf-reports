package uk.nhs.gpitf.reports.transform;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import org.hl7.fhir.dstu3.model.HumanName;
import org.hl7.fhir.dstu3.model.Practitioner;
import org.junit.Before;
import org.junit.Test;
import uk.nhs.connect.iucds.cda.ucr.PN;
import uk.nhs.connect.iucds.cda.ucr.POCDMT000002UK01Person;
import uk.nhs.gpitf.reports.Stub;

public class PractitionerTransformerTest {

  private PractitionerTransformer practitionerTransformer;

  private POCDMT000002UK01Person person;

  @Before
  public void setup() {
    practitionerTransformer = new PractitionerTransformer();
    person = POCDMT000002UK01Person.Factory.newInstance();
  }

  @Test
  public void testTransform() {
    person.setNameArray(new PN[]{Stub.personName()});

    Practitioner practitioner = practitionerTransformer.transform(person);

    HumanName name = practitioner.getNameFirstRep();
    assertThat(name.getNameAsSingleString(), is("Homer Simpson"));
  }

}