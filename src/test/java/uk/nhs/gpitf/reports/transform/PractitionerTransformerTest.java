package uk.nhs.gpitf.reports.transform;

import org.hl7.fhir.dstu3.model.Practitioner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import uk.nhs.connect.iucds.cda.ucr.PN;
import uk.nhs.connect.iucds.cda.ucr.POCDMT000002UK01Person;
import uk.nhs.gpitf.reports.Stub;

@RunWith(MockitoJUnitRunner.class)
public class PractitionerTransformerTest {

  @Mock
  private HumanNameTransformer humanNameTransformer;

  @InjectMocks
  private PractitionerTransformer practitionerTransformer;

  private POCDMT000002UK01Person person;

  @Before
  public void setup() {
    person = POCDMT000002UK01Person.Factory.newInstance();
  }

  @Test
  public void testTransform() {
    var name = Stub.fullPersonName();
    person.setNameArray(new PN[]{name});

    Practitioner practitioner = practitionerTransformer.transform(person, null, null);

    Mockito.verify(humanNameTransformer).transform(Mockito.any(PN.class));
  }

}