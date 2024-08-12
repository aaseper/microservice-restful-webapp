package eolopark.server.controller.auth;

import eolopark.server.security.jwt.AuthResponse;
import eolopark.server.security.jwt.LoginRequest;
import eolopark.server.security.jwt.UserLoginService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping ("/api/auth")
public class LoginController {

    /* Attributes */
    private final UserLoginService userLoginService;

    /* Constructor */
    LoginController (UserLoginService userLoginService) {
        this.userLoginService = userLoginService;
    }

    /* Methods */
    @Operation (summary = "Log in")
    @ApiResponses (value = {@ApiResponse (responseCode = "200", description = "Log in successful", content =
            {@Content (mediaType = "application/json")})})
    @PostMapping ("/login")
    public ResponseEntity<AuthResponse> login (@Parameter (description = "Access token to fill") @CookieValue (name =
            "accessToken", required = false) String accessToken, @Parameter (description = "Refresh token to refresh " +
            "authentication") @CookieValue (name = "refreshToken", required = false) String refreshToken,
                                               @Parameter (description = "Request to log in (security)") @RequestBody LoginRequest loginRequest) throws IllegalArgumentException {
        return userLoginService.login(loginRequest, accessToken, refreshToken);
    }

    @Operation (summary = "Refresh token")
    @ApiResponses (value = {@ApiResponse (responseCode = "200", description = "Token refreshed successful", content =
            {@Content (mediaType = "application/json")})})
    @PostMapping ("/refresh")
    public ResponseEntity<AuthResponse> refreshToken (@Parameter (description = "Refresh token to refresh " +
            "authentication") @CookieValue (name = "refreshToken", required = false) String refreshToken) {

        return userLoginService.refresh(refreshToken);
    }

    @Operation (summary = "Log out")
    @ApiResponses (value = {@ApiResponse (responseCode = "200", description = "Log out successful", content =
            {@Content (mediaType = "application/json")})})
    @PostMapping ("/logout")
    public ResponseEntity<AuthResponse> logOut (@Parameter (description = "Request content") HttpServletRequest request, @Parameter (description = "Response content") HttpServletResponse response) {

        return ResponseEntity.ok(new AuthResponse(AuthResponse.Status.SUCCESS, userLoginService.logout(request, response)));
    }
}
