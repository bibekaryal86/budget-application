package budget.application.model.dto.response;

public record DbHealthStatus(
    boolean dbHealthy,
    String dbMessage,
    int activeConnections,
    int idleConnections,
    int totalConnections,
    int threadsAwaitingConnection) {}
