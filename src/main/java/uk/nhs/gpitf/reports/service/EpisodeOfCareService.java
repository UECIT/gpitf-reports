package uk.nhs.gpitf.reports.service;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.hl7.fhir.dstu3.model.EpisodeOfCare;
import org.hl7.fhir.dstu3.model.Reference;
import org.springframework.stereotype.Service;
import uk.nhs.connect.iucds.cda.ucr.POCDMT000002UK01ClinicalDocument1;
import uk.nhs.connect.iucds.cda.ucr.POCDMT000002UK01EncompassingEncounter;
import uk.nhs.gpitf.reports.transform.EpisodeOfCareTransformer;

@Service
@RequiredArgsConstructor
public class EpisodeOfCareService {

  private final FhirStorageService storageService;

  private final EpisodeOfCareTransformer episodeOfCareTransformer;

  public Optional<Reference> createEpisodeOfCare(POCDMT000002UK01ClinicalDocument1 clinicalDocument) {
    POCDMT000002UK01EncompassingEncounter encompassingEncounter = clinicalDocument.getComponentOf()
        .getEncompassingEncounter();

    if (!encompassingEncounter.isSetResponsibleParty() ||
        encompassingEncounter.getResponsibleParty().getAssignedEntity() == null) {
      return Optional.empty();
    }

    EpisodeOfCare episodeOfCare = episodeOfCareTransformer
        .transformEpisodeOfCare(encompassingEncounter.getResponsibleParty().getAssignedEntity());

    return Optional.of(storageService.create(episodeOfCare));
  }

}
