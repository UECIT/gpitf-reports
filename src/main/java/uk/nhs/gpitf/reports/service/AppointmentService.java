package uk.nhs.gpitf.reports.service;

import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.hl7.fhir.dstu3.model.Appointment;
import org.hl7.fhir.dstu3.model.Reference;
import org.springframework.stereotype.Service;
import uk.nhs.connect.iucds.cda.ucr.POCDMT000002UK01Entry;
import uk.nhs.connect.iucds.cda.ucr.POCDMT000002UK01Section;
import uk.nhs.connect.iucds.cda.ucr.POCDMT000002UK01StructuredBody;
import uk.nhs.gpitf.reports.constants.IUCDSSystems;
import uk.nhs.gpitf.reports.constants.IUCDSTemplates;
import uk.nhs.gpitf.reports.model.InputBundle;
import uk.nhs.gpitf.reports.transform.AppointmentTransformer;
import uk.nhs.gpitf.reports.util.StructuredBodyUtil;

@Service
@RequiredArgsConstructor
public class AppointmentService {

  private static final String APPOINTMENT = "749001000000101";

  private final AppointmentTransformer appointmentTransformer;
  private final FhirStorageService storageService;

  public Optional<Reference> createAppointment(
      InputBundle inputBundle,
      Reference referralRequest,
      Reference patient
  ) {
    POCDMT000002UK01StructuredBody structuredBody = StructuredBodyUtil
        .getStructuredBody(inputBundle.getClinicalDocument());
    List<POCDMT000002UK01Entry> appointmentEnties =
        StructuredBodyUtil.getEntriesOfType(structuredBody, IUCDSTemplates.APPOINTMENT_REFERENCE);

    List<POCDMT000002UK01Section> appointmentSections = StructuredBodyUtil
        .getSectionsOfType(structuredBody, IUCDSSystems.SNOMED, APPOINTMENT);

    if (appointmentEnties.isEmpty()) {
      return Optional.empty();
    }

    Preconditions.checkArgument(appointmentEnties.size() == 1, "Multiple appointmentEnties found");
    POCDMT000002UK01Entry entry = Iterables.getOnlyElement(appointmentEnties);
    POCDMT000002UK01Section matchingSection = appointmentSections.iterator().next();
    Appointment appointment = appointmentTransformer
        .transform(entry, matchingSection, referralRequest, patient);

    return Optional.of(storageService.create(appointment));
  }
}
