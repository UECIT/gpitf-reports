package uk.nhs.gpitf.reports.transform;

import static org.exparity.hamcrest.date.DateMatchers.isInstant;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.verify;

import java.net.URL;
import java.time.Month;
import org.hl7.fhir.dstu3.model.Encounter;
import org.hl7.fhir.dstu3.model.Encounter.EncounterStatus;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.nhs.connect.iucds.cda.ucr.ClinicalDocumentDocument1.Factory;
import uk.nhs.connect.iucds.cda.ucr.POCDMT000002UK01ClinicalDocument1;
import uk.nhs.gpitf.reports.model.InputBundle;
import uk.nhs.gpitf.reports.service.AllergyIntoleranceService;
import uk.nhs.gpitf.reports.service.ClinicalImpressionService;
import uk.nhs.gpitf.reports.service.EncounterParticipantService;
import uk.nhs.gpitf.reports.service.EpisodeOfCareService;
import uk.nhs.gpitf.reports.service.LocationService;
import uk.nhs.gpitf.reports.service.MedicationService;
import uk.nhs.gpitf.reports.service.OrganizationService;
import uk.nhs.gpitf.reports.service.PatientService;
import uk.nhs.gpitf.reports.service.RelatedPersonService;
import uk.nhs.gpitf.reports.service.TriageReportService;

@RunWith(MockitoJUnitRunner.class)
public class EncounterTransformerTest {

  @InjectMocks
  private EncounterTransformer encounterTransformer;

  @Mock
  private LocationService locationService;

  @Mock
  private OrganizationService organizationService;

  @Mock
  private EncounterParticipantService encounterParticipantService;

  @Mock
  private EpisodeOfCareService episodeOfCareService;

  @Mock
  private PatientService patientService;
  
  @Mock
  private RelatedPersonService relatedPersonService;
  
  @Mock
  private MedicationService medicationService;

  @Mock
  private AllergyIntoleranceService allergyIntoleranceService;
  
  @Mock
  private TriageReportService triageReportService;
  
  @Mock
  private ClinicalImpressionService clinicalImpressionService;
  
  @Test
  public void testTransform() throws Exception {
    URL resource = getClass().getResource("/example-clinical-doc.xml");
    var inputBundle = new InputBundle();
    inputBundle.setClinicalDocument(Factory.parse(resource).getClinicalDocument());

    Encounter encounter = encounterTransformer.transform(inputBundle);

    assertThat(encounter.getPeriod().getStart(),
        isInstant(2017, Month.JANUARY, 1, 19, 45, 0, 0));
    assertThat(encounter.getPeriod().getEnd(),
        isInstant(2017, Month.JANUARY, 1, 20, 15, 0, 0));
    assertThat(encounter.getStatus(), is(EncounterStatus.FINISHED));

    POCDMT000002UK01ClinicalDocument1 clinicalDocument = inputBundle.getClinicalDocument();
    verify(episodeOfCareService).createEpisodeOfCare(same(inputBundle), same(clinicalDocument));
    verify(encounterParticipantService).createParticipants(inputBundle, clinicalDocument);
    verify(locationService).createFromEncompassingEncounter(clinicalDocument);
    verify(organizationService).createServiceProvider(inputBundle, clinicalDocument);
    verify(patientService).createPatient(inputBundle, clinicalDocument);
    verify(relatedPersonService).createRelatedPerson(inputBundle, encounter);
    verify(medicationService).createMedication(inputBundle, encounter);
    verify(allergyIntoleranceService).createAllergyIntolerance(inputBundle, encounter);
  }
}