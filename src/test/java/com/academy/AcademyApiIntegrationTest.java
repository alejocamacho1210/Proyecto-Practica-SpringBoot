package com.academy;

import com.academy.business.exception.ApiExceptions;
import com.academy.business.service.AcademyService;
import com.academy.persistence.entity.CourseEntity;
import com.academy.persistence.entity.StudentEntity;
import com.academy.security.AuthService;
import com.academy.security.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import tools.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class AcademyApiIntegrationTest {

    @Value("${local.server.port}")
    private int port;

    @Autowired
    private AuthService auth;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private AcademyService academy;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void httpSecurityAllowsPublicHealthAndRestrictsCourseCreation() throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> health = client.send(request("GET", "/internal/health", null, null),
                HttpResponse.BodyHandlers.ofString());
        assertThat(health.statusCode()).isEqualTo(200);

        HttpResponse<String> unauthenticated = client.send(request("GET", "/api/v1/students", null, null),
                HttpResponse.BodyHandlers.ofString());
        assertThat(unauthenticated.statusCode()).isEqualTo(401);

        String loginJson = objectMapper.writeValueAsString(Map.of("username", "ana", "password", "ana123"));
        HttpResponse<String> login = client.send(request("POST", "/api/v1/auth/login", loginJson, null),
                HttpResponse.BodyHandlers.ofString());
        String token = objectMapper.readTree(login.body()).get("token").asString();

        HttpResponse<String> forbidden = client.send(request("POST", "/api/v1/courses",
                "{\"name\":\"Java\",\"price\":100,\"maxCapacity\":10}", token),
                HttpResponse.BodyHandlers.ofString());
        assertThat(forbidden.statusCode()).isEqualTo(403);
    }

    private HttpRequest request(String method, String path, String body, String token) {
        HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create("http://localhost:" + port + path));
        if (token != null) {
            builder.header("Authorization", "Bearer " + token);
        }
        if (body != null) {
            builder.header("Content-Type", "application/json")
                    .method(method, HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8));
        } else {
            builder.method(method, HttpRequest.BodyPublishers.noBody());
        }
        return builder.build();
    }

    @Test
    void loginCreatesSignedTokenWithAccountRole() {
        String token = auth.login("ana", "ana123");
        assertThat(jwtService.parse(token).getSubject()).isEqualTo("ana");
        assertThat(jwtService.parse(token).get("role", String.class)).isEqualTo("USER");
    }

    @Test
    void enrollmentRejectsDuplicatesAndFullCourses() {
        StudentEntity student = academy.createStudent(new StudentEntity("Test Student", 20, "test@mail.com"));
        CourseEntity course = academy.createCourse(new CourseEntity("Test Course", 100, 1));
        academy.enroll(student.getId(), course.getId());

        assertThatThrownBy(() -> academy.enroll(student.getId(), course.getId()))
                .isInstanceOf(ApiExceptions.Conflict.class);
        StudentEntity second = academy.createStudent(new StudentEntity("Other Student", 21, "other@mail.com"));
        assertThatThrownBy(() -> academy.enroll(second.getId(), course.getId()))
                .isInstanceOf(ApiExceptions.Conflict.class);
    }
}