package uk.nhs.gpitf.reports.service;

import java.io.IOException;
import org.hl7.fhir.dstu3.model.AllergyIntolerance;
import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.ClinicalImpression;
import org.hl7.fhir.dstu3.model.Consent;
import org.hl7.fhir.dstu3.model.DiagnosticReport;
import org.hl7.fhir.dstu3.model.Encounter;
import org.hl7.fhir.dstu3.model.MedicationStatement;
import org.hl7.fhir.dstu3.model.Observation;
import org.hl7.fhir.dstu3.model.Reference;
import org.hl7.fhir.dstu3.model.RelatedPerson;
import org.hl7.fhir.dstu3.model.Resource;
import org.hl7.fhir.instance.model.api.IIdType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IClientInterceptor;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.api.IHttpRequest;
import ca.uhn.fhir.rest.client.api.IHttpResponse;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FhirStorageService {

  private final FhirContext context;

  @Value("${fhir.server}")
  private String fhirServer;

  @Value("${fhir.server.auth.token}")
  private String fhirServerAuthToken;

  public Reference create(Resource resource) {
    var id = client()
        .create()
        .resource(resource)
        .execute()
        .getId();
    resource.setId(id);
    return new Reference(id);
  }

  private IGenericClient client() {
    IGenericClient iGenericClient = context.newRestfulGenericClient(fhirServer);
    iGenericClient.registerInterceptor(new IClientInterceptor() {
      @Override
      public void interceptRequest(IHttpRequest theRequest) {
        theRequest.addHeader(HttpHeaders.AUTHORIZATION, fhirServerAuthToken);
      }

      @Override
      public void interceptResponse(IHttpResponse theResponse) throws IOException {

      }
    });

    return iGenericClient;
  }

  public Bundle getEncounterReport(Reference reference) {
    IIdType referenceElement = reference.getReferenceElement();
    String baseUrl = referenceElement.getBaseUrl();
    String encounterId = referenceElement.getIdPart();

    return context.newRestfulGenericClient(baseUrl)
        .search().forResource(Encounter.class)
        .where(Encounter.RES_ID.exactly().identifier(encounterId))
        .include(Encounter.INCLUDE_ALL)
        .revInclude(Encounter.INCLUDE_ALL)
        .returnBundle(Bundle.class)
        .execute();
  }
  
  public Resource fetchResourceFromUrl(String theUrl, String refType) {
    if (refType.equals("Observation")) {
      return client().fetchResourceFromUrl(Observation.class, theUrl);
    } else if (refType.equals("Consent")) {
      return client().fetchResourceFromUrl(Consent.class, theUrl);
    } else if (refType.equals("ClinicalImpression")) {
      return client().fetchResourceFromUrl(ClinicalImpression.class, theUrl);
    } else if (refType.equals("AllergyIntolerance")) {
      return client().fetchResourceFromUrl(AllergyIntolerance.class, theUrl);
    } else if (refType.equals("DiagnosticReport")) {
      return client().fetchResourceFromUrl(DiagnosticReport.class, theUrl);
    } else if (refType.equals("MedicationStatement")) {
      return client().fetchResourceFromUrl(MedicationStatement.class, theUrl);
    } else if (refType.equals("RelatedPerson")) {
      return client().fetchResourceFromUrl(RelatedPerson.class, theUrl);
    }
    return null;
  }
}
