package uk.nhs.gpitf.reports.service;

import lombok.RequiredArgsConstructor;
import org.apache.xmlbeans.XmlRuntimeException;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.dstu3.model.Reference;
import org.springframework.stereotype.Service;
import uk.nhs.connect.iucds.cda.ucr.POCDMT000002UK01ClinicalDocument1;
import uk.nhs.gpitf.reports.model.InputBundle;
import uk.nhs.gpitf.reports.transform.PatientTransformer;

@Service
@RequiredArgsConstructor
public class PatientService {

  private final PatientTransformer patientTransformer;
  private final FhirStorageService storageService;

  public Reference createPatient(InputBundle inputBundle, POCDMT000002UK01ClinicalDocument1 document) {
    var recordTargets = document.getRecordTargetArray();
    if (recordTargets.length != 1) {
      throw new XmlRuntimeException("No recordTarget element found in ClinicalDocument");
    }

    Patient patient = patientTransformer.transform(inputBundle, recordTargets[0].getPatientRole());
    inputBundle.addResource(patient);
    return storageService.create(patient);
  }
}
