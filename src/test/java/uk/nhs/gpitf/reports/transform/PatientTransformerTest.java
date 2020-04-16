package uk.nhs.gpitf.reports.transform;

import static org.hamcrest.CoreMatchers.both;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static uk.nhs.gpitf.reports.Matchers.isConcept;
import static uk.nhs.gpitf.reports.Matchers.isNhsNumber;
import static uk.nhs.gpitf.reports.Matchers.isReferenceWithDisplay;
import java.io.IOException;
import java.net.URL;
import org.apache.xmlbeans.XmlException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import uk.nhs.connect.iucds.cda.ucr.AD;
import uk.nhs.connect.iucds.cda.ucr.BL;
import uk.nhs.connect.iucds.cda.ucr.CE;
import uk.nhs.connect.iucds.cda.ucr.CS;
import uk.nhs.connect.iucds.cda.ucr.II;
import uk.nhs.connect.iucds.cda.ucr.PN;
import uk.nhs.connect.iucds.cda.ucr.POCDMT000002UK01LanguageCommunication;
import uk.nhs.connect.iucds.cda.ucr.POCDMT000002UK01Organization;
import uk.nhs.connect.iucds.cda.ucr.POCDMT000002UK01Patient;
import uk.nhs.connect.iucds.cda.ucr.POCDMT000002UK01PatientRole;
import uk.nhs.connect.iucds.cda.ucr.TEL;
import uk.nhs.connect.iucds.cda.ucr.TS;
import uk.nhs.connect.iucds.cda.ucr.ClinicalDocumentDocument1.Factory;
import uk.nhs.gpitf.reports.Stub;
import uk.nhs.gpitf.reports.constants.FHIRSystems;
import uk.nhs.gpitf.reports.constants.IUCDSSystems;
import uk.nhs.gpitf.reports.enums.Language;
import uk.nhs.gpitf.reports.enums.LanguageAbilityMode;
import uk.nhs.gpitf.reports.enums.LanguageAbilityProficiency;
import uk.nhs.gpitf.reports.enums.MaritalStatus;
import uk.nhs.gpitf.reports.enums.NhsNumberVerificationStatus;
import uk.nhs.gpitf.reports.model.InputBundle;
import uk.nhs.gpitf.reports.service.OrganizationService;
import uk.nhs.gpitf.reports.util.DateUtil;

@RunWith(MockitoJUnitRunner.class)
public class PatientTransformerTest {

  @Mock
  private AddressTransformer addressTransformer;

  @Mock
  private HumanNameTransformer humanNameTransformer;

  @Mock
  private OrganizationService organizationService;

  @InjectMocks
  private PatientTransformer patientTransformer;

  private POCDMT000002UK01PatientRole documentPatientRole;
  private POCDMT000002UK01Patient documentPatient;
  private POCDMT000002UK01LanguageCommunication documentLanguageCommunication;
  private POCDMT000002UK01Organization documentOrganization;
  private InputBundle inputBundle;

  @Before
  public void setup() throws XmlException, IOException {
    documentPatientRole = POCDMT000002UK01PatientRole.Factory.newInstance();
    documentPatient = POCDMT000002UK01Patient.Factory.newInstance();
    documentLanguageCommunication = POCDMT000002UK01LanguageCommunication.Factory.newInstance();
    documentOrganization = POCDMT000002UK01Organization.Factory.newInstance();
    URL resource = getClass().getResource("/example-clinical-doc.xml");
    inputBundle = new InputBundle();
    inputBundle.setClinicalDocument(Factory.parse(resource).getClinicalDocument());
  }

  private void linkDocuments() {
    documentPatient.setLanguageCommunicationArray(
        new POCDMT000002UK01LanguageCommunication[]{ documentLanguageCommunication });
    documentPatientRole.setPatient(documentPatient);
    documentPatientRole.setProviderOrganization(documentOrganization);
  }

  @Test
  public void transformOnlyIdentifier_random() {
    var identifier = Stub.createII(IUCDSSystems.LOCAL_PERSON, "testIdentifier", "testAuthority");
    documentPatientRole.setIdArray(new II[]{identifier});

    var patient = patientTransformer.transform(inputBundle, documentPatientRole);

    assertThat(patient.getIdentifier(), contains(
        both(hasProperty("value", equalTo("testIdentifier")))
        .and(hasProperty("assigner", isReferenceWithDisplay("testAuthority")))));
  }

  @Test
  public void transformOnlyIdentifier_verifiedNhsNumber() {
    var verifiedNhsNumber = Stub.createII(IUCDSSystems.NHS_NUMBER_VERIFIED, "012345678");
    documentPatientRole.setIdArray(new II[]{verifiedNhsNumber});

    var patient = patientTransformer.transform(inputBundle, documentPatientRole);

    var identifier = patient.getIdentifierFirstRep();
    assertThat(identifier, isNhsNumber("012345678"));
    assertThat(identifier, hasProperty(
        "nhsNumberVerificationStatus",
        isConcept(NhsNumberVerificationStatus.VERIFIED)));
  }

  @Test
  public void transformOnlyIdentifier_unverifiedNhsNumber() {
    var unverifiedNhsNumber = Stub.createII(IUCDSSystems.NHS_NUMBER_UNVERIFIED, "012345678");
    documentPatientRole.setIdArray(new II[]{unverifiedNhsNumber});

    var patient = patientTransformer.transform(inputBundle, documentPatientRole);

    var identifier = patient.getIdentifierFirstRep();
    assertThat(identifier, isNhsNumber("012345678"));
    assertThat(identifier, hasProperty(
        "nhsNumberVerificationStatus",
        isConcept(NhsNumberVerificationStatus.UNVERIFIED)));
  }

  @Test
  public void transformOnlyTelecom() {
    documentPatientRole.setTelecomArray(new TEL[] { Stub.tel() });

    var patient = patientTransformer.transform(inputBundle, documentPatientRole);

    var telecom = patient.getTelecomFirstRep();
    assertThat(telecom.getValue(), is("012345678"));
  }

  @Test
  public void transformOnlyCommunicationComponent() {
    var languageCode = CS.Factory.newInstance();
    languageCode.setCode("en");
    documentLanguageCommunication.setLanguageCode(languageCode);
    var isPreferred = BL.Factory.newInstance();
    isPreferred.setValue(true);
    documentLanguageCommunication.setPreferenceInd(isPreferred);

    linkDocuments();

    var patient = patientTransformer.transform(inputBundle, documentPatientRole);
    var communication = patient.getCommunicationFirstRep();

    assertThat(communication.getLanguage(), isConcept(Language.EN));
    assertThat(communication.getPreferred(), is(true));
  }

  @Test
  public void transformOnlyCommunicationExtension() {
    var languageCode = CS.Factory.newInstance();
    languageCode.setCode("en");
    documentLanguageCommunication.setLanguageCode(languageCode);
    var isPreferred = BL.Factory.newInstance();
    isPreferred.setValue(true);
    documentLanguageCommunication.setPreferenceInd(isPreferred);

    var abilityMode = CE.Factory.newInstance();
    abilityMode.setCode("ESP");
    documentLanguageCommunication.setModeCode(abilityMode);

    var abilityProficiency = CE.Factory.newInstance();
    abilityProficiency.setCode("G");
    documentLanguageCommunication.setProficiencyLevelCode(abilityProficiency);

    linkDocuments();

    var patient = patientTransformer.transform(inputBundle, documentPatientRole);
    var communication = patient.getExtensionsByUrl(FHIRSystems.NHS_COMMUNICATION).get(0);

    assertThat(
        communication.getExtensionsByUrl("language").get(0).getValue(),
        isConcept(Language.EN));
    assertThat(
        communication.getExtensionsByUrl("preferred").get(0).getValue(),
        hasProperty("value", is(true)));
    assertThat(
        communication.getExtensionsByUrl("modeOfCommunication").get(0).getValue(),
        isConcept(LanguageAbilityMode.EXPRESSED_SPOKEN));
    assertThat(
        communication.getExtensionsByUrl("communicationProficiency").get(0).getValue(),
        isConcept(LanguageAbilityProficiency.GOOD));
  }

  @Test
  public void transformBasicPatient() {
    documentPatientRole.setAddrArray(new AD[] { Stub.addr() });
    documentPatient.setNameArray(new PN[] { Stub.fullPersonName() });

    TS birthDate = TS.Factory.newInstance();
    birthDate.setValue("19750915"); // 15 Sep 1975
    documentPatient.setBirthTime(birthDate);

    var gender = CE.Factory.newInstance();
    gender.setDisplayName("Male");
    documentPatient.setAdministrativeGenderCode(gender);

    var maritalStatus = CE.Factory.newInstance();
    maritalStatus.setCode(MaritalStatus.POLYGAMOUS.getValue());
    documentPatient.setMaritalStatusCode(maritalStatus);

    linkDocuments();

    var patient = patientTransformer.transform(inputBundle, documentPatientRole);

    assertThat(patient.getBirthDate(), equalTo(DateUtil.parse("19750915")));
    assertThat(patient.getGender().toCode(), is("male"));
    assertThat(patient.getMaritalStatus(), isConcept(MaritalStatus.POLYGAMOUS));

    Mockito.verify(addressTransformer).transform(documentPatientRole.getAddrArray(0));
    Mockito.verify(humanNameTransformer)
        .transform(documentPatientRole.getPatient().getNameArray(0));
    Mockito.verify(organizationService)
        .createOrganization(inputBundle, documentPatientRole.getProviderOrganization());
  }
}
