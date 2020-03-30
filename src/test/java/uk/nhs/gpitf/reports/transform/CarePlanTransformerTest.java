package uk.nhs.gpitf.reports.transform;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import com.google.common.collect.Iterables;
import org.apache.xmlbeans.XmlString;
import org.hl7.fhir.dstu3.model.CarePlan;
import org.hl7.fhir.dstu3.model.CarePlan.CarePlanIntent;
import org.hl7.fhir.dstu3.model.CarePlan.CarePlanStatus;
import org.hl7.fhir.dstu3.model.Reference;
import org.junit.Before;
import org.junit.Test;
import uk.nhs.connect.iucds.cda.ucr.POCDMT000002UK01Section;
import uk.nhs.gpitf.reports.transform.CarePlanTransformer.CarePlanInput;

public class CarePlanTransformerTest {

  private CarePlanTransformer carePlanTransformer;

  private POCDMT000002UK01Section carePlanSection;

  @Before
  public void setup() {
    carePlanTransformer = new CarePlanTransformer();
    carePlanSection = POCDMT000002UK01Section.Factory.newInstance();
  }

  @Test
  public void shouldTransformCarePlan() {
    Reference encounterRef = new Reference("Encounter/123");
    Reference patientRef = new Reference("Patient/321");
    carePlanSection.addNewTitle()
        .set(XmlString.Factory.newValue("Some advice"));
    carePlanSection.addNewText()
        .addNewContent()
        .set(XmlString.Factory.newValue("Stay indoors"));
    CarePlanInput input = new CarePlanInput(carePlanSection, encounterRef, patientRef);

    CarePlan carePlan = carePlanTransformer.transformCarePlan(input);

    assertThat(carePlan.getStatus(), is(CarePlanStatus.COMPLETED));
    assertThat(carePlan.getIntent(), is(CarePlanIntent.PLAN));
    assertThat(carePlan.getTitle(), is("Some advice"));
    assertThat(carePlan.getDescription(), is("Stay indoors"));
    assertThat(carePlan.getContext(), is(encounterRef));
    assertThat(carePlan.getSubject(), is(patientRef));

    String textContent = Iterables.getOnlyElement(carePlan.getText()
        .getDiv()
        .getChildNodes())
        .getContent();
    assertThat(textContent, is("Stay indoors"));
  }

}