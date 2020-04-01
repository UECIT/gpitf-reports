package uk.nhs.gpitf.reports;

import lombok.experimental.UtilityClass;
import org.hl7.fhir.dstu3.model.CarePlan;
import org.hl7.fhir.dstu3.model.Narrative;
import org.hl7.fhir.dstu3.model.Observation;
import org.hl7.fhir.dstu3.model.Questionnaire;
import org.hl7.fhir.dstu3.model.QuestionnaireResponse;
import org.hl7.fhir.dstu3.model.ReferralRequest;
import org.hl7.fhir.utilities.xhtml.XhtmlNode;

@UtilityClass
public class FhirStub {

  public CarePlan carePlan() {
    return (CarePlan) new CarePlan().setText(narrative("Care Plan")).setId("CarePlan/123");
  }

  public ReferralRequest referralRequest() {
    return (ReferralRequest) new ReferralRequest().setText(narrative("Referral"))
        .setId("ReferralRequest/421");
  }

  public Questionnaire questionnaire() {
    return (Questionnaire) new Questionnaire().setText(narrative("QNaire"))
        .setId("Questionnaire/33");
  }

  public QuestionnaireResponse questionnaireResponse() {
    return (QuestionnaireResponse) new QuestionnaireResponse().setText(narrative("QResponse"))
        .setId("QuestionnaireResponse/4444");
  }

  public Observation observation() {
    return (Observation) new Observation().setText(narrative("Observation"))
        .setId("Observation/33322");
  }

  private Narrative narrative(String string) {
    return new Narrative().setDiv(new XhtmlNode().setValue(string));
  }

}
