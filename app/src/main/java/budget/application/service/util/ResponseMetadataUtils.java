package budget.application.service.util;

import io.github.bibekaryal86.shdsvc.dtos.ResponseMetadata;
import io.github.bibekaryal86.shdsvc.helpers.CommonUtilities;

public class ResponseMetadataUtils {
  private ResponseMetadataUtils() {}

  public static ResponseMetadata defaultInsertResponseMetadata() {
    return new ResponseMetadata(
        ResponseMetadata.emptyResponseStatusInfo(),
        new ResponseMetadata.ResponseCrudInfo(1, 0, 0, 0),
        ResponseMetadata.emptyResponsePageInfo());
  }

  public static ResponseMetadata defaultUpdateResponseMetadata() {
    return new ResponseMetadata(
        ResponseMetadata.emptyResponseStatusInfo(),
        new ResponseMetadata.ResponseCrudInfo(0, 1, 0, 0),
        ResponseMetadata.emptyResponsePageInfo());
  }

  public static ResponseMetadata defaultDeleteResponseMetadata(int deletedCount) {
    return new ResponseMetadata(
        ResponseMetadata.emptyResponseStatusInfo(),
        new ResponseMetadata.ResponseCrudInfo(0, 0, deletedCount, 0),
        ResponseMetadata.emptyResponsePageInfo());
  }

  public static ResponseMetadata defaultStatusInfoResponseMetadata(String errMsg, Exception ex) {
    if (!CommonUtilities.isEmpty(errMsg)) {
      return new ResponseMetadata(
          new ResponseMetadata.ResponseStatusInfo(errMsg),
          ResponseMetadata.emptyResponseCrudInfo(),
          ResponseMetadata.emptyResponsePageInfo());
    } else if (ex != null && !CommonUtilities.isEmpty(ex.getMessage())) {
      return new ResponseMetadata(
          new ResponseMetadata.ResponseStatusInfo(ex.getMessage()),
          ResponseMetadata.emptyResponseCrudInfo(),
          ResponseMetadata.emptyResponsePageInfo());
    }
    return ResponseMetadata.emptyResponseMetadata();
  }
}
