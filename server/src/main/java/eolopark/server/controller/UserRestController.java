package eolopark.server.controller;

import eolopark.server.model.dto.*;
import eolopark.server.model.internal.Aerogenerator;
import eolopark.server.model.internal.Eolopark;
import eolopark.server.model.internal.User;
import eolopark.server.service.ParkService;
import eolopark.server.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping ("/api/users")
public class UserRestController {

    /* Attributes */
    private final UserService userService;
    private final ParkService parkService;
    private final PasswordEncoder passwordEncoder;

    /* Constructor */
    UserRestController (UserService userService, ParkService parkService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.parkService = parkService;
        this.passwordEncoder = passwordEncoder;
    }

    @Operation (summary = "Get current user")
    @ApiResponses (value = {@ApiResponse (responseCode = "200", description = "Return the page of the logged user",
            content = {@Content (mediaType = "application/json", schema = @Schema (implementation =
                    UserNameResponse.class))}), @ApiResponse (responseCode = "403", description = "Access Denied",
            content = @Content), @ApiResponse (responseCode = "401", description = "Full authentication is required " +
            "to access this resource", content = @Content)})
    @GetMapping ("/me")
    public ResponseEntity<UserNameResponse> me (@Parameter (description = "HTTP Servlet request containing user " +
            "information, including role details") HttpServletRequest request) {
        Principal principal = request.getUserPrincipal();
        if (principal != null)
            return ResponseEntity.ok(new UserNameResponse(userService.getUserByName(principal.getName()).getName()));
        else throw new InsufficientAuthenticationException("You are not logged in");
    }

    @Operation (summary = "Create a new user")
    @ApiResponses (value = {@ApiResponse (responseCode = "201", description = "New user created", content =
            {@Content (mediaType = "application/json", schema = @Schema (implementation = UserNameResponse.class))}),
            @ApiResponse (responseCode = "500", description = "A user with the same id is already registered",
                    content = @Content)})
    /* Methods */
    @PostMapping ("/register")
    public ResponseEntity<UserNameResponse> signin (@Parameter (description = "Request body containing user " +
            "registration details") @RequestBody UserRequest userRequest) {
        userService.saveUser(new User(userRequest.username(), passwordEncoder.encode(userRequest.password())));
        User user = userService.getUserByName(userRequest.username());
        URI location = URI.create("/api/users/" + user.getId());
        return ResponseEntity.created(location).body(new UserNameResponse(user.getName()));
    }

    @Operation (summary = "Get all users")
    @ApiResponses (value = {@ApiResponse (responseCode = "200", description = "Return the list of user with their " +
            "roles", content = {@Content (mediaType = "application/json", schema = @Schema (implementation =
            UserIdAndNameAndRolesResponse.class))}), @ApiResponse (responseCode = "401", description = "Full " +
            "authentication is required to access this resource", content = @Content), @ApiResponse (responseCode =
            "403", description = "Access Denied", content = @Content)})
    @GetMapping ("/")
    public ResponseEntity<List<UserIdAndNameAndRolesResponse>> getAllUser (@Parameter (description = "Pageable object" +
            " for pagination") Pageable page) {
        return ResponseEntity.ok(userService.getAllUser(page).stream().map((User u) -> new UserIdAndNameAndRolesResponse(u.getId(), u.getName(), u.getRoles())).toList());
    }


    @Operation (summary = "Update user by ID")
    @ApiResponses (value = {@ApiResponse (responseCode = "200", description = "User modified", content =
            {@Content (mediaType = "application/json", schema = @Schema (implementation = UserNameResponse.class))}),
            @ApiResponse (responseCode = "401", description = "Full authentication is required to access this " +
                    "resource", content = @Content), @ApiResponse (responseCode = "403", description = "Access " +
            "Denied", content = @Content), @ApiResponse (responseCode = "404", description = "The requested resource " +
            "could not be found", content = @Content)})
    @PutMapping ("/{id}")
    public ResponseEntity<UserNameResponse> putUser (@Parameter (description = "ID of the user to update") @PathVariable Long id) {
        userService.replaceUserById(id);
        return ResponseEntity.ok(new UserNameResponse(userService.getUserById(id).getName()));
    }

    @Operation (summary = "Delete user by ID")
    @ApiResponses (value = {@ApiResponse (responseCode = "200", description = "User deleted", content =
            {@Content (mediaType = "application/json", schema = @Schema (implementation = UserNameResponse.class))}),
            @ApiResponse (responseCode = "401", description = "Full authentication is required to access this " +
                    "resource", content = @Content), @ApiResponse (responseCode = "403", description = "Access " +
            "Denied", content = @Content), @ApiResponse (responseCode = "404", description = "The requested resource " +
            "could not be found", content = @Content)})
    @DeleteMapping ("/{id}")
    public ResponseEntity<UserNameResponse> deleteUser (@Parameter (description = "ID of the user to delete") @PathVariable Long id) {
        User user = userService.getUserById(id);
        userService.deleteUserById(id);
        return ResponseEntity.ok(new UserNameResponse(user.getName()));
    }

    @Operation (summary = "Get user by ID")
    @ApiResponses (value = {@ApiResponse (responseCode = "200", description = "Return the user with the ID given",
            content = {@Content (mediaType = "application/json", schema = @Schema (implementation =
                    UserResponse.class))}), @ApiResponse (responseCode = "401", description = "Full authentication is" +
            " required to access this resource", content = @Content), @ApiResponse (responseCode = "403",
            description = "Access Denied", content = @Content), @ApiResponse (responseCode = "404", description =
            "The requested resource could not be found", content = @Content)})
    @GetMapping ("/{id}")
    public ResponseEntity<UserResponse> getUser (@Parameter (description = "ID of the user to retrieve") @PathVariable Long id, @Parameter (description = "Pageable object for pagination") Pageable page) {
        User user = userService.getUserById(id);
        List<EoloparkResponse> eoloparks =
                parkService.getParkByUserId(id, page).stream().map((Eolopark e) -> new EoloparkResponse(e.getId(),
                        e.getName(), e.getCity(), e.getLatitude(), e.getLongitude(), e.getArea(), e.getTerrainType(),
                        new SubstationResponse(e.getSubstation().getModel(), e.getSubstation().getPower(),
                                e.getSubstation().getVoltage()),
                        e.getAerogenerators().stream().map((Aerogenerator a) -> new AerogeneratorResponse(a.getId(),
                                a.getAerogeneratorId(), a.getAerogeneratorLatitude(), a.getAerogeneratorLongitude(),
                                a.getBladeLength(), a.getHeight(), a.getAerogeneratorPower())).collect(Collectors.toList()))).toList();
        return ResponseEntity.ok(new UserResponse(user.getId(), user.getName(), user.getParkMax(), user.getRoles(),
                eoloparks));
    }
}
