package uk.nhs.gpitf.reports.service;

import lombok.RequiredArgsConstructor;
import org.hl7.fhir.dstu3.model.Device;
import org.hl7.fhir.dstu3.model.Device.DeviceUdiComponent;
import org.hl7.fhir.dstu3.model.Device.FHIRDeviceStatus;
import org.hl7.fhir.dstu3.model.Reference;
import org.springframework.stereotype.Service;
import uk.nhs.gpitf.reports.enums.DeviceKind;

@Service
@RequiredArgsConstructor
public class DeviceService {

  private final FhirStorageService storageService;
  private final NarrativeService narrativeService;

  public Reference createTransformerDevice() {
   Device device = new Device();
    device.setText(narrativeService.buildNarrative(
        "111 Report to CDS Encounter Report Transformer"));

    device
        .setStatus(FHIRDeviceStatus.ACTIVE)
        .setType(DeviceKind.APPLICATION_SOFTWARE.toCodeableConcept())
        .setUdi(new DeviceUdiComponent().setName("111 Report transformer"));

    return createDevice(device);
  }

  public Reference createDevice(Device device) {
    return storageService.create(device);
  }
}
