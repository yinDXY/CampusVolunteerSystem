package backend.modules.auth.service;

import backend.modules.auth.dto.LoginRequest;
import backend.modules.auth.dto.LoginVO;
import backend.modules.auth.dto.RegisterRequest;

public interface AuthService {
    void register(RegisterRequest request);
    LoginVO login(LoginRequest request);
}
