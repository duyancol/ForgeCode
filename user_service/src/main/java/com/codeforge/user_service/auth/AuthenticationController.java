package com.codeforge.user_service.auth;

import com.codeforge.user_service.config.JwtService;
import com.codeforge.user_service.token.Token;
import com.codeforge.user_service.token.TokenRepository;
import com.codeforge.user_service.token.TokenType;
import com.codeforge.user_service.user.Role;
import com.codeforge.user_service.user.User;
import com.codeforge.user_service.user.UserRepository;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.*;

@RestController
@RequestMapping("/api/v1/auth")

@RequiredArgsConstructor
public class AuthenticationController {
    private final UserRepository repository;
    private final AuthenticationService service;
    @Autowired AuthenticationService authenticationService;
    @Autowired
    private final TokenRepository tokenRepository;
    @Autowired
    private final JwtService jwtService;
//    @PostMapping("/google/{id}")
//    public ResponseEntity<AuthenticationResponse> authenticateWithGoogle(@PathVariable("id") String googleIdToken) {
//        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new JacksonFactory())
//                .setAudience(Collections.singletonList("272854032499-uvoh7etrb27k4sp664qd3baj900l703l.apps.googleusercontent.com"))
//                .build();
//
//        try {
//            GoogleIdToken idToken = verifier.verify(googleIdToken);
//            if (idToken != null) {
//                GoogleIdToken.Payload payload = idToken.getPayload();
//
//                String userId = payload.getSubject();
//                String email = payload.getEmail();
//                String fullName = (String) payload.get("name");
//
//                Optional<User> existingUserOptional = repository.findByEmail(email);
//                if (existingUserOptional.isPresent()) {
//                    User existingUser = existingUserOptional.get();
//                    String jwtToken = jwtService.generateToken(existingUser);
//                    return ResponseEntity.ok(
//                            AuthenticationResponse.builder()
//                                    .id(existingUser.getId())
//                                    .token(jwtToken)
//                                    .firstname(existingUser.getFirstname())
//                                    .email(existingUser.getEmail())
//                                    .build()
//                    );
//                } else {
//                    var user = User.builder()
//                            .userId(userId)
//                            .firstname(fullName)
//                            .email(email)
//                            .role(Role.USER)
//                            .build();
//                    var savedUser = repository.save(user);
//                    String jwtToken = jwtService.generateToken(savedUser);
//                    saveUserToken(savedUser, jwtToken);
//
//                    return ResponseEntity.ok(
//                            AuthenticationResponse.builder()
//                                    .id(savedUser.getId())
//                                    .token(jwtToken)
//                                    .firstname(savedUser.getFirstname())
//                                    .email(savedUser.getEmail())
//                                    .build()
//                    );
//                }
//            } else {
//                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
//        }
//    }

@PostMapping("/google")
public ResponseEntity<?> authenticateWithGoogle(
        @RequestBody Map<String, String> body,
        HttpServletRequest request,
        HttpServletResponse response) {

    String googleIdToken = body.get("idToken");

    GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new JacksonFactory())
            .setAudience(Collections.singletonList("272854032499-uvoh7etrb27k4sp664qd3baj900l703l.apps.googleusercontent.com"))
            .build();

    try {
        GoogleIdToken idToken = verifier.verify(googleIdToken);
        if (idToken != null) {
            GoogleIdToken.Payload payload = idToken.getPayload();

            String userId = payload.getSubject();
            String email = payload.getEmail();
            String fullName = (String) payload.get("name");

            User user = repository.findByEmail(email).orElseGet(() -> {
                User newUser = User.builder()
                        .userId(userId)
                        .firstname(fullName)
                        .email(email)
                        .role(Role.USER)
                        .build();
                return repository.save(newUser);
            });

            String clientIp = request.getRemoteAddr();
            String userAgent = request.getHeader("User-Agent");

            String jwtToken = jwtService.generateToken1(new HashMap<>(), user, clientIp, userAgent);
            saveUserToken(user, jwtToken); // vẫn giữ

            ResponseCookie cookie = ResponseCookie.from("access_token", jwtToken)
                    .httpOnly(true)
                    .secure(false)
                    .path("/")
                    .maxAge(Duration.ofMinutes(15))
                    .sameSite("Strict")
                    .build();

            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, cookie.toString())
                    .body(Map.of(
                            "id", user.getId(),
                            "firstname", user.getFirstname(),
                            "email", user.getEmail()
                    ));
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid Google token.");
        }
    } catch (Exception e) {
        e.printStackTrace();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal server error");
    }
}


    private void saveUserToken(User user, String jwtToken) {
        var token = Token.builder()
                .user(user)
                .token(jwtToken)
                .tokenType(TokenType.BEARER)
                .expired(false)
                .revoked(false)
                .build();
        System.out.println(token);
        tokenRepository.save(token);
    }
    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponse> register(
            @RequestBody RegisterRequest request
    ) {
        return ResponseEntity.ok(service.register(request));
    }
    @PostMapping("/authenticate")
    public ResponseEntity<AuthenticationResponse> authenticate(
            @RequestBody AuthenticationRequest request
    ) {
        return ResponseEntity.ok(service.authenticate(request));
    }

    @Autowired
    UserRepository userRepository;
    @PutMapping("/users/updateRole")
    public void updateRole(@RequestParam("email") String email, @RequestParam("role") String role) {
        authenticationService.updateUserRole(email,role);
    }
    @PutMapping("/users/updateUserSetting")
    public void updateUserSetting(@RequestParam("email") String email,
                                  @RequestParam("firstName") String firstName,
                                  @RequestParam("lastName") String lastName,
                                  @RequestParam("phone") String phone,
                                  @RequestParam("chooseaddress") String chooseAddress,
                                  @RequestParam("address") String address) {
        authenticationService.updateUserSetting(email,firstName,lastName,phone,chooseAddress+address);
    }
    @GetMapping("/users/userAll")
    public List<AuthenticationResponse> getUserAll() {
        return  authenticationService.findAll();

    }
    @GetMapping("/users/getByEmail")
    public AuthenticationResponse getUserByID(@RequestParam("email") String email) {
        return  authenticationService.getUserByID(email);

    }

//    @GetMapping("/users/forotPass")
//    public String getForgot(@RequestParam("email") String email) {
//        return  authenticationService.processForgotPasswordForm(email);
//
//    }
    @GetMapping("/reset_password/{token}")
    public String showResetPasswordForm(@PathVariable String token) {
        User customer = userRepository.findByResetPasswordToken(token);


        if (customer == null) {

            return "message";
        }

        return "reset_password_form";
    }
    @PutMapping("/users/updatePass")
    public void updatePass(@RequestParam("token") String token, @RequestParam("passnew") String passnew) {
        authenticationService.updatePassword(token,passnew);
    }
}
