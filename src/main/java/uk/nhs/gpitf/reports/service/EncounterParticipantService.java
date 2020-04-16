package uk.nhs.gpitf.reports.service;

import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.Coding;
import org.hl7.fhir.dstu3.model.Encounter.EncounterParticipantComponent;
import org.hl7.fhir.dstu3.model.Reference;
import org.springframework.stereotype.Service;
import uk.nhs.connect.iucds.cda.ucr.POCDMT000002UK01ClinicalDocument1;
import uk.nhs.connect.iucds.cda.ucr.POCDMT000002UK01EncounterParticipant;
import uk.nhs.gpitf.reports.model.InputBundle;

@Service
@RequiredArgsConstructor
public class EncounterParticipantService {

  private final PractitionerService practitionerService;

  public List<EncounterParticipantComponent> createParticipants(
      InputBundle inputBundle, POCDMT000002UK01ClinicalDocument1 clinicalDocument) {

    POCDMT000002UK01EncounterParticipant[] encounterParticipantArray = clinicalDocument
        .getComponentOf()
        .getEncompassingEncounter()
        .getEncounterParticipantArray();

    List<EncounterParticipantComponent> components = new ArrayList<>();

    for (POCDMT000002UK01EncounterParticipant participant: encounterParticipantArray) {
      Reference reference = practitionerService.createPractitioner(inputBundle,
          participant.getAssignedEntity().getAssignedPerson(), null, null);

      EncounterParticipantComponent component = new EncounterParticipantComponent();
      component.setIndividual(reference);
      component.addType(new CodeableConcept()
          .addCoding(new Coding()
              .setCode(participant.getTypeCode().toString()))); //TODO: Don't have system?

      components.add(component);
    }
    return components;
  }

}
