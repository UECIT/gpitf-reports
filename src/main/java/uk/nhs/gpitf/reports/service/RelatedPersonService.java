package uk.nhs.gpitf.reports.service;

import org.hl7.fhir.dstu3.model.Encounter;
import org.hl7.fhir.dstu3.model.Reference;
import org.hl7.fhir.dstu3.model.RelatedPerson;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import uk.nhs.gpitf.reports.model.InputBundle;
import uk.nhs.gpitf.reports.transform.RelatedPersonTransformer;

@Service
@RequiredArgsConstructor
public class RelatedPersonService {

  private final FhirStorageService fhirStorageService;
  private final RelatedPersonTransformer relatedPersonTransformer;

  public Reference createRelatedPerson(InputBundle inputBundle, Encounter encounter) {
    RelatedPerson relatedPerson = relatedPersonTransformer.transform(inputBundle.getClinicalDocument(), encounter);
    if (relatedPerson != null) {
      inputBundle.addResource(relatedPerson);
      return create(relatedPerson);
    }
    return null;
  }
  
  public Reference create(RelatedPerson relatedPerson) {
    return fhirStorageService.create(relatedPerson);
  }
}
