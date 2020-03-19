package uk.nhs.gpitf.reports.util;

import java.util.Arrays;
import java.util.Optional;
import lombok.experimental.UtilityClass;
import uk.nhs.connect.iucds.cda.ucr.II;
import uk.nhs.gpitf.reports.constants.FHIRSystems;
import uk.nhs.gpitf.reports.constants.IUCDSSystems;

@UtilityClass
public class IdUtil {

  public Optional<String> getOdsSite(II[] idArray) {
    return Arrays.stream(idArray)
        .filter(id -> FHIRSystems.ODS_SITE.equals(id.getRoot()))
        .findAny()
        .map(II::getExtension);
  }

  public Optional<String> getOdsCode(II[] idArray) {
    return Arrays.stream(idArray)
        .filter(id -> IUCDSSystems.ODS_ORGANIZATION.equals(id.getRoot()))
        .findAny()
        .map(II::getExtension);
  }

}
