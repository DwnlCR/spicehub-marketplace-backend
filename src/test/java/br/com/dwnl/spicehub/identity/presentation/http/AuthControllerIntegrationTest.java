package br.com.dwnl.spicehub.identity.presentation.http;

import br.com.dwnl.spicehub.config.PostgresTestContainerConfig;
import br.com.dwnl.spicehub.config.RedisTestContainerConfig;
import br.com.dwnl.spicehub.identity.application.exception.EmailSendingException;
import br.com.dwnl.spicehub.identity.application.port.EmailVerificationEmailSender;
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
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.ObjectMapper;

import java.util.concurrent.*;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
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

    @MockitoBean
    private EmailVerificationEmailSender emailVerificationEmailSender;

    private final MockMvc mockMvc;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    @Autowired
    AuthControllerIntegrationTest(
            MockMvc mockMvc,
            UserRepository userRepository,
            ObjectMapper objectMapper
    ) {
        this.mockMvc = mockMvc;
        this.userRepository = userRepository;
        this.objectMapper = objectMapper;
    }

    @Test
    void shouldRegisterUserSuccessfully() throws Exception {

        String requestBody = """
                {
                    "name": "Daniel Rodrigues",
                    "email": "daniel@gmail.com",
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
                .andExpect(jsonPath("$.email").value("daniel@gmail.com"))
                .andExpect(jsonPath("$.verificationEmailSent").value(true));

        User user = userRepository
                .findByEmail(new Email("daniel@gmail.com"))
                .orElseThrow();

        assertEquals("Daniel Rodrigues", user.getName());
        assertEquals("daniel@gmail.com", user.getEmail().value());
        assertTrue(user.isEnabled());
        assertFalse(user.isEmailVerified());
        assertTrue(user.getRoles().contains(RoleName.USER));

        verify(emailVerificationEmailSender)
                .send(
                        eq(new Email("daniel@gmail.com")),
                        any(String.class)
                );
    }

    @Test
    void shouldRegisterUserWhenVerificationEmailSendingFails() throws Exception {

        doThrow(new EmailSendingException(
                "Failed to send email verification code",
                new RuntimeException("Email provider unavailable")
        ))
                .when(emailVerificationEmailSender)
                .send(
                        any(Email.class),
                        any(String.class)
                );

        String requestBody = """
            {
                "name": "Email Failure User",
                "email": "email.failure@gmail.com",
                "password": "Daniel@123"
            }
            """;

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.name").value("Email Failure User"))
                .andExpect(jsonPath("$.email").value("email.failure@gmail.com"))
                .andExpect(jsonPath("$.roles[0]").value("USER"))
                .andExpect(jsonPath("$.verificationEmailSent").value(false));

        User user = userRepository
                .findByEmail(new Email("email.failure@gmail.com"))
                .orElseThrow();

        assertEquals("Email Failure User", user.getName());
        assertEquals("email.failure@gmail.com", user.getEmail().value());
        assertTrue(user.isEnabled());
        assertFalse(user.isEmailVerified());
        assertTrue(user.getRoles().contains(RoleName.USER));

        verify(emailVerificationEmailSender)
                .send(
                        eq(new Email("email.failure@gmail.com")),
                        any(String.class)
                );
    }

    @Test
    void shouldReturnConflictWhenEmailAlreadyExists() throws Exception {

        String requestBody = """
                {
                    "name": "Daniel Rodrigues",
                    "email": "duplicate@gmail.com",
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

    @Test
    void shouldTreatEmailAsCaseInsensitiveWhenRegistering() throws Exception {

        String firstRequest = """
            {
                "name": "Daniel Rodrigues",
                "email": "CaseInsensitive@gmail.com",
                "password": "Daniel@123"
            }
            """;

        String secondRequest = """
            {
                "name": "Daniel Rodrigues",
                "email": "caseinsensitive@gmail.com",
                "password": "Daniel@123"
            }
            """;

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(firstRequest))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email")
                        .value("caseinsensitive@gmail.com"));

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(secondRequest))
                .andExpect(status().isConflict())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_PROBLEM_JSON
                ))
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void shouldLoginSuccessfully() throws Exception {

        String registerRequest = """
                {
                    "name": "Login User",
                    "email": "login.success@gmail.com",
                    "password": "Daniel@123"
                }
                """;

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerRequest))
                .andExpect(status().isCreated());

        verifyUserEmail("login.success@gmail.com");

        String loginRequest = """
                {
                    "email": "login.success@gmail.com",
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
    void shouldReturnForbiddenWhenEmailIsNotVerified() throws Exception {

        String email = "unverified@gmail.com";
        String password = "Daniel@123";

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Unverified User",
                                  "email": "%s",
                                  "password": "%s"
                                }
                                """.formatted(email, password)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "%s",
                                  "password": "%s"
                                }
                                """.formatted(email, password)))
                .andExpect(status().isForbidden())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_PROBLEM_JSON
                ))
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.title").value("Email not verified"))
                .andExpect(jsonPath("$.detail")
                        .value("Email address has not been verified"))
                .andExpect(cookie().doesNotExist("refresh_token"));
    }

    @Test
    void shouldReturnUnauthorizedWhenPasswordIsIncorrect() throws Exception {

        String registerRequest = """
                {
                    "name": "Wrong Password User",
                    "email": "wrongpassword@gmail.com",
                    "password": "Daniel@123"
                }
                """;

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerRequest))
                .andExpect(status().isCreated());

        String loginRequest = """
                {
                    "email": "wrongpassword@gmail.com",
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
                    "email": "nonexistent@gmail.com",
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
                    "email": "refresh@gmail.com",
                    "password": "Daniel@123"
                }
                """;

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerRequest))
                .andExpect(status().isCreated());

        verifyUserEmail("refresh@gmail.com");

        String loginRequest = """
                {
                    "email": "refresh@gmail.com",
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
                    "email": "rotation@gmail.com",
                    "password": "Daniel@123"
                }
                """;

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerRequest))
                .andExpect(status().isCreated());

        verifyUserEmail("rotation@gmail.com");

        String loginRequest = """
                {
                    "email": "rotation@gmail.com",
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
                    "email": "logout@gmail.com",
                    "password": "Daniel@123"
                }
                """;

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerRequest))
                .andExpect(status().isCreated());

        verifyUserEmail("logout@gmail.com");

        String loginRequest = """
                {
                    "email": "logout@gmail.com",
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
                    "email": "csrf@gmail.com",
                    "password": "Daniel@123"
                }
                """;

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerRequest))
                .andExpect(status().isCreated());

        verifyUserEmail("csrf@gmail.com");

        String loginRequest = """
                {
                    "email": "csrf@gmail.com",
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
                    "email": "logout.csrf@gmail.com",
                    "password": "Daniel@123"
                }
                """;

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerRequest))
                .andExpect(status().isCreated());

        verifyUserEmail("logout.csrf@gmail.com");

        String loginRequest = """
                {
                    "email": "logout.csrf@gmail.com",
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
                    "email": "authenticated@gmail.com",
                    "password": "Daniel@123"
                }
                """;

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerRequest))
                .andExpect(status().isCreated());

        verifyUserEmail("authenticated@gmail.com");

        String loginRequest = """
                {
                    "email": "authenticated@gmail.com",
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
                        .value("authenticated@gmail.com"))
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
                    "email": "disabled@gmail.com",
                    "password": "Daniel@123"
                }
                """;

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerRequest))
                .andExpect(status().isCreated());

        User user = userRepository
                .findByEmail(new Email("disabled@gmail.com"))
                .orElseThrow();

        user.disable();

        userRepository.save(user);

        String loginRequest = """
                {
                    "email": "disabled@gmail.com",
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
                    "email": "concurrent@gmail.com",
                    "password": "Daniel@123"
                }
                """;

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerRequest))
                .andExpect(status().isCreated());

        verifyUserEmail("concurrent@gmail.com");

        String loginRequest = """
                {
                    "email": "concurrent@gmail.com",
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

        verifyUserEmail("deleted@gmail.com");

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

        assertNotNull(accessToken);
        assertNotNull(user);
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

        verifyUserEmail(email);

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

        User user = userRepository
                .findByEmail(new Email(email))
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

    private void verifyUserEmail(String email) {
        User user = userRepository
                .findByEmail(new Email(email))
                .orElseThrow();

        user.verifyEmail();

        userRepository.save(user);
    }
}