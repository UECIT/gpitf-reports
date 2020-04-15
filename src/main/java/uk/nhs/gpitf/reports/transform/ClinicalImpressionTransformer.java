package uk.nhs.gpitf.reports.transform;

import java.util.List;
import org.hl7.fhir.dstu3.model.ClinicalImpression;
import org.hl7.fhir.dstu3.model.ClinicalImpression.ClinicalImpressionStatus;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
import org.hl7.fhir.dstu3.model.Encounter;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;
import uk.nhs.connect.iucds.cda.ucr.POCDMT000002UK01ClinicalDocument1;
import uk.nhs.connect.iucds.cda.ucr.POCDMT000002UK01Section;
import uk.nhs.connect.iucds.cda.ucr.POCDMT000002UK01StructuredBody;
import uk.nhs.gpitf.reports.constants.IUCDSSystems;
import uk.nhs.gpitf.reports.util.StructuredBodyUtil;

@Component
@RequiredArgsConstructor
public class ClinicalImpressionTransformer {

  public ClinicalImpression transform(POCDMT000002UK01ClinicalDocument1 clinicalDocument, Encounter encounter) {
    
    POCDMT000002UK01StructuredBody structuredBody = StructuredBodyUtil
        .getStructuredBody(clinicalDocument);
    
    List<POCDMT000002UK01Section> clinicalImpressionSection = StructuredBodyUtil
        .getSectionsOfType(structuredBody, IUCDSSystems.SNOMED, "887181000000106");
    ClinicalImpression clinicalImpression = null;
    if (clinicalImpressionSection.size() > 0) {
      String contentArray = clinicalImpressionSection.get(0).getText().xmlText();
      Document doc = Jsoup.parse(contentArray, "", Parser.xmlParser());
      clinicalImpression = new ClinicalImpression()
          .setSubject(encounter.getSubject())
          .setStatus(ClinicalImpressionStatus.COMPLETED)
          .setDescription(doc.select("content").text());
    }
    return clinicalImpression;
  }
}
