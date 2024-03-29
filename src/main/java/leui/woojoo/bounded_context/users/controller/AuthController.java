package leui.woojoo.bounded_context.users.controller;

import leui.woojoo.bounded_context.sms.service.SmsService;
import leui.woojoo.bounded_context.users.dto.web.*;
import leui.woojoo.bounded_context.users.entity.Users;
import leui.woojoo.base.jwt.JwtProvider;
import leui.woojoo.bounded_context.groups.service.GroupsService;
import leui.woojoo.bounded_context.users.service.AuthService;
import leui.woojoo.bounded_context.users.service.UsersService;
import leui.woojoo.base.utils.UserUtils;
import leui.woojoo.base.utils.FileUtils;
import leui.woojoo.bounded_context.users.dto.UserDetail;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;
    private final UsersService usersService;
    private final GroupsService groupsService;
    private final SmsService smsService;
    private final JwtProvider jwtProvider;
    private final FileUtils fileUtils;

    @GetMapping("/test")
    public String jmeterTest() {
        return "ok";
    }

    @PostMapping(value = "/signup", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<SignupResponse> signup(SignupRequest requestDto) throws IllegalStateException, IOException {

        if (authService.findByPhoneNumber(requestDto.getPhoneNumber()) != null) {
            return new ResponseEntity<>(new SignupResponse(), HttpStatus.CONFLICT);
        }

        String profileImageName = fileUtils.upload(requestDto.getFile(), "profile");
        Users users = authService.save(requestDto.toUserEntity(profileImageName));

        groupsService.save(requestDto.toUserGroupEntity(users));
        smsService.delete(requestDto.getPhoneNumber());

        String token = jwtProvider.createToken(users.getId());
        return new ResponseEntity<>(new SignupResponse(token), HttpStatus.OK);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest loginRequest) {

        String phoneNumber = loginRequest.getPhoneNumber();
        UserDetail users = authService.findByPhoneNumber(phoneNumber);
        if (users == null) {
            return new ResponseEntity<>(new LoginResponse(), HttpStatus.UNAUTHORIZED);
        }

        if (!smsService.verify(phoneNumber, loginRequest.getSmsCode())) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        smsService.delete(phoneNumber);
        
        String token = jwtProvider.createToken(users.getId());
        
        if (!users.getFcmToken().equals(loginRequest.getFcmToken())) {
            authService.updateFcmToken(users.getId(), loginRequest.getFcmToken());
        }
        return new ResponseEntity<>(new LoginResponse(token), HttpStatus.OK);
    }

    @DeleteMapping("/withdrawal")
    @ResponseStatus(HttpStatus.OK)
    public String withdrawUser(@AuthenticationPrincipal User user) {
        String profileImageName = authService.deleteUser(Long.parseLong(user.getUsername()));
        if (profileImageName != null) {
            fileUtils.delete(profileImageName, "profile");
        }
        return "ok";
    }

    @PostMapping("/sync-token")
    @ResponseStatus(HttpStatus.OK)
    public String asyncFcmToken(@AuthenticationPrincipal User user, @RequestBody FcmRequest fcmRequest) {
        usersService.asyncFcmToken(UserUtils.resolveUserId(user), fcmRequest.getFcmToken());
        return "ok";
    }
}
