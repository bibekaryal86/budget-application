package budget.application;

import budget.application.common.Constants;
import budget.application.server.core.ServerNetty;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

public abstract class IntegrationBaseTest {

  protected static ServerNetty server;
  protected static int port;
  protected static final UUID TEST_ID = UUID.fromString("5b15fdaf-758b-4c4f-97d1-2405b716867a");

  @BeforeAll
  static void beforeAll() throws Exception {
    setSystemEnvPropertyTestData();
    TestDataSource.start();
    server = new ServerNetty(TestDataSource.getDataSource(), null);
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

  protected HttpResponse<String> httpGet(String path, boolean isIncludeAuth) throws Exception {
    HttpRequest.Builder builder =
        HttpRequest.newBuilder().uri(URI.create("http://localhost:" + port + path)).GET();
    if (isIncludeAuth) {
      builder.header("Authorization", "Basic " + basicAuthCredentialsForTest).build();
    }

    return HttpClient.newHttpClient().send(builder.build(), HttpResponse.BodyHandlers.ofString());
  }

  protected HttpResponse<String> httpPost(String path, String json, boolean isIncludeAuth)
      throws Exception {
    HttpRequest.Builder builder =
        HttpRequest.newBuilder()
            .uri(URI.create("http://localhost:" + port + path))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(json));
    if (isIncludeAuth) {
      builder.header("Authorization", "Basic " + basicAuthCredentialsForTest).build();
    }
    return HttpClient.newHttpClient().send(builder.build(), HttpResponse.BodyHandlers.ofString());
  }

  protected HttpResponse<String> httpPut(String path, String json, boolean isIncludeAuth)
      throws Exception {
    HttpRequest.Builder builder =
        HttpRequest.newBuilder()
            .uri(URI.create("http://localhost:" + port + path))
            .header("Content-Type", "application/json")
            .PUT(HttpRequest.BodyPublishers.ofString(json));
    if (isIncludeAuth) {
      builder.header("Authorization", "Basic " + basicAuthCredentialsForTest).build();
    }
    return HttpClient.newHttpClient().send(builder.build(), HttpResponse.BodyHandlers.ofString());
  }

  protected HttpResponse<String> httpDelete(String path, boolean isIncludeAuth) throws Exception {
    HttpRequest.Builder builder =
        HttpRequest.newBuilder().uri(URI.create("http://localhost:" + port + path)).DELETE();
    if (isIncludeAuth) {
      builder.header("Authorization", "Basic " + basicAuthCredentialsForTest).build();
    }
    return HttpClient.newHttpClient().send(builder.build(), HttpResponse.BodyHandlers.ofString());
  }
}
