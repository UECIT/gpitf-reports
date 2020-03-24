package uk.nhs.gpitf.reports.service;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.hl7.fhir.dstu3.model.Organization;
import org.hl7.fhir.dstu3.model.Reference;
import org.springframework.stereotype.Service;
import uk.nhs.connect.iucds.cda.ucr.POCDMT000002UK01ClinicalDocument1;
import uk.nhs.connect.iucds.cda.ucr.POCDMT000002UK01EncompassingEncounter;
import uk.nhs.connect.iucds.cda.ucr.POCDMT000002UK01Organization;
import uk.nhs.gpitf.reports.transform.OrganizationTransformer;

@Service
@RequiredArgsConstructor
public class OrganizationService {

  private final OrganizationTransformer organizationTransformer;

  private final FhirStorageService storageService;

  public Optional<Reference> createServiceProvider(POCDMT000002UK01ClinicalDocument1 clinicalDocument) {
    POCDMT000002UK01EncompassingEncounter encompassingEncounter = clinicalDocument.getComponentOf()
        .getEncompassingEncounter();

    if (encompassingEncounter.isSetLocation() &&
        encompassingEncounter.getLocation().getHealthCareFacility() != null &&
        encompassingEncounter.getLocation().getHealthCareFacility().isSetServiceProviderOrganization()) {

      POCDMT000002UK01Organization serviceProviderOrganization = encompassingEncounter.getLocation()
          .getHealthCareFacility()
          .getServiceProviderOrganization();
      return Optional.of(createOrganization(serviceProviderOrganization));
    }

    return Optional.empty();
  }

  public Reference createOrganization(POCDMT000002UK01Organization docOrg) {
    Organization organization = organizationTransformer.transform(docOrg);
    return storageService.create(organization);
  }

}
