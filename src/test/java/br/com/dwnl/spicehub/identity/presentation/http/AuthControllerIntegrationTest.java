package br.com.dwnl.spicehub.identity.presentation.http;

import br.com.dwnl.spicehub.config.PostgresTestContainerConfig;
import br.com.dwnl.spicehub.config.RedisTestContainerConfig;
import br.com.dwnl.spicehub.identity.domain.model.Email;
import br.com.dwnl.spicehub.identity.domain.model.RoleName;
import br.com.dwnl.spicehub.identity.domain.model.User;
import br.com.dwnl.spicehub.identity.domain.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockCookie;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.ObjectMapper;

import java.util.concurrent.*;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Import({
        PostgresTestContainerConfig.class,
        RedisTestContainerConfig.class
})
class AuthControllerIntegrationTest {

    private final MockMvc mockMvc;

    private final UserRepository userRepository;

    private final ObjectMapper objectMapper;

    @Autowired
    AuthControllerIntegrationTest(MockMvc mockMvc, UserRepository userRepository, ObjectMapper objectMapper) {
        this.mockMvc = mockMvc;
        this.userRepository = userRepository;
        this.objectMapper = objectMapper;
    }

    @Test
    void shouldRegisterUserSuccessfully() throws Exception {

        String requestBody = """
                {
                    "name": "Daniel Rodrigues",
                    "email": "daniel@spicehub.com",
                    "password": "Daniel@123"
                }
                """;

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.name").value("Daniel Rodrigues"))
                .andExpect(jsonPath("$.email").value("daniel@spicehub.com"));

        User user = userRepository.findByEmail(new Email("daniel@spicehub.com")).orElseThrow();
        assertEquals("Daniel Rodrigues", user.getName());
        assertEquals("daniel@spicehub.com", user.getEmail().value());
        assertTrue(user.isEnabled());
        assertTrue(user.getRoles().contains(RoleName.USER));
    }

    @Test
    void shouldReturnConflictWhenEmailAlreadyExists() throws Exception {

        String requestBody = """
                {
                    "name": "Daniel Rodrigues",
                    "email": "duplicate@spicehub.com",
                    "password": "Daniel@123"
                }
                """;

        mockMvc.perform(post("/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                .andExpect(status().isConflict())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.title").isNotEmpty())
                .andExpect(jsonPath("$.detail").isNotEmpty())
                .andExpect(jsonPath("$.instance").value("/auth/register"))
                .andExpect(jsonPath("$.timestamp").isNotEmpty());
    }

    void shouldTreatEmailAsCaseInsensitiveWhenRegistering() throws Exception {

        String firstRequest = """
                {
                    "name": "Daniel Rodrigues",
                    "email": "Daniel@spicehub.com",
                    "password": "Daniel@123"
                }
                """;

        String secondRequest = """
                {
                    "name": "Daniel Rodrigues",
                    "email": "daniel@spicehub.com",
                    "password": "Daniel@123"
                }
                """;

        mockMvc.perform(post("/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(firstRequest))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("daniel@spicehub.com"));

        mockMvc.perform(post("/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(secondRequest))
                .andExpect(status().isConflict())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_PROBLEM_JSON
                ))
                .andExpect(jsonPath("$.status").value(409));
    }

    void shouldLoginSuccessfully() throws Exception {

        String registerRequest = """
                {
                    "name": "Login User",
                    "email": "daniel@spicehub.com",
                    "password": "Daniel@123"
                }
                """;

        mockMvc.perform(post("/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(registerRequest))
                .andExpect(status().isCreated());

        String loginRequest = """
                {
                    "email": "daniel@spicehub.com",
                    "password": "Daniel@123"
                }
                """;

        mockMvc.perform(post("/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(loginRequest))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON
                ))
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(cookie().exists("refresh_token"))
                .andExpect(cookie().httpOnly("refresh_token", true))
                .andExpect(cookie().path("refresh_token", "/auth"));
    }

    @Test
    void shouldReturnUnauthorizedWhenPasswordIsIncorrect() throws Exception {

        String registerRequest = """
            {
                "name": "Wrong Password User",
                "email": "wrongpassword@spicehub.com",
                "password": "Daniel@123"
            }
            """;

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerRequest))
                .andExpect(status().isCreated());

        String loginRequest = """
            {
                "email": "wrongpassword@spicehub.com",
                "password": "WrongPassword@123"
            }
            """;

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequest))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_PROBLEM_JSON
                ))
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.title").isNotEmpty())
                .andExpect(jsonPath("$.detail").isNotEmpty())
                .andExpect(jsonPath("$.instance").value("/auth/login"))
                .andExpect(jsonPath("$.timestamp").isNotEmpty())
                .andExpect(cookie().doesNotExist("refresh_token"));
    }

    @Test
    void shouldReturnUnauthorizedWhenUserDoesNotExist() throws Exception {

        String loginRequest = """
            {
                "email": "nonexistent@spicehub.com",
                "password": "Daniel@123"
            }
            """;

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequest))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_PROBLEM_JSON
                ))
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.title").isNotEmpty())
                .andExpect(jsonPath("$.detail").isNotEmpty())
                .andExpect(jsonPath("$.instance").value("/auth/login"))
                .andExpect(jsonPath("$.timestamp").isNotEmpty())
                .andExpect(cookie().doesNotExist("refresh_token"));
    }

    @Test
    void shouldRefreshAccessTokenSuccessfully() throws Exception {

        String registerRequest = """
            {
                "name": "Refresh User",
                "email": "refresh@spicehub.com",
                "password": "Daniel@123"
            }
            """;

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerRequest))
                .andExpect(status().isCreated());

        String loginRequest = """
            {
                "email": "refresh@spicehub.com",
                "password": "Daniel@123"
            }
            """;

        MvcResult loginResult = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequest))
                .andExpect(status().isOk())
                .andExpect(cookie().exists("refresh_token"))
                .andReturn();

        String refreshToken = loginResult
                .getResponse()
                .getCookie("refresh_token")
                .getValue();

        MockCookie refreshCookie = new MockCookie(
                "refresh_token",
                refreshToken
        );

        refreshCookie.setPath("/auth");
        refreshCookie.setHttpOnly(true);

        mockMvc.perform(post("/auth/refresh")
                        .cookie(refreshCookie)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON
                ))
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(cookie().exists("refresh_token"))
                .andExpect(cookie().httpOnly("refresh_token", true))
                .andExpect(cookie().path("refresh_token", "/auth"));
    }

    @Test
    void shouldRotateRefreshTokenAndRejectReusedToken() throws Exception {

        String registerRequest = """
            {
                "name": "Refresh Rotation User",
                "email": "rotation@spicehub.com",
                "password": "Daniel@123"
            }
            """;

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerRequest))
                .andExpect(status().isCreated());

        String loginRequest = """
            {
                "email": "rotation@spicehub.com",
                "password": "Daniel@123"
            }
            """;

        MvcResult loginResult = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequest))
                .andExpect(status().isOk())
                .andExpect(cookie().exists("refresh_token"))
                .andReturn();

        String refreshTokenA = loginResult
                .getResponse()
                .getCookie("refresh_token")
                .getValue();

        MockCookie refreshCookieA = new MockCookie(
                "refresh_token",
                refreshTokenA
        );

        refreshCookieA.setPath("/auth");
        refreshCookieA.setHttpOnly(true);

        MvcResult firstRefreshResult = mockMvc.perform(post("/auth/refresh")
                        .cookie(refreshCookieA)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(cookie().exists("refresh_token"))
                .andReturn();

        String refreshTokenB = firstRefreshResult
                .getResponse()
                .getCookie("refresh_token")
                .getValue();

        assertNotEquals(refreshTokenA, refreshTokenB);

        MockCookie reusedRefreshCookieA = new MockCookie(
                "refresh_token",
                refreshTokenA
        );

        reusedRefreshCookieA.setPath("/auth");
        reusedRefreshCookieA.setHttpOnly(true);

        mockMvc.perform(post("/auth/refresh")
                        .cookie(reusedRefreshCookieA)
                        .with(csrf()))
                .andExpect(status().isUnauthorized());

        MockCookie refreshCookieB = new MockCookie(
                "refresh_token",
                refreshTokenB
        );

        refreshCookieB.setPath("/auth");
        refreshCookieB.setHttpOnly(true);

        mockMvc.perform(post("/auth/refresh")
                        .cookie(refreshCookieB)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(cookie().exists("refresh_token"));
    }

    @Test
    void shouldLogoutSuccessfullyAndInvalidateRefreshToken() throws Exception {

        String registerRequest = """
            {
                "name": "Logout User",
                "email": "logout@spicehub.com",
                "password": "Daniel@123"
            }
            """;

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerRequest))
                .andExpect(status().isCreated());

        String loginRequest = """
            {
                "email": "logout@spicehub.com",
                "password": "Daniel@123"
            }
            """;

        MvcResult loginResult = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequest))
                .andExpect(status().isOk())
                .andExpect(cookie().exists("refresh_token"))
                .andReturn();

        String refreshToken = loginResult
                .getResponse()
                .getCookie("refresh_token")
                .getValue();

        MockCookie refreshCookie = new MockCookie(
                "refresh_token",
                refreshToken
        );

        refreshCookie.setPath("/auth");
        refreshCookie.setHttpOnly(true);

        mockMvc.perform(post("/auth/logout")
                        .cookie(refreshCookie)
                        .with(csrf()))
                .andExpect(status().isNoContent())
                .andExpect(cookie().exists("refresh_token"))
                .andExpect(cookie().maxAge("refresh_token", 0))
                .andExpect(cookie().httpOnly("refresh_token", true))
                .andExpect(cookie().path("refresh_token", "/auth"));

        MockCookie revokedRefreshCookie = new MockCookie(
                "refresh_token",
                refreshToken
        );

        revokedRefreshCookie.setPath("/auth");
        revokedRefreshCookie.setHttpOnly(true);

        mockMvc.perform(post("/auth/refresh")
                        .cookie(revokedRefreshCookie)
                        .with(csrf()))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_PROBLEM_JSON
                ))
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.title").value("Invalid refresh token"))
                .andExpect(jsonPath("$.detail")
                        .value("Invalid or expired refresh token"));
    }

    @Test
    void shouldRejectRefreshWithoutCsrfToken() throws Exception {

        String registerRequest = """
            {
                "name": "CSRF User",
                "email": "csrf@spicehub.com",
                "password": "Daniel@123"
            }
            """;

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerRequest))
                .andExpect(status().isCreated());

        String loginRequest = """
            {
                "email": "csrf@spicehub.com",
                "password": "Daniel@123"
            }
            """;

        MvcResult loginResult = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequest))
                .andExpect(status().isOk())
                .andExpect(cookie().exists("refresh_token"))
                .andReturn();

        String refreshToken = loginResult
                .getResponse()
                .getCookie("refresh_token")
                .getValue();

        MockCookie refreshCookie = new MockCookie(
                "refresh_token",
                refreshToken
        );

        refreshCookie.setPath("/auth");
        refreshCookie.setHttpOnly(true);

        mockMvc.perform(post("/auth/refresh")
                        .cookie(refreshCookie))
                .andExpect(status().isForbidden())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_PROBLEM_JSON
                ))
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.title").value("Forbidden"))
                .andExpect(jsonPath("$.detail")
                        .value("Invalid or missing CSRF token"));
    }

    @Test
    void shouldRejectLogoutWithoutCsrfToken() throws Exception {

        String registerRequest = """
            {
                "name": "Logout CSRF User",
                "email": "logout.csrf@spicehub.com",
                "password": "Daniel@123"
            }
            """;

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerRequest))
                .andExpect(status().isCreated());

        String loginRequest = """
            {
                "email": "logout.csrf@spicehub.com",
                "password": "Daniel@123"
            }
            """;

        MvcResult loginResult = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequest))
                .andExpect(status().isOk())
                .andExpect(cookie().exists("refresh_token"))
                .andReturn();

        String refreshToken = loginResult
                .getResponse()
                .getCookie("refresh_token")
                .getValue();

        MockCookie refreshCookie = new MockCookie(
                "refresh_token",
                refreshToken
        );

        refreshCookie.setPath("/auth");
        refreshCookie.setHttpOnly(true);


        mockMvc.perform(post("/auth/logout")
                        .cookie(refreshCookie))
                .andExpect(status().isForbidden())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_PROBLEM_JSON
                ))
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.title").value("Forbidden"))
                .andExpect(jsonPath("$.detail")
                        .value("Invalid or missing CSRF token"));


        mockMvc.perform(post("/auth/refresh")
                        .cookie(refreshCookie)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty());
    }

    @Test
    void shouldReturnUnauthorizedWhenAccessingMeWithoutAccessToken() throws Exception {

        mockMvc.perform(get("/users/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_PROBLEM_JSON
                ))
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void shouldReturnAuthenticatedUserWhenAccessingMeWithValidAccessToken() throws Exception {

        String registerRequest = """
            {
                "name": "Authenticated User",
                "email": "authenticated@spicehub.com",
                "password": "Daniel@123"
            }
            """;

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerRequest))
                .andExpect(status().isCreated());

        String loginRequest = """
            {
                "email": "authenticated@spicehub.com",
                "password": "Daniel@123"
            }
            """;

        MvcResult loginResult = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andReturn();

        String accessToken = objectMapper
                .readTree(loginResult.getResponse().getContentAsString())
                .get("accessToken")
                .asText();

        mockMvc.perform(get("/users/me")
                        .header(
                                "Authorization",
                                "Bearer " + accessToken
                        ))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON
                ))
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.email")
                        .value("authenticated@spicehub.com"))
                .andExpect(jsonPath("$.roles[0]")
                        .value("USER"));
    }

    @Test
    void shouldReturnUnauthorizedWhenAccessTokenIsInvalid() throws Exception {

        String invalidAccessToken = "invalid.jwt.token";

        mockMvc.perform(get("/users/me")
                        .header(
                                "Authorization",
                                "Bearer " + invalidAccessToken
                        ))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_PROBLEM_JSON
                ))
                .andExpect(jsonPath("$.status").value(401));
    }


    @Test
    void shouldReturnUnauthorizedWhenDisabledUserTriesToLogin() throws Exception {

        String registerRequest = """
            {
                "name": "Disabled User",
                "email": "disabled@spicehub.com",
                "password": "Daniel@123"
            }
            """;

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerRequest))
                .andExpect(status().isCreated());

        User user = userRepository
                .findByEmail(new Email("disabled@spicehub.com"))
                .orElseThrow();

        user.disable();

        userRepository.save(user);

        String loginRequest = """
            {
                "email": "disabled@spicehub.com",
                "password": "Daniel@123"
            }
            """;

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequest))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_PROBLEM_JSON
                ))
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(cookie().doesNotExist("refresh_token"));
    }


    @Test
    void shouldAllowOnlyOneConcurrentRefreshForSameToken() throws Exception {

        String registerRequest = """
            {
                "name": "Concurrent Refresh User",
                "email": "concurrent@spicehub.com",
                "password": "Daniel@123"
            }
            """;

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerRequest))
                .andExpect(status().isCreated());

        String loginRequest = """
            {
                "email": "concurrent@spicehub.com",
                "password": "Daniel@123"
            }
            """;

        MvcResult loginResult = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequest))
                .andExpect(status().isOk())
                .andExpect(cookie().exists("refresh_token"))
                .andReturn();

        String refreshToken = loginResult
                .getResponse()
                .getCookie("refresh_token")
                .getValue();

        ExecutorService executor = Executors.newFixedThreadPool(2);

        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);

        Callable<Integer> refreshRequest = () -> {

            MockCookie refreshCookie = new MockCookie(
                    "refresh_token",
                    refreshToken
            );

            refreshCookie.setPath("/auth");
            refreshCookie.setHttpOnly(true);

            ready.countDown();

            start.await();

            try {
                return mockMvc.perform(post("/auth/refresh")
                                .cookie(refreshCookie)
                                .with(csrf()))
                        .andReturn()
                        .getResponse()
                        .getStatus();
            } catch (Exception exception) {
                throw new RuntimeException(exception);
            }
        };

        try {

            Future<Integer> firstRequest =
                    executor.submit(refreshRequest);

            Future<Integer> secondRequest =
                    executor.submit(refreshRequest);

            ready.await();

            start.countDown();

            int firstStatus = firstRequest.get();
            int secondStatus = secondRequest.get();

            long successfulRequests = Stream
                    .of(firstStatus, secondStatus)
                    .filter(status -> status == 200)
                    .count();

            long unauthorizedRequests = Stream
                    .of(firstStatus, secondStatus)
                    .filter(status -> status == 401)
                    .count();

            assertEquals(1, successfulRequests);
            assertEquals(1, unauthorizedRequests);

        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    void shouldReturnUnauthorizedWhenAuthenticatedUserNoLongerExists() throws Exception {

        String registerRequest = """
        {
            "name": "Deleted User",
            "email": "deleted@gmail.com",
            "password": "Daniel@123"
        }
        """;

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerRequest))
                .andExpect(status().isCreated());

        String loginRequest = """
        {
            "email": "deleted@gmail.com",
            "password": "Daniel@123"
        }
        """;

        MvcResult loginResult = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequest))
                .andExpect(status().isOk())
                .andReturn();

        String accessToken = objectMapper
                .readTree(loginResult.getResponse().getContentAsString())
                .get("accessToken")
                .asText();

        User user = userRepository
                .findByEmail(new Email("deleted@gmail.com"))
                .orElseThrow();


    }

    @Test
    void shouldReturnUnauthorizedWhenUserIsDisabledAfterAccessTokenWasIssued() throws Exception {
        String email = "disabled.after.login@gmail.com";
        String password = "Password123";

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "name": "Disabled User",
                              "email": "%s",
                              "password": "%s"
                            }
                            """.formatted(email, password)))
                .andExpect(status().isCreated());

        MvcResult loginResult = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "email": "%s",
                              "password": "%s"
                            }
                            """.formatted(email, password)))
                .andExpect(status().isOk())
                .andReturn();

        String accessToken = objectMapper
                .readTree(loginResult.getResponse().getContentAsString())
                .get("accessToken")
                .asText();

        User user = userRepository.findByEmail(new Email(email))
                .orElseThrow();

        user.disable();
        userRepository.save(user);

        mockMvc.perform(get("/users/me")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("Unauthorized"))
                .andExpect(jsonPath("$.detail").value("User account is disabled"));
    }

}