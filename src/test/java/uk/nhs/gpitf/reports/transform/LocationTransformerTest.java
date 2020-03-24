package uk.nhs.gpitf.reports.transform;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import org.hl7.fhir.dstu3.model.Coding;
import org.hl7.fhir.dstu3.model.Identifier;
import org.hl7.fhir.dstu3.model.Location;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import uk.nhs.connect.iucds.cda.ucr.II;
import uk.nhs.connect.iucds.cda.ucr.POCDMT000002UK01HealthCareFacility;
import uk.nhs.connect.iucds.cda.ucr.POCDMT000002UK01Location;
import uk.nhs.connect.iucds.cda.ucr.POCDMT000002UK01Place;
import uk.nhs.gpitf.reports.Stub;
import uk.nhs.gpitf.reports.constants.FHIRSystems;

@RunWith(MockitoJUnitRunner.class)
public class LocationTransformerTest {

  @Mock
  private AddressTransformer addressTransformer;

  @InjectMocks
  private LocationTransformer locationTransformer;

  private POCDMT000002UK01Location documentLocation;

  @Before
  public void setup() {
    documentLocation = POCDMT000002UK01Location.Factory.newInstance();
  }

  @Test
  public void transformLocationCode() {
    documentLocation.addNewHealthCareFacility().setCode(Stub.code());

    Location location = locationTransformer.transform(documentLocation);

    Coding typeCoding = location.getType().getCodingFirstRep();
    assertThat(typeCoding.getSystem(), is(FHIRSystems.SERVICE_DELIVERY_LOCATION_ROLE_TYPE));
    assertThat(typeCoding.getCode(), is("CODE"));
    assertThat(typeCoding.getDisplay(), is("DISPLAY"));
  }

  @Test
  public void transformLocationPlaceOnly() {
    POCDMT000002UK01HealthCareFacility healthCareFacility = documentLocation
        .addNewHealthCareFacility();
    healthCareFacility.setCode(Stub.code());
    POCDMT000002UK01Place place = healthCareFacility.addNewLocation();
    place.setAddr(Stub.addr());
    place.setName(Stub.name());

    Location location = locationTransformer.transform(documentLocation);

    assertThat(location.getName(), is("Name of location"));
    Mockito.verify(addressTransformer).transform(place.getAddr());
  }

  @Test
  public void transformLocationOdsSite() {
    POCDMT000002UK01HealthCareFacility healthCareFacility = documentLocation
        .addNewHealthCareFacility();
    healthCareFacility.setCode(Stub.code());
    healthCareFacility.setIdArray(new II[]{Stub.odsSite()});

    Location location = locationTransformer.transform(documentLocation);

    Identifier odsSite = location.getIdentifierFirstRep();
    assertThat(odsSite.getSystem(), is(FHIRSystems.ODS_SITE));
    assertThat(odsSite.getValue(), is("SITE"));
  }

  @Test
  public void transformLocationOdsSitePlace() {
    POCDMT000002UK01HealthCareFacility healthCareFacility = documentLocation
        .addNewHealthCareFacility();
    healthCareFacility.setCode(Stub.code());
    POCDMT000002UK01Place place = healthCareFacility.addNewLocation();
    place.setAddr(Stub.addr());
    place.setName(Stub.name());
    healthCareFacility.setIdArray(new II[]{Stub.odsSite()});

    Location location = locationTransformer.transform(documentLocation);

    assertThat(location.getName(), is("Name of location"));
    Mockito.verify(addressTransformer).transform(place.getAddr());
    Identifier odsSite = location.getIdentifierFirstRep();
    assertThat(odsSite.getSystem(), is(FHIRSystems.ODS_SITE));
    assertThat(odsSite.getValue(), is("SITE"));
  }

}