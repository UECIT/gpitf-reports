package uk.nhs.gpitf.reports.transform;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import org.hl7.fhir.dstu3.model.Address;
import org.hl7.fhir.dstu3.model.Identifier;
import org.hl7.fhir.dstu3.model.Organization;
import org.junit.Before;
import org.junit.Test;
import uk.nhs.connect.iucds.cda.ucr.AD;
import uk.nhs.connect.iucds.cda.ucr.II;
import uk.nhs.connect.iucds.cda.ucr.ON;
import uk.nhs.connect.iucds.cda.ucr.POCDMT000002UK01Organization;
import uk.nhs.connect.iucds.cda.ucr.TEL;
import uk.nhs.gpitf.reports.Stub;
import uk.nhs.gpitf.reports.constants.FHIRSystems;

public class OrganizationTransformerTest {

  private OrganizationTransformer organizationTransformer;

  private POCDMT000002UK01Organization documentOrg;

  @Before
  public void setup() {
    organizationTransformer = new OrganizationTransformer();
    documentOrg = POCDMT000002UK01Organization.Factory.newInstance();
  }

  @Test
  public void transformMinOrg() {
    documentOrg.setIdArray(new II[]{Stub.odsCode()});

    Organization organization = organizationTransformer.transform(documentOrg);

    Identifier odsSite = organization.getIdentifierFirstRep();
    assertThat(odsSite.getSystem(), is(FHIRSystems.ODS_ORGANIZATION));
    assertThat(odsSite.getValue(), is("ODS_CODE"));
  }

  @Test
  public void transformFullOrg() {
    documentOrg.setIdArray(new II[]{Stub.odsCode()});
    documentOrg.setNameArray(new ON[]{Stub.orgName()});
    documentOrg.setTelecomArray(new TEL[]{Stub.tel()});
    documentOrg.setAddrArray(new AD[]{Stub.addr()});

    Organization organization = organizationTransformer.transform(documentOrg);

    Identifier odsSite = organization.getIdentifierFirstRep();
    assertThat(odsSite.getSystem(), is(FHIRSystems.ODS_ORGANIZATION));
    assertThat(odsSite.getValue(), is("ODS_CODE"));
    assertThat(organization.getTelecomFirstRep().getValue(), is("012345678"));
    //TODO: NCTH-595
    Address address = organization.getAddressFirstRep();
//    assertThat(Iterables.getOnlyElement(address.getLine()).getValue(), is("1 Main Street"));
//    assertThat(address.getCity(), is("City"));
//    assertThat(address.getPostalCode(), is("NE1 4HH"));
//    assertThat(address.getCountry(), is("UK"));
//    assertThat(address.getText(), is("The Address of the Location"));
  }

}