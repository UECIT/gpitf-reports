package uk.nhs.gpitf.reports.transform;

import java.util.Optional;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.dstu3.model.BooleanType;
import org.hl7.fhir.dstu3.model.CareConnectIdentifier;
import org.hl7.fhir.dstu3.model.ContactPoint;
import org.hl7.fhir.dstu3.model.Enumerations.AdministrativeGender;
import org.hl7.fhir.dstu3.model.Extension;
import org.hl7.fhir.dstu3.model.NHSNumberIdentifier;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.dstu3.model.Patient.PatientCommunicationComponent;
import org.hl7.fhir.dstu3.model.Reference;
import org.springframework.stereotype.Component;
import uk.nhs.connect.iucds.cda.ucr.II;
import uk.nhs.connect.iucds.cda.ucr.POCDMT000002UK01LanguageCommunication;
import uk.nhs.connect.iucds.cda.ucr.POCDMT000002UK01PatientRole;
import uk.nhs.connect.iucds.cda.ucr.TEL;
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

@Component
@RequiredArgsConstructor
public class PatientTransformer {

  private final AddressTransformer addressTransformer;
  private final HumanNameTransformer humanNameTransformer;
  private final OrganizationService organizationService;

  public Patient transform(InputBundle inputBundle, POCDMT000002UK01PatientRole patientRole) {
    var patient = new Patient();

    Stream.of(patientRole.getIdArray())
        .map(this::transformIdentifier)
        .flatMap(Optional::stream)
        .forEach(patient::addIdentifier);

    Stream.of(patientRole.getAddrArray())
        .map(addressTransformer::transform)
        .forEach(patient::addAddress);

    Stream.of(patientRole.getTelecomArray())
        .map(this::transformTelecom)
        .forEach(patient::addTelecom);

    var patientElement = patientRole.getPatient();
    if (patientElement != null) {
      Stream.of(patientElement.getNameArray())
          .map(humanNameTransformer::transform)
          .forEach(patient::addName);

      if (patientElement.isSetAdministrativeGenderCode()) {
        var genderCode = patientElement.getAdministrativeGenderCode();
        patient.setGender(AdministrativeGender.fromCode(
            genderCode.getDisplayName().toLowerCase()));
      }

      if (patientElement.isSetMaritalStatusCode()) {
        var maritalStatusCode = patientElement.getMaritalStatusCode().getCode();
        var maritalStatus = MaritalStatus.fromCode(maritalStatusCode).toCodeableConcept();
        patient.setMaritalStatus(maritalStatus);
      }

      if (patientElement.isSetBirthTime()) {
        var birthTimeText = patientElement.getBirthTime().getValue();
        patient.setBirthDate(DateUtil.parse(birthTimeText));
      }

      // TODO: patientElement.birthplace -> patient.extension (birthPlace)

      Stream.of(patientElement.getLanguageCommunicationArray())
          .map(this::transformCommunicationComponent)
          .forEach(patient::addCommunication);

      // TODO: this should technically go into CareConnectPatient.nhsCommunication extension
      // but the profile implementation has it mistakenly marked as a single element
      // instead of a collection
      Stream.of(patientElement.getLanguageCommunicationArray())
          .map(this::transformCommunicationExtension)
          .forEach(patient::addExtension);
    }

    if (patientRole.isSetProviderOrganization()) {
      patient.addGeneralPractitioner(
          organizationService.createOrganization(inputBundle, patientRole.getProviderOrganization()));
    }

    return patient;
  }

  public Optional<CareConnectIdentifier> transformIdentifier(II id) {
    CareConnectIdentifier identifier;
    switch (id.getRoot()) {
      case IUCDSSystems.LOCAL_PERSON:
        identifier = new CareConnectIdentifier();
        break;
      case IUCDSSystems.NHS_NUMBER_UNVERIFIED:
        identifier = new NHSNumberIdentifier()
            .setNhsNumberVerificationStatus(
                NhsNumberVerificationStatus.UNVERIFIED.toCodeableConcept())
            .setSystem(FHIRSystems.NHS_NUMBER);
        break;
      case IUCDSSystems.NHS_NUMBER_VERIFIED:
        identifier = new NHSNumberIdentifier()
            .setNhsNumberVerificationStatus(
              NhsNumberVerificationStatus.VERIFIED.toCodeableConcept())
            .setSystem(FHIRSystems.NHS_NUMBER);
        break;
      default:
        return Optional.empty();
    }

    identifier.setValue(id.getExtension());
    if (StringUtils.isNotEmpty(id.getAssigningAuthorityName())) {
      identifier.setAssigner(new Reference().setDisplay(id.getAssigningAuthorityName()));
    }

    return Optional.of(identifier);
  }

  public ContactPoint transformTelecom(TEL tel) {
    return new ContactPoint().setValue(tel.getValue());
  }

  public PatientCommunicationComponent transformCommunicationComponent(
      POCDMT000002UK01LanguageCommunication communicationElement) {
    var communicationComponent = new PatientCommunicationComponent();

    if (communicationElement.isSetLanguageCode()) {
      var languageCode = communicationElement.getLanguageCode().getCode();
      var language = Language.fromCode(languageCode).toCodeableConcept();
      communicationComponent.setLanguage(language);
    }

    if (communicationElement.isSetPreferenceInd()) {
      var isPreferred = communicationElement.getPreferenceInd().getValue();
      communicationComponent.setPreferred(isPreferred);
    }

    return communicationComponent;
  }

  // TODO: should return NhsCommunicationExtension but cannot as it does not extend Extension
  public Extension transformCommunicationExtension(
      POCDMT000002UK01LanguageCommunication communicationElement) {
    var communicationExtension = new Extension(FHIRSystems.NHS_COMMUNICATION);

    if (communicationElement.isSetLanguageCode()) {
      var languageCode = communicationElement.getLanguageCode().getCode();
      var language = Language.fromCode(languageCode).toCodeableConcept();
      communicationExtension.addExtension("language", language);
    }

    if (communicationElement.isSetPreferenceInd()) {
      var isPreferred = communicationElement.getPreferenceInd().getValue();
      communicationExtension.addExtension("preferred", new BooleanType(isPreferred));
    }

    if (communicationElement.isSetModeCode()) {
      var modeCode = communicationElement.getModeCode().getCode();
      var mode = LanguageAbilityMode.fromCode(modeCode).toCodeableConcept();
      communicationExtension.addExtension("modeOfCommunication", mode);
    }

    if (communicationElement.isSetProficiencyLevelCode()) {
      var proficiencyCode = communicationElement.getProficiencyLevelCode().getCode();
      var proficiency = LanguageAbilityProficiency.fromCode(proficiencyCode).toCodeableConcept();
      communicationExtension.addExtension("communicationProficiency", proficiency);
    }

    return communicationExtension;
  }
}
