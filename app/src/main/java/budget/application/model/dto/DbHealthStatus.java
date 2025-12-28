package budget.application.model.dto;

public record DbHealthStatus(
    boolean dbHealthy,
    String dbMessage,
    int activeConnections,
    int idleConnections,
    int totalConnections,
    int threadsAwaitingConnection) {}
