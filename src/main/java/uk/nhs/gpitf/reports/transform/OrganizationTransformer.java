package uk.nhs.gpitf.reports.transform;

import java.util.function.Consumer;
import lombok.RequiredArgsConstructor;
import org.hl7.fhir.dstu3.model.Coding;
import org.hl7.fhir.dstu3.model.Organization;
import org.springframework.stereotype.Component;
import uk.nhs.connect.iucds.cda.ucr.AD;
import uk.nhs.connect.iucds.cda.ucr.CE;
import uk.nhs.connect.iucds.cda.ucr.POCDMT000002UK01Organization;
import uk.nhs.connect.iucds.cda.ucr.TEL;
import uk.nhs.gpitf.reports.constants.FHIRSystems;
import uk.nhs.gpitf.reports.util.IdUtil;
import uk.nhs.gpitf.reports.util.NodeUtil;

@Component
@RequiredArgsConstructor
public class OrganizationTransformer {

  public Organization transform(POCDMT000002UK01Organization serviceProviderOrganization) {

    Organization organization = new Organization();

    IdUtil.getOdsCode(serviceProviderOrganization.getIdArray())
        .ifPresent(addOdsCode(organization));

    for (TEL tel: serviceProviderOrganization.getTelecomArray()) {
      organization.addTelecom()
          .setValue(tel.getValue());
    }

    if (serviceProviderOrganization.sizeOfNameArray() > 0) {
      organization.setName(NodeUtil.getNodeValueString(serviceProviderOrganization.getNameArray(0)));
    }

    for (AD ad: serviceProviderOrganization.getAddrArray()) {
      organization.addAddress()
          .setText(ad.xmlText()); //TODO: NCTH-595
    }

    if (serviceProviderOrganization.isSetStandardIndustryClassCode()) {
      CE code = serviceProviderOrganization.getStandardIndustryClassCode();
      organization.addType()
          .addCoding(new Coding(code.getCodeSystem(), code.getCode(), code.getDisplayName()));
    }

    return organization;
  }

  private Consumer<String> addOdsCode(Organization organization) {
    return site -> organization.addIdentifier()
        .setSystem(FHIRSystems.ODS_ORGANIZATION)
        .setValue(site);
  }

}
