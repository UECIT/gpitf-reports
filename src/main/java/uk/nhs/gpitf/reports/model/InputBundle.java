package uk.nhs.gpitf.reports.model;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import org.hl7.fhir.dstu3.model.DomainResource;
import org.nhspathways.webservices.pathways.pathwayscase.PathwaysCaseDocument.PathwaysCase;
import uk.nhs.connect.iucds.cda.ucr.POCDMT000002UK01ClinicalDocument1;

@Data
public class InputBundle {
  private POCDMT000002UK01ClinicalDocument1 clinicalDocument;
  private PathwaysCase pathwaysCase;

  List<DomainResource> resourcesCreated = new ArrayList<>();

  public void addResource(DomainResource reference) {
    resourcesCreated.add(reference);
  }

}
