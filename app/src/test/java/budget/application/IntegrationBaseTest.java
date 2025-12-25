package budget.application;

import budget.application.common.Constants;
import budget.application.server.core.ServerNetty;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

public abstract class IntegrationBaseTest {

  protected static ServerNetty server;
  protected static int port;

  @BeforeAll
  static void beforeAll() throws Exception {
    setSystemEnvPropertyTestData();
    TestDataSource.start();
    TestDataSource.flywayMigrate(TestDataSource.getDataSource());

    server = new ServerNetty(TestDataSource.getDataSource());

    server.start();

    port = server.getBoundPort();
  }

  @AfterAll
  static void afterAll() {
    server.stop();
  }

  private static void setSystemEnvPropertyTestData() {
    Constants.ENV_KEY_NAMES.forEach(
        env -> {
          if (!(Constants.ENV_SERVER_PORT.equals(env)
              || Constants.SPRING_PROFILES_ACTIVE.equals(env))) {
            System.setProperty(env, env);
          }
        });
    System.setProperty(Constants.SPRING_PROFILES_ACTIVE, Constants.TESTING_ENV);
    System.setProperty(Constants.ENV_SERVER_PORT, Constants.ENV_PORT_DEFAULT);
  }

  private final String basicAuthCredentialsForTest =
      Base64.getEncoder()
          .encodeToString(
              String.format("%s:%s", Constants.ENV_SELF_USERNAME, Constants.ENV_SELF_PASSWORD)
                  .getBytes(StandardCharsets.UTF_8));

  protected HttpResponse<String> httpGet(String path) throws Exception {
    HttpRequest req =
        HttpRequest.newBuilder()
            .header("Authorization", "Basic " + basicAuthCredentialsForTest)
            .uri(URI.create("http://localhost:" + port + path))
            .GET()
            .build();

    return HttpClient.newHttpClient().send(req, HttpResponse.BodyHandlers.ofString());
  }

  protected HttpResponse<String> httpPost(String path, String json) throws Exception {
    HttpRequest req =
        HttpRequest.newBuilder()
            .uri(URI.create("http://localhost:" + port + path))
            .header("Content-Type", "application/json")
            .header("Authorization", "Basic " + basicAuthCredentialsForTest)
            .POST(HttpRequest.BodyPublishers.ofString(json))
            .build();

    return HttpClient.newHttpClient().send(req, HttpResponse.BodyHandlers.ofString());
  }
}
