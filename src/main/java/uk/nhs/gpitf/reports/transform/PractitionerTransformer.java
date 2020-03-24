package uk.nhs.gpitf.reports.transform;

import lombok.RequiredArgsConstructor;
import org.hl7.fhir.dstu3.model.Practitioner;
import org.springframework.stereotype.Component;
import uk.nhs.connect.iucds.cda.ucr.POCDMT000002UK01Person;

@Component
@RequiredArgsConstructor
public class PractitionerTransformer {

  private final HumanNameTransformer nameTransformer;

  public Practitioner transform(POCDMT000002UK01Person person) {
    Practitioner practitioner = new Practitioner();
    practitioner.addName(nameTransformer.transform(person.getNameArray(0)));
    return practitioner;
  }

}
