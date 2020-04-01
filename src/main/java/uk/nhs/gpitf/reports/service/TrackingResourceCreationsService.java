package uk.nhs.gpitf.reports.service;

import org.hl7.fhir.dstu3.model.DomainResource;
import org.hl7.fhir.dstu3.model.Reference;
import org.springframework.beans.factory.annotation.Autowired;
import uk.nhs.gpitf.reports.model.InputBundle;

public abstract class TrackingResourceCreationsService {

  @Autowired
  private FhirStorageService storageService;

  protected Reference create(DomainResource resource, InputBundle inputBundle) {
    Reference reference = storageService.create(resource);
    inputBundle.addResource(resource);
    return reference;
  }

}
