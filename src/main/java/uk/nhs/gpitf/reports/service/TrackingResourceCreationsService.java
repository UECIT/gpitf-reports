package uk.nhs.gpitf.reports.service;

import org.hl7.fhir.dstu3.model.Reference;
import org.hl7.fhir.dstu3.model.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import uk.nhs.gpitf.reports.model.InputBundle;

public abstract class TrackingResourceCreationsService {

  @Autowired
  private FhirStorageService storageService;

  protected Reference create(Resource resource, InputBundle inputBundle) {
    Reference reference = storageService.create(resource);
    inputBundle.addResource(reference);
    return reference;
  }

}
