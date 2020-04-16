package uk.nhs.gpitf.reports.transform;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.hl7.fhir.dstu3.model.Consent;
import org.hl7.fhir.dstu3.model.Consent.ConsentDataComponent;
import org.hl7.fhir.dstu3.model.Consent.ConsentDataMeaning;
import org.hl7.fhir.dstu3.model.Consent.ConsentState;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;
import org.hl7.fhir.dstu3.model.Encounter;
import org.hl7.fhir.dstu3.model.Identifier;
import org.hl7.fhir.dstu3.model.Period;
import org.hl7.fhir.dstu3.model.Reference;
import org.springframework.stereotype.Component;
import uk.nhs.connect.iucds.cda.ucr.CE;
import uk.nhs.connect.iucds.cda.ucr.II;
import uk.nhs.connect.iucds.cda.ucr.IVLTS;
import uk.nhs.connect.iucds.cda.ucr.POCDMT000002UK01Authorization;
import uk.nhs.connect.iucds.cda.ucr.POCDMT000002UK01ClinicalDocument1;
import uk.nhs.connect.iucds.cda.ucr.POCDMT000002UK01Consent;
import uk.nhs.connect.iucds.cda.ucr.POCDMT000002UK01Entry;
import uk.nhs.connect.iucds.cda.ucr.POCDMT000002UK01Observation;
import uk.nhs.connect.iucds.cda.ucr.POCDMT000002UK01Section;
import uk.nhs.connect.iucds.cda.ucr.POCDMT000002UK01StructuredBody;
import uk.nhs.connect.iucds.cda.ucr.StrucDocTable;
import uk.nhs.gpitf.reports.constants.IUCDSSystems;
import uk.nhs.gpitf.reports.constants.IUCDSTemplates;
import uk.nhs.gpitf.reports.util.CodeUtil;
import uk.nhs.gpitf.reports.util.DateUtil;
import uk.nhs.gpitf.reports.util.StructuredBodyUtil;

@Component
@RequiredArgsConstructor
public class ConsentTransformer {

  public Consent transform(POCDMT000002UK01ClinicalDocument1 clinicalDocument,
      Encounter encounter) {

    Consent consent = new Consent()
        .setStatus(ConsentState.ACTIVE)
        .setPatient(encounter.getSubject())
        .setDateTime(new Date())
        .setConsentingParty(List.of(encounter.getSubject()))
        .addOrganization(encounter.getServiceProvider())
        .addData(new ConsentDataComponent()
            .setMeaning(ConsentDataMeaning.RELATED)
            .setReference(new Reference(encounter)));

    transformAuthCode(consent, clinicalDocument);

    var structuredBody = StructuredBodyUtil.getStructuredBody(clinicalDocument);
    if (structuredBody != null) {
      transformPermissionToView(consent, structuredBody);
      transformConsentSource(consent, structuredBody);
    }

    return consent;
  }

  private void transformAuthCode(Consent consent,
      POCDMT000002UK01ClinicalDocument1 clinicalDocument) {
    for (POCDMT000002UK01Authorization auth : clinicalDocument.getAuthorizationArray()) {
      POCDMT000002UK01Consent authConsent = auth.getConsent();
      CE consentCode = authConsent.getCode();
      consent.addAction(CodeUtil.createCodeableConceptFromCE(consentCode));
    }
  }

  private void transformPermissionToView(Consent consent,
      POCDMT000002UK01StructuredBody structuredBody) {
    var permissionEntries = StructuredBodyUtil
        .getEntriesOfType(structuredBody, IUCDSTemplates.PERMISSION_TO_VIEW);
    if (permissionEntries.isEmpty()) {
      return;
    }

    for (POCDMT000002UK01Entry permissionEntry : permissionEntries) {
      POCDMT000002UK01Observation observation = permissionEntry.getObservation();
      if (observation == null || !observation.isSetEffectiveTime()) {
        continue;
      }

      Period dataPeriod = new Period();
      IVLTS effectiveTime = observation.getEffectiveTime();
      if (effectiveTime.isSetLow()) {
        dataPeriod.setStart(DateUtil.parse(effectiveTime.getLow().getValue()));
      }
      if (effectiveTime.isSetHigh()) {
        dataPeriod.setEnd(DateUtil.parse(effectiveTime.getHigh().getValue()));
      }
      consent.setDataPeriod(dataPeriod);
    }
  }

  private void transformConsentSource(Consent consent,
      POCDMT000002UK01StructuredBody structuredBody) {

    List<POCDMT000002UK01Section> sections = StructuredBodyUtil.getSectionsOfType(
        structuredBody, IUCDSSystems.SNOMED, "887031000000108");

    if (sections.size() > 0) {
      String xmlText = sections.get(0).getTitle().xmlText();
      Document doc = Jsoup.parse(xmlText, "", Parser.xmlParser());
      String tableArray = sections.get(0).getText().xmlText();
      Document doc2 = Jsoup.parse(tableArray, "", Parser.xmlParser());
      ArrayList<String> downServers = new ArrayList<>();
      Element table = doc2.select("table").get(0); //select the first table.
      Elements rows = table.select("tr");

      for (int i = 1; i < rows.size(); i++) { //first row is the col names so skip it.
          Element row = rows.get(i);
          Elements cols = row.select("td");
          downServers.add(cols.select("td").text());
      }
    }
    for (POCDMT000002UK01Section section : sections) {
      II id = section.getId();
      consent.setSource(new Identifier().setValue(id.getRoot()));
    }
  }

}
