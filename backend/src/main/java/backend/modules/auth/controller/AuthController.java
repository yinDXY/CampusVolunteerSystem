package backend.modules.auth.controller;

import backend.common.result.R;
import backend.modules.auth.dto.LoginRequest;
import backend.modules.auth.dto.LoginVO;
import backend.modules.auth.dto.RegisterRequest;
import backend.modules.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * 志愿者注册
     * POST /api/auth/register
     */
    @PostMapping("/register")
    public R<Void> register(@Valid @RequestBody RegisterRequest request) {
        authService.register(request);
        return R.ok();
    }

    /**
     * 登录（所有角色通用）
     * POST /api/auth/login
     * 返回 JWT token + 用户基本信息
     */
    @PostMapping("/login")
    public R<LoginVO> login(@Valid @RequestBody LoginRequest request) {
        return R.ok(authService.login(request));
    }
}
