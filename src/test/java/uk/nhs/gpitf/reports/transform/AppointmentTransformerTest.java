package uk.nhs.gpitf.reports.transform;

import static org.exparity.hamcrest.date.DateMatchers.sameInstant;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.xmlbeans.XmlString;
import org.hl7.fhir.dstu3.model.Appointment;
import org.hl7.fhir.dstu3.model.Appointment.AppointmentParticipantComponent;
import org.hl7.fhir.dstu3.model.Appointment.AppointmentStatus;
import org.hl7.fhir.dstu3.model.Reference;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.nhs.connect.iucds.cda.ucr.AD;
import uk.nhs.connect.iucds.cda.ucr.IVLTS;
import uk.nhs.connect.iucds.cda.ucr.POCDMT000002UK01Encounter;
import uk.nhs.connect.iucds.cda.ucr.POCDMT000002UK01Entry;
import uk.nhs.connect.iucds.cda.ucr.POCDMT000002UK01ParticipantRole;
import uk.nhs.connect.iucds.cda.ucr.POCDMT000002UK01Section;
import uk.nhs.gpitf.reports.Stub;
import uk.nhs.gpitf.reports.service.LocationService;

@RunWith(MockitoJUnitRunner.class)
public class AppointmentTransformerTest {

  @Mock
  private LocationService locationService;

  @InjectMocks
  private AppointmentTransformer appointmentTransformer;

  private POCDMT000002UK01Entry appointmentEntry;
  private POCDMT000002UK01Section matchingSection;

  @Before
  public void setup() {
    appointmentEntry = POCDMT000002UK01Entry.Factory.newInstance();
  }

  @Test
  public void transformMinimumAppointment() {
    setEffectiveDate();

    Reference referralRef = new Reference("ReferralRequest/123");
    Reference patientRef = new Reference("Patient/321");

    Appointment transformedAppointment = appointmentTransformer
        .transform(appointmentEntry, null, referralRef, patientRef);
    ZonedDateTime start = ZonedDateTime.of(2017, 6, 1, 14, 0, 0, 0, ZoneId.of("UTC"));
    ZonedDateTime end = start.plusMinutes(10);

    assertThat(transformedAppointment.getStatus(), is(AppointmentStatus.BOOKED));
    assertThat(transformedAppointment.getStart(), sameInstant(start.toInstant()));
    assertThat(transformedAppointment.getEnd(), sameInstant(end.toInstant()));
    assertThat(transformedAppointment.getParticipant(),
        contains(hasProperty("actor", is(patientRef))));
    assertThat(transformedAppointment.getIncomingReferral(), contains(referralRef));
  }

  @Test
  public void transformFullAppointment() {
    setEffectiveDate();

    Reference referralRef = new Reference("ReferralRequest/123");
    Reference patientRef = new Reference("Patient/321");
    Reference locationRef = new Reference("Location/444");

    POCDMT000002UK01Encounter encounter = appointmentEntry.getEncounter();
    POCDMT000002UK01ParticipantRole participant = encounter.addNewParticipant().addNewParticipantRole();
    participant.setAddrArray(new AD[] {Stub.addr()});

    ZonedDateTime start = ZonedDateTime.of(2017, 6, 1, 14, 0, 0, 0, ZoneId.of("UTC"));
    ZonedDateTime end = start.plusMinutes(10);

    when(locationService.createFromParticipant(participant))
        .thenReturn(Optional.of(locationRef));

    matchingSection = POCDMT000002UK01Section.Factory.newInstance();
    matchingSection.addNewTitle().set(XmlString.Factory.newValue("Appointment Title"));
    matchingSection.addNewText().addNewContent().set(XmlString.Factory.newValue("Description"));

    Appointment transformedAppointment = appointmentTransformer
        .transform(appointmentEntry, null, referralRef, patientRef);

    assertThat(transformedAppointment.getStatus(), is(AppointmentStatus.BOOKED));
    assertThat(transformedAppointment.getStart(), sameInstant(start.toInstant()));
    assertThat(transformedAppointment.getEnd(), sameInstant(end.toInstant()));

    List<Reference> participantRefs = transformedAppointment.getParticipant().stream()
        .map(AppointmentParticipantComponent::getActor)
        .collect(Collectors.toUnmodifiableList());
    assertThat(participantRefs, containsInAnyOrder(patientRef, locationRef));
    assertThat(transformedAppointment.getIncomingReferral(), contains(referralRef));
  }

  private void setEffectiveDate() {
    POCDMT000002UK01Encounter encounter = appointmentEntry.addNewEncounter();
    IVLTS start = encounter.addNewEffectiveTime();
    start.setValue("201706011400+00");
  }


}