package uk.nhs.gpitf.reports.transform;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.net.URL;
import org.apache.xmlbeans.XmlException;
import org.hl7.fhir.dstu3.model.Condition;
import org.hl7.fhir.dstu3.model.Encounter;
import org.hl7.fhir.dstu3.model.Reference;
import org.hl7.fhir.dstu3.model.ReferralRequest;
import org.hl7.fhir.dstu3.model.ReferralRequest.ReferralCategory;
import org.hl7.fhir.dstu3.model.ReferralRequest.ReferralPriority;
import org.hl7.fhir.dstu3.model.ReferralRequest.ReferralRequestStatus;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import uk.nhs.connect.iucds.cda.ucr.ClinicalDocumentDocument1;
import uk.nhs.connect.iucds.cda.ucr.ClinicalDocumentDocument1.Factory;
import uk.nhs.connect.iucds.cda.ucr.POCDMT000002UK01InformationRecipient;
import uk.nhs.gpitf.reports.constants.FHIRSystems;
import uk.nhs.gpitf.reports.constants.IUCDSSystems;
import uk.nhs.gpitf.reports.service.ConditionService;
import uk.nhs.gpitf.reports.service.HealthcareServiceService;

@RunWith(MockitoJUnitRunner.class)
public class ReferralRequestTransformerTest {

  @Mock
  private ConditionService conditionService;

  @Mock
  private HealthcareServiceService healthcareServiceService;

  @InjectMocks
  private ReferralRequestTransformer referralRequestTransformer;

  private ClinicalDocumentDocument1 clinicalDocument;
  private Encounter encounter;

  private Reference patientRef = new Reference("Patient/1");
  private Reference deviceRef = new Reference("Device/1");
  private Reference conditionRef = new Reference("Condition/1");
  private Reference healthcareServiceRef = new Reference("HealthcareService/1");

  @Before
  public void setup() throws IOException, XmlException {
    URL resource = getClass().getResource("/example-clinical-doc.xml");
    clinicalDocument = Factory.parse(resource);

    encounter = new Encounter();
    encounter
        .setSubject(patientRef)
        .setId("Encounter/1");

    Mockito.when(conditionService.create(any())).thenReturn(conditionRef);
    Mockito.when(healthcareServiceService
        .createHealthcareService(any(POCDMT000002UK01InformationRecipient.class)))
        .thenReturn(healthcareServiceRef);
  }

  @Test
  public void testTransform() {

    ReferralRequest referralRequest = referralRequestTransformer
        .transform(clinicalDocument, encounter, deviceRef);

    assertEquals(ReferralRequestStatus.ACTIVE, referralRequest.getStatus());
    assertEquals(ReferralCategory.PLAN, referralRequest.getIntent());
    assertEquals(ReferralPriority.ROUTINE, referralRequest.getPriority());

    assertTrue("context",
        new Reference(encounter).equalsDeep(referralRequest.getContext()));
    assertTrue("subject",
        encounter.getSubject().equalsDeep(referralRequest.getSubject()));

    assertTrue("occurrence", referralRequest.hasOccurrence());
    assertTrue("authoredOn", referralRequest.hasAuthoredOn());
    assertTrue("requester.agent",
        deviceRef.equalsDeep(referralRequest.getRequester().getAgent()));
    assertTrue("requester.onBehalfOf",
        encounter.getServiceProvider().equalsDeep(referralRequest.getRequester().getOnBehalfOf()));

    assertTrue("recipient",
        referralRequest.getRecipientFirstRep().equalsDeep(healthcareServiceRef));
    assertTrue("reasonReference",
        referralRequest.getReasonReferenceFirstRep().equalsDeep(conditionRef));

    verify(healthcareServiceService, times(2))
        .createHealthcareService(any(POCDMT000002UK01InformationRecipient.class));

    var conditionCaptor = ArgumentCaptor.forClass(Condition.class);
    verify(conditionService).create(conditionCaptor.capture());
    Condition condition = conditionCaptor.getValue();
    assertTrue("Condition missing snomed code",
        condition.getCode().getCoding().stream().anyMatch(coding ->
            coding.getSystem().equals(FHIRSystems.SNOMED)
                && coding.getCode().equals("22298006")
        ));
    assertTrue("Condition missing pathways code",
        condition.getCode().getCoding().stream().anyMatch(coding ->
            coding.getSystem().equals(IUCDSSystems.CLINICAL_DISCRIMINATORS)
                && coding.getCode().equals("Dx011")
        ));
  }
}
