package uk.nhs.gpitf.reports.transform;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.mockito.Mockito.verify;
import static uk.nhs.gpitf.reports.Matchers.isReferenceWithDisplay;

import com.google.common.collect.Iterables;
import java.util.ArrayList;
import org.apache.xmlbeans.XmlString;
import org.hl7.fhir.dstu3.model.CarePlan.CarePlanIntent;
import org.hl7.fhir.dstu3.model.CarePlan.CarePlanStatus;
import org.hl7.fhir.dstu3.model.Encounter;
import org.hl7.fhir.dstu3.model.Reference;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.nhspathways.webservices.pathways.pathwayscase.PathwaysCaseDocument.PathwaysCase.PathwayDetails.PathwayTriageDetails.PathwayTriage.TriageLineDetails.TriageLine;
import uk.nhs.connect.iucds.cda.ucr.POCDMT000002UK01Section;
import uk.nhs.gpitf.reports.model.InputBundle;
import uk.nhs.gpitf.reports.service.QuestionnaireResponseService;

@RunWith(MockitoJUnitRunner.class)
public class CarePlanTransformerTest {

  @Mock
  private QuestionnaireResponseService questionnaireResponseService;

  @InjectMocks
  private CarePlanTransformer carePlanTransformer;

  private POCDMT000002UK01Section carePlanSection;

  @Before
  public void setup() {
    carePlanSection = POCDMT000002UK01Section.Factory.newInstance();
  }

  @Test
  public void shouldTransformCarePlan() {
    var encounter = new Encounter();
    encounter.setSubject(new Reference().setDisplay("SubjectReference"));
    carePlanSection.addNewTitle()
        .set(XmlString.Factory.newValue("Some advice"));
    carePlanSection.addNewText()
        .addNewContent()
        .set(XmlString.Factory.newValue("Stay indoors"));

    InputBundle inputBundle = new InputBundle();

    var triageLines = new ArrayList<TriageLine>(2);
    triageLines.add(TriageLine.Factory.newInstance());
    triageLines.add(TriageLine.Factory.newInstance());

    var carePlan =
        carePlanTransformer.transformCarePlan(carePlanSection, encounter, triageLines, inputBundle);

    assertThat(carePlan.getStatus(), is(CarePlanStatus.COMPLETED));
    assertThat(carePlan.getIntent(), is(CarePlanIntent.PLAN));
    assertThat(carePlan.getTitle(), is("Some advice"));
    assertThat(carePlan.getDescription(), is("Stay indoors"));
    assertThat(carePlan.getContext().getResource(), sameInstance(encounter));
    assertThat(carePlan.getSubject(), isReferenceWithDisplay("SubjectReference"));

    String textContent = Iterables.getOnlyElement(carePlan.getText()
        .getDiv()
        .getChildNodes())
        .getContent();
    assertThat(textContent, is("Stay indoors"));

    verify(questionnaireResponseService)
        .createQuestionnaireResponse(triageLines.get(0), encounter, inputBundle);
    verify(questionnaireResponseService)
        .createQuestionnaireResponse(triageLines.get(1), encounter, inputBundle);
  }

}