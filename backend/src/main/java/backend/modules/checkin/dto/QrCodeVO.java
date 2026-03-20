package backend.modules.checkin.dto;

import lombok.Data;

import java.time.LocalDateTime;

/** 二维码响应 VO */
@Data
public class QrCodeVO {
    /** Base64 编码的 PNG 图片，格式：data:image/png;base64,... */
    private String qrCodeBase64;
    /** 令牌过期时间 */
    private LocalDateTime expireTime;
}
