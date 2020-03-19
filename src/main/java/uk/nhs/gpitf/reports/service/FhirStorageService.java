package uk.nhs.gpitf.reports.service;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IClientInterceptor;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.api.IHttpRequest;
import ca.uhn.fhir.rest.client.api.IHttpResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.hl7.fhir.dstu3.model.Reference;
import org.hl7.fhir.dstu3.model.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FhirStorageService {

  private final FhirContext context;

  @Value("${fhir.server}")
  private String fhirServer;

  @Value("${fhir.server.auth.token}")
  private String fhirServerAuthToken;

  /**
   * Updates a record with an existing ID, or {@link #create(Resource)} a new record if the ID is missing
   *
   * @param resource the resource to update on the remote server
   * @return a reference to the stored resource
   */
  public Reference upsert(Resource resource) {
    if (resource.hasId()) {
      client()
          .update()
          .resource(resource)
          .execute();
      return new Reference(resource.getId());
    } else {
      return create(resource);
    }
  }

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
}
