package uk.nhs.gpitf.reports.transform;

import java.util.Arrays;
import java.util.Date;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.time.DateUtils;
import org.hl7.fhir.dstu3.model.Appointment;
import org.hl7.fhir.dstu3.model.Appointment.AppointmentParticipantComponent;
import org.hl7.fhir.dstu3.model.Appointment.AppointmentStatus;
import org.hl7.fhir.dstu3.model.Appointment.ParticipantRequired;
import org.hl7.fhir.dstu3.model.Appointment.ParticipationStatus;
import org.hl7.fhir.dstu3.model.Reference;
import org.springframework.stereotype.Component;
import uk.nhs.connect.iucds.cda.ucr.POCDMT000002UK01Encounter;
import uk.nhs.connect.iucds.cda.ucr.POCDMT000002UK01Entry;
import uk.nhs.connect.iucds.cda.ucr.POCDMT000002UK01Participant2;
import uk.nhs.connect.iucds.cda.ucr.POCDMT000002UK01Section;
import uk.nhs.gpitf.reports.service.LocationService;
import uk.nhs.gpitf.reports.util.DateUtil;
import uk.nhs.gpitf.reports.util.NodeUtil;

@Component
@RequiredArgsConstructor
public class AppointmentTransformer {

  private static final String FORMAT = "yyyyMMddHHmmX";

  private final LocationService locationService;

  public Appointment transform(
      POCDMT000002UK01Entry appointmentEntry,
      POCDMT000002UK01Section matchingSection,
      Reference referralRequest,
      Reference patient) {

    POCDMT000002UK01Encounter encounter = appointmentEntry.getEncounter();
    Date startDate = DateUtil.parse(encounter.getEffectiveTime().getValue());
    Date endDate = DateUtils.addMinutes(startDate, 10); //FIXED to 10 minutes after
    Appointment appointment = new Appointment()
        .setStatus(AppointmentStatus.BOOKED) //FIXED value
        .addIncomingReferral(referralRequest)
        .setStart(startDate)
        .setEnd(endDate);

    if (matchingSection != null) {
      appointment
          .setDescription(NodeUtil.getNodeValueString(matchingSection.getTitle()))
          .setComment(NodeUtil.getNodeValueString(matchingSection.getText().getContentArray(0)));
    }

    appointment.addParticipant(new AppointmentParticipantComponent()
        .setActor(patient)
        .setRequired(ParticipantRequired.REQUIRED)
        .setStatus(ParticipationStatus.ACCEPTED));

    Arrays.stream(encounter.getParticipantArray())
        .map(POCDMT000002UK01Participant2::getParticipantRole)
        .map(locationService::createFromParticipant)
        .filter(Optional::isPresent)
        .map(Optional::get)
        .map(ref -> new AppointmentParticipantComponent()
            .setActor(ref)
            .setRequired(ParticipantRequired.REQUIRED)
            .setStatus(ParticipationStatus.ACCEPTED))
        .forEach(appointment::addParticipant);
    return appointment;
  }

}
