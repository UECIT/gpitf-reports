package uk.nhs.gpitf.reports.transform;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.Coding;
import org.hl7.fhir.dstu3.model.ContactPoint;
import org.hl7.fhir.dstu3.model.Encounter;
import org.hl7.fhir.dstu3.model.Enumerations.AdministrativeGender;
import org.hl7.fhir.dstu3.model.RelatedPerson;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;
import uk.nhs.connect.iucds.cda.ucr.POCDMT000002UK01ClinicalDocument1;
import uk.nhs.connect.iucds.cda.ucr.POCDMT000002UK01Informant12;

@Component
@RequiredArgsConstructor
public class RelatedPersonTransformer {

  private final AddressTransformer addressTransformer;
  private final HumanNameTransformer humanNameTransformer;
  
  public RelatedPerson transform(POCDMT000002UK01ClinicalDocument1 clinicalDocument, Encounter encounter) {
    POCDMT000002UK01Informant12 informant = null;
    RelatedPerson RelatedPerson = null;
    if (clinicalDocument.getInformantArray() != null && clinicalDocument.getInformantArray().length > 0) {
      informant = clinicalDocument.getInformantArray(0);
      
      List<ContactPoint> contacts = new ArrayList<ContactPoint>();
      ContactPoint tel1= new ContactPoint().setValue(informant.getRelatedEntity().getTelecomArray(0).getValue());
      ContactPoint tel2= new ContactPoint().setValue(informant.getRelatedEntity().getTelecomArray(1).getValue());
      contacts.add(tel1);
      contacts.add(tel2);
      
      RelatedPerson = new RelatedPerson()
          .setName(Arrays.asList(humanNameTransformer.transform(informant.getRelatedEntity().getRelatedPerson().getNameArray()[0])))
          .setGender(AdministrativeGender.UNKNOWN)
          .setPatient(encounter.getSubject())
          .setRelationship(new CodeableConcept().addCoding(new Coding().setDisplay(informant.getRelatedEntity().getCode().xgetDisplayName().getStringValue())))
          .setTelecom(contacts)
          .setAddress(Arrays.asList(addressTransformer.transform(informant.getRelatedEntity().getAddrArray()[0])));
    }
    return RelatedPerson;
  }
}
