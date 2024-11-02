package com.capstone.safeGuard.apis.main.presentation;

import com.capstone.safeGuard.apis.member.application.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
@Slf4j
public class MainController {
    private final JwtService jwtService;

    @GetMapping("/home")
    public String showHome() {
        return "home";
    }

    @GetMapping("/group")
    public String showGroup() {
        return "group";
    }


    @GetMapping("/option")
    public String showOption() {
        return "option";
    }


    @GetMapping("/check-auth")
    public ResponseEntity checkAuth(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        log.info("token: {}", token);

        if (jwtService.findByToken(token).isBlackList()) {
            return ResponseEntity.status(403).build();
        }
        return ResponseEntity.ok().build();
    }
}
