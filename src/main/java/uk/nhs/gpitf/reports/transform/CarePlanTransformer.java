package uk.nhs.gpitf.reports.transform;

import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.hl7.fhir.dstu3.model.CarePlan;
import org.hl7.fhir.dstu3.model.CarePlan.CarePlanIntent;
import org.hl7.fhir.dstu3.model.CarePlan.CarePlanStatus;
import org.hl7.fhir.dstu3.model.Narrative;
import org.hl7.fhir.dstu3.model.Reference;
import org.springframework.stereotype.Component;
import uk.nhs.connect.iucds.cda.ucr.POCDMT000002UK01Section;
import uk.nhs.gpitf.reports.util.NodeUtil;

@Component
@RequiredArgsConstructor
public class CarePlanTransformer {

  @Value
  public static class CarePlanInput {
    POCDMT000002UK01Section carePlanSection;
    Reference encounter;
    Reference patient;
  }

  public CarePlan transformCarePlan(CarePlanInput input) {

    CarePlan carePlan = new CarePlan();
    POCDMT000002UK01Section carePlanSection = input.getCarePlanSection();

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
    carePlan.setContext(input.getEncounter());
    carePlan.setSubject(input.getPatient());
//    carePlan.addAuthor(); //TODO: No mapping exists.
//    carePlan.addAddresses(); //TODO: No mapping exists.
//    carePlan.addSupportingInfo(); //TODO: No mapping exists.

    return carePlan;
  }

}
