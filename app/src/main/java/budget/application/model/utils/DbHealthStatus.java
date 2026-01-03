package budget.application.model.utils;

public record DbHealthStatus(
    boolean dbHealthy,
    String dbMessage,
    int activeConnections,
    int idleConnections,
    int totalConnections,
    int threadsAwaitingConnection) {}
