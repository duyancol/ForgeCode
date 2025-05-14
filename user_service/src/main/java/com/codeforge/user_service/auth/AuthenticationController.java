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
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/auth")
@CrossOrigin("*")
@RequiredArgsConstructor
public class AuthenticationController {
    private final UserRepository repository;
    private final AuthenticationService service;
    @Autowired AuthenticationService authenticationService;
    @Autowired
    private final TokenRepository tokenRepository;
    @Autowired
    private final JwtService jwtService;
    @PostMapping("/google/{id}")
    public User authenticateWithGoogle(@PathVariable("id") String googleIdToken ) {
        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new JacksonFactory())
                .setAudience(Collections.singletonList("272854032499-uvoh7etrb27k4sp664qd3baj900l703l.apps.googleusercontent.com")) // Thay thế bằng Google Client ID của bạn
                .build();

        try {
            // Xác thực Google ID token
            GoogleIdToken idToken = verifier.verify(googleIdToken);
            if (idToken != null) {
                GoogleIdToken.Payload payload = idToken.getPayload();

                // Lấy thông tin người dùng từ Payload
                String userId = payload.getSubject();
                String email = payload.getEmail();
                String fullName = (String) payload.get("name");
                Optional<User> existingUserOptional = repository.findByEmail(email);


                if (existingUserOptional.isPresent()) {
                    int id=0;
                    // Nếu tài khoản đã tồn tại, trả về thông tin người dùng từ cơ sở dữ liệu
//          User existingUser = existingUserOptional.get();
                    System.out.println("LOI GI O DAY luu duoc");

                    List<User> list = repository.findAll();
                    for( User u : list){
                        if(u.getEmail().equals(email)){
                            id=u.getId();
                        }
                    }
                    User newUser = new User(id,userId, fullName, email,Role.USER);
                    return newUser;
                } else {

//          // Nếu tài khoản chưa tồn tại, tạo mới một đối tượng User từ thông tin người dùng
//          User newUser = new User(userId, fullName, email,Role.USER);
//
//          // Lưu thông tin người dùng vào cơ sở dữ liệu bằng UserRepository
//          repository.save(newUser);
//
//          // Trả về thông tin người dùng mới
//          System.out.println("luu duoc");

                    var user = User.builder()
                            .userId(userId)
                            .firstname(fullName)
                            .email(email)
                            .role(Role.USER)
                            .build();
                    var savedUser = repository.save(user);
                    var jwtToken = jwtService.generateToken(user);
                    saveUserToken(savedUser, jwtToken);
                    System.out.println("luu duoc");
                    return savedUser;

                }


            } else {
                // Xác thực không thành công
                System.out.println("k luu duoc");
                return null;
            }
        } catch (Exception e) {
            // Xử lý lỗi xác thực
            e.printStackTrace();
            System.out.println("loi");
            return null;
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
