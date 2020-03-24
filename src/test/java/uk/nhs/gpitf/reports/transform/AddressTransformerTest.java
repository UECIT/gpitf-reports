package uk.nhs.gpitf.reports.transform;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import com.google.common.collect.Iterables;
import org.junit.Test;
import uk.nhs.gpitf.reports.Stub;

public class AddressTransformerTest {

  @Test
  public void transformPlace() {

    var address = new AddressTransformer().transform(Stub.addr());

    assertThat(Iterables.getOnlyElement(address.getLine()).getValue(), is("1 Main Street"));
    assertThat(address.getCity(), is("City"));
    assertThat(address.getPostalCode(), is("NE1 4HH"));
    assertThat(address.getCountry(), is("UK"));
    assertThat(address.getText(), is("The Address of the Location"));
  }
}
