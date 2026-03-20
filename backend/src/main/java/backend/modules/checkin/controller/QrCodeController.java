package backend.modules.checkin.controller;

import backend.common.result.R;
import backend.modules.checkin.dto.QrCodeVO;
import backend.modules.checkin.service.QrCodeService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/checkin")
@RequiredArgsConstructor
public class QrCodeController {

    private final QrCodeService qrCodeService;

    /**
     * 获取活动当前的动态二维码（管理员用）
     * GET /api/checkin/qrcode/{activityId}
     * 权限：活动管理员、超级管理员
     *
     * 说明：每次调用都会刷新令牌（用于管理员主动刷新场景）；
     *       Spring Task 同时每分钟自动刷新，前端只需轮询本接口。
     */
    @GetMapping("/qrcode/{activityId}")
    @PreAuthorize("hasAnyRole('ACTIVITY_ADMIN', 'SUPER_ADMIN')")
    public R<QrCodeVO> getQrCode(@PathVariable Long activityId) {
        return R.ok(qrCodeService.getQrCode(activityId));
    }
}
