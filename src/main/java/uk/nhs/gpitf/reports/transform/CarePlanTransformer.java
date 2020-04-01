package uk.nhs.gpitf.reports.transform;

import static uk.nhs.gpitf.reports.util.ReferenceUtil.ofTypes;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.hl7.fhir.dstu3.model.CarePlan;
import org.hl7.fhir.dstu3.model.CarePlan.CarePlanIntent;
import org.hl7.fhir.dstu3.model.CarePlan.CarePlanStatus;
import org.hl7.fhir.dstu3.model.Encounter;
import org.hl7.fhir.dstu3.model.Narrative;
import org.hl7.fhir.dstu3.model.Observation;
import org.hl7.fhir.dstu3.model.QuestionnaireResponse;
import org.hl7.fhir.dstu3.model.Reference;
import org.hl7.fhir.dstu3.model.Resource;
import org.nhspathways.webservices.pathways.pathwayscase.PathwaysCaseDocument.PathwaysCase.PathwayDetails.PathwayTriageDetails.PathwayTriage.TriageLineDetails.TriageLine;
import org.springframework.stereotype.Component;
import uk.nhs.connect.iucds.cda.ucr.POCDMT000002UK01Section;
import uk.nhs.gpitf.reports.model.InputBundle;
import uk.nhs.gpitf.reports.service.QuestionnaireResponseService;
import uk.nhs.gpitf.reports.util.NodeUtil;

@Component
@RequiredArgsConstructor
public class CarePlanTransformer {

  private final QuestionnaireResponseService questionnaireResponseService;

  public CarePlan transformCarePlan(
      POCDMT000002UK01Section carePlanSection,
      Encounter encounter,
      List<TriageLine> triageLines,
      InputBundle inputBundle) {

    CarePlan carePlan = new CarePlan();

    carePlan.setTitle(NodeUtil.getNodeValueString(carePlanSection.getTitle()));

    //Hard coded to look where it is located in the POCD_EX200001GB02_01_A1_ITK.xml example.
    String carePlanTextContent =
        NodeUtil.getNodeValueString(carePlanSection.getText().getContentArray(0));

    Narrative narrative = new Narrative();
    narrative.setDivAsString(carePlanTextContent);
    carePlan.setText(narrative);
    carePlan.setDescription(carePlanTextContent);
    carePlan.setStatus(CarePlanStatus.COMPLETED);
    carePlan.setIntent(CarePlanIntent.PLAN);
    carePlan.setContext(new Reference(encounter));
    carePlan.setSubject(encounter.getSubject());

//    carePlan.addAuthor(); //TODO: No mapping exists.
//    carePlan.addAddresses(); //TODO: No mapping exists.

    inputBundle.getResourcesCreated().stream()
        .filter(ofTypes(QuestionnaireResponse.class, Observation.class))
        .map(Resource::getIdElement)
        .map(Reference::new)
        .forEach(carePlan::addSupportingInfo);

    return carePlan;
  }

}
