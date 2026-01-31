package budget.application.common;

public class Exceptions {
  public static class BadRequestException extends RuntimeException {
    public BadRequestException(String message) {
      super(message);
    }
  }

  public static class NotFoundException extends RuntimeException {
    public NotFoundException(String entity, String column) {
      super(String.format("[%s] Not found for [%s]...", entity, column));
    }
  }
}
