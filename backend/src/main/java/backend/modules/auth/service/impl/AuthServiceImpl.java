package backend.modules.auth.service.impl;

import backend.common.exception.AppException;
import backend.common.result.ResultCode;
import backend.modules.auth.dto.LoginRequest;
import backend.modules.auth.dto.LoginVO;
import backend.modules.auth.dto.RegisterRequest;
import backend.modules.auth.service.AuthService;
import backend.modules.user.entity.SysUser;
import backend.modules.user.mapper.SysUserMapper;
import backend.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final SysUserMapper sysUserMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Override
    @Transactional
    public void register(RegisterRequest request) {
        if (sysUserMapper.countByUsername(request.getUsername()) > 0) {
            throw new AppException(ResultCode.USER_EXISTS);
        }
        SysUser user = SysUser.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .realName(request.getRealName())
                .studentId(request.getStudentId())
                .phone(request.getPhone())
                .email(request.getEmail())
                .role(0)   // 默认注册为志愿者
                .build();
        sysUserMapper.insert(user);
    }

    @Override
    public LoginVO login(LoginRequest request) {
        SysUser user = sysUserMapper.selectByUsername(request.getUsername());
        if (user == null) {
            throw new AppException(ResultCode.USER_NOT_FOUND);
        }
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new AppException(ResultCode.PASSWORD_ERROR);
        }
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new AppException(ResultCode.ACCOUNT_DISABLED);
        }
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());
        return LoginVO.builder()
                .token(token)
                .userId(user.getId())
                .username(user.getUsername())
                .realName(user.getRealName())
                .role(user.getRole())
                .build();
    }
}
