package uk.nhs.gpitf.reports.model;

import lombok.Data;
import org.nhspathways.webservices.pathways.pathwayscase.PathwaysCaseDocument.PathwaysCase;
import uk.nhs.connect.iucds.cda.ucr.POCDMT000002UK01ClinicalDocument1;

@Data
public class InputBundle {
  private POCDMT000002UK01ClinicalDocument1 clinicalDocument;
  private PathwaysCase pathwaysCase;
}
