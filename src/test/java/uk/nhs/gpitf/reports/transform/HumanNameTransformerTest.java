package uk.nhs.gpitf.reports.transform;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import org.junit.Before;
import org.junit.Test;
import uk.nhs.gpitf.reports.Stub;

public class HumanNameTransformerTest {

  private HumanNameTransformer humanNameTransformer;

  @Before
  public void setup() {
    humanNameTransformer = new HumanNameTransformer();
  }

  @Test
  public void transform_noSubNodes() {
    var name = humanNameTransformer.transform(Stub.simplePersonName());

    assertThat(name.getText(), is("Stewie Griffin"));
    assertThat(name.getGiven(), empty());
    assertThat(name.getFamily(), isEmptyOrNullString());
  }

  @Test
  public void transform_withSubNodes() {
    var name = humanNameTransformer.transform(Stub.fullPersonName());

    assertThat(name.getText(), isEmptyOrNullString());
    assertThat(name.getGiven(), contains(hasToString("Homer")));
    assertThat(name.getFamily(), is("Simpson"));
  }
}
