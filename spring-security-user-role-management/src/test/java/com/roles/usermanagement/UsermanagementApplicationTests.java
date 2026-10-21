package com.roles.usermanagement;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ActiveProfiles;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * CAMPUSDESK integration test suite.
 * Validates mandatory test cases (CP-01 to CP-16) and API contract.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UsermanagementApplicationTests {

    @Autowired
    private Environment environment;

    private final HttpClient client = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

    private HttpResponse<String> request(String method, String path, String body, String token) throws Exception {
        int port = Integer.parseInt(environment.getProperty("local.server.port", "8080"));
        var builder = HttpRequest.newBuilder(URI.create("http://localhost:" + port + path))
                .header("Content-Type", "application/json");

        if (token != null) {
            builder.header("Authorization", "Bearer " + token);
        }

        builder.method(method, body == null
                ? HttpRequest.BodyPublishers.noBody()
                : HttpRequest.BodyPublishers.ofString(body));

        return client.send(builder.build(), HttpResponse.BodyHandlers.ofString());
    }

    private String login(String identifier, String password) throws Exception {
        String body = String.format("{\"username\":\"%s\",\"password\":\"%s\"}", identifier, password);
        HttpResponse<String> response = request("POST", "/api/auth/login", body, null);
        assertThat(response.statusCode())
                .withFailMessage("Login failed for " + identifier + " with status: " + response.statusCode() + " body: " + response.body())
                .isEqualTo(200);
        JsonNode json = mapper.readTree(response.body());
        return json.get("token").asText();
    }

    @Test
    @Order(1)
    void exposesSwaggerUiAndOpenApiDocs() throws Exception {
        HttpResponse<String> response = request("GET", "/v3/api-docs", null, null);
        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body().toLowerCase()).contains("openapi");
    }

    @Test
    @Order(2)
    void authenticatesBootstrapAdmin() throws Exception {
        String adminToken = login("superadmin", "SuperAdmin123!");
        assertThat(adminToken).isNotBlank();
    }

    @Test
    @Order(3)
    void cp01_registersValidUserWithRoleUser() throws Exception {
        String body = """
                {
                  "username": "usuario_test1",
                  "email": "usuario_test1@technova.com",
                  "password": "Password123!"
                }
                """;
        HttpResponse<String> response = request("POST", "/api/auth/register", body, null);
        assertThat(response.statusCode()).isEqualTo(201);
        JsonNode json = mapper.readTree(response.body());
        assertThat(json.get("role").asText()).isEqualTo("USER");
    }

    @Test
    @Order(4)
    void cp02_rejectsDuplicateEmailWithConflict() throws Exception {
        String body = """
                {
                  "username": "usuario_test2",
                  "email": "usuario_test1@technova.com",
                  "password": "Password123!"
                }
                """;
        HttpResponse<String> response = request("POST", "/api/auth/register", body, null);
        assertThat(response.statusCode()).isEqualTo(409);
    }

    @Test
    @Order(5)
    void cp03_logsInSuccessfullyAndReturnsJwt() throws Exception {
        String token = login("usuario_test1", "Password123!");
        assertThat(token).isNotBlank();
    }

    @Test
    @Order(6)
    void cp04_rejectsProtectedEndpointWithoutToken() throws Exception {
        HttpResponse<String> response = request("GET", "/api/tickets", null, null);
        assertThat(response.statusCode()).isIn(401, 403);
    }

    @Test
    @Order(7)
    void cp05_createsTicketInOpenStatus() throws Exception {
        String userToken = login("usuario_test1", "Password123!");
        String body = """
                {
                  "title": "Falla en monitor de escritorio",
                  "description": "La pantalla no enciende tras conectar cable HDMI",
                  "category": "HARDWARE",
                  "priority": "ALTA"
                }
                """;
        HttpResponse<String> response = request("POST", "/api/tickets", body, userToken);
        assertThat(response.statusCode()).withFailMessage("Response was " + response.statusCode() + " body: " + response.body()).isEqualTo(201);

        JsonNode json = mapper.readTree(response.body());
        assertThat(json.get("status").asText()).isEqualTo("OPEN");
        assertThat(json.get("applicantUsername").asText()).isEqualTo("usuario_test1");
    }

    @Test
    @Order(8)
    void cp06_rejectsViewingOtherUsersTicket() throws Exception {
        // Register user 2
        String bodyUser2 = """
                {
                  "username": "usuario_ajeno",
                  "email": "usuario_ajeno@technova.com",
                  "password": "Password123!"
                }
                """;
        request("POST", "/api/auth/register", bodyUser2, null);
        String user2Token = login("usuario_ajeno", "Password123!");

        // User 2 attempts to query ticket 1 created by usuario_test1
        HttpResponse<String> response = request("GET", "/api/tickets/1", null, user2Token);
        assertThat(response.statusCode()).isEqualTo(403);
    }

    @Test
    @Order(9)
    void cp08_and_cp07_assignTechnicianRolesAndEndpoints() throws Exception {
        String adminToken = login("superadmin", "SuperAdmin123!");

        // Create technician with TECHNICIAN role using /api/user/add
        String createTechBody = """
                {
                  "username": "tecnico_soporte",
                  "email": "tecnico@technova.com",
                  "password": "Password123!",
                  "role": "TECHNICIAN"
                }
                """;
        request("POST", "/api/user/add", createTechBody, adminToken);

        // CP-08: Standard user attempts to assign technician -> 403
        String userToken = login("usuario_test1", "Password123!");
        String assignBody = "{\"technicianUsername\": \"tecnico_soporte\"}";
        HttpResponse<String> userAssign = request("PATCH", "/api/tickets/1/assign", assignBody, userToken);
        assertThat(userAssign.statusCode()).isEqualTo(403);

        // CP-07: Admin assigns technician with PATCH /api/tickets/1/assign -> 200 and ASSIGNED status
        HttpResponse<String> adminAssign = request("PATCH", "/api/tickets/1/assign", assignBody, adminToken);
        assertThat(adminAssign.statusCode()).isEqualTo(200);

        JsonNode json = mapper.readTree(adminAssign.body());
        assertThat(json.get("status").asText()).isEqualTo("ASSIGNED");
    }

    @Test
    @Order(10)
    void cp09_rejectsModificationOfTicketByTechnician() throws Exception {
        String techToken = login("tecnico_soporte", "Password123!");
        String updateBody = """
                {
                  "title": "Intento de modificación no autorizada",
                  "description": "Texto cambiado",
                  "category": "HARDWARE",
                  "priority": "BAJA"
                }
                """;
        HttpResponse<String> response = request("PUT", "/api/tickets/1", updateBody, techToken);
        assertThat(response.statusCode()).isEqualTo(403);
    }

    @Test
    @Order(11)
    void cp10_rejectsResolvingTicketWithoutInProgress() throws Exception {
        String techToken = login("tecnico_soporte", "Password123!");

        // Attempt transition directly from ASSIGNED to RESOLVED (skipping IN_PROGRESS) -> 409
        String body = "{\"action\": \"RESOLVE\"}";
        HttpResponse<String> response = request("POST", "/api/tickets/1/transitions", body, techToken);
        assertThat(response.statusCode()).isEqualTo(409);
    }

    @Test
    @Order(12)
    void cp11_and_cp12_transitionsThroughLifecycleAndClosesTicket() throws Exception {
        String techToken = login("tecnico_soporte", "Password123!");
        String userToken = login("usuario_test1", "Password123!");

        // 1. Start progress (ASSIGNED -> IN_PROGRESS)
        HttpResponse<String> startProgress = request("PATCH", "/api/tickets/1/status", "{\"status\": \"IN_PROGRESS\"}", techToken);
        assertThat(startProgress.statusCode()).isEqualTo(200);

        // 2. Mark as resolved (IN_PROGRESS -> RESOLVED)
        HttpResponse<String> resolve = request("PATCH", "/api/tickets/1/status", "{\"status\": \"RESOLVED\"}", techToken);
        assertThat(resolve.statusCode()).isEqualTo(200);

        // 3. Requester confirms closure (RESOLVED -> CLOSED)
        HttpResponse<String> close = request("PATCH", "/api/tickets/1/status", "{\"status\": \"CLOSED\"}", userToken);
        assertThat(close.statusCode()).isEqualTo(200);

        // CP-11: Verify that changes were recorded in status history
        HttpResponse<String> historyResp = request("GET", "/api/tickets/1/history", null, userToken);
        assertThat(historyResp.statusCode()).isEqualTo(200);
        JsonNode historyJson = mapper.readTree(historyResp.body());
        assertThat(historyJson.isArray()).isTrue();
        assertThat(historyJson.size()).isGreaterThanOrEqualTo(3);
    }

    @Test
    @Order(13)
    void cp13_rejectsCommentsOnClosedTicket() throws Exception {
        String userToken = login("usuario_test1", "Password123!");
        String body = "{\"content\": \"Comentario posterior al cierre\"}";
        HttpResponse<String> response = request("POST", "/api/tickets/1/comments", body, userToken);
        assertThat(response.statusCode()).isEqualTo(400);
    }

    @Test
    @Order(14)
    void cp14_returnsReportsSummaryAndDashboardMetrics() throws Exception {
        String adminToken = login("superadmin", "SuperAdmin123!");

        // RT-03 requires GET /api/reports/summary
        HttpResponse<String> summary = request("GET", "/api/reports/summary", null, adminToken);
        assertThat(summary.statusCode()).isEqualTo(200);

        // /api/dashboard/stats also responds
        HttpResponse<String> stats = request("GET", "/api/dashboard/stats", null, adminToken);
        assertThat(stats.statusCode()).isEqualTo(200);
    }

    @Test
    @Order(15)
    void technicianEndpointsAreAccessible() throws Exception {
        String adminToken = login("superadmin", "SuperAdmin123!");

        // RT-03 requires GET /api/users/technicians
        HttpResponse<String> usersTech = request("GET", "/api/users/technicians", null, adminToken);
        assertThat(usersTech.statusCode()).isEqualTo(200);

        // /api/technicians also responds
        HttpResponse<String> tech = request("GET", "/api/technicians", null, adminToken);
        assertThat(tech.statusCode()).isEqualTo(200);
    }

    @Test
    @Order(16)
    void cp16_rejectsInvalidOrExpiredJwt() throws Exception {
        HttpResponse<String> response = request("GET", "/api/tickets", null, "token_falso_invalido_xyz");
        assertThat(response.statusCode()).isIn(401, 403);
    }
}
