package uk.nhs.gpitf.reports;


import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import org.apache.commons.io.IOUtils;
import org.hl7.fhir.dstu3.model.CarePlan;
import org.hl7.fhir.dstu3.model.Encounter;
import org.hl7.fhir.dstu3.model.EpisodeOfCare;
import org.hl7.fhir.dstu3.model.Location;
import org.hl7.fhir.dstu3.model.Organization;
import org.hl7.fhir.dstu3.model.Practitioner;
import org.hl7.fhir.dstu3.model.Reference;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.nhs.gpitf.reports.controller.ReportController;
import uk.nhs.gpitf.reports.service.FhirStorageService;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class ReportControllerTest {

  @MockBean
  private FhirStorageService fhirStorageService;

  @Autowired
  private ReportController reportController;

  @Autowired
  private FhirStorageService storageService;

  @Before
  public void setup() {
    Mockito.reset(storageService);

    when(storageService.create(any()))
        .thenReturn(new Reference("123"));
  }

  @Test
  public void testExample() throws Exception {
    URL resource = getClass().getResource("/example-iucds.xml");
    testTransform(resource);
    verify(storageService).create(any(Encounter.class));
    verify(storageService, times(2))
        .create(any(Practitioner.class));
    verify(storageService).create(any(EpisodeOfCare.class));
    verify(storageService, times(2))
        .create(any(Organization.class));
    verify(storageService).create(any(Location.class));
    verify(storageService, never())
        .create(any(CarePlan.class));
  }

  @Test
  public void test111ExampleTransform() throws Exception {
    URL resource = getClass().getResource("/POCD_EX200001GB02_01_A1_ITK.xml");
    testTransform(resource);
    verify(storageService).create(any(Encounter.class));
    verify(storageService).create(any(Practitioner.class));
    verify(storageService).create(any(EpisodeOfCare.class));
    verify(storageService, times(4)).create(any(Organization.class));
    verify(storageService, times(2)).create(any(Location.class));
    verify(storageService).create(any(CarePlan.class));
  }

  private void testTransform(URL resource) throws Exception {
    String docString = IOUtils.toString(resource, StandardCharsets.UTF_8);

    ResponseEntity<String> encounterRef = reportController.createEncounterReport(docString);

    assertThat(encounterRef.getBody(), is("123"));
  }

}