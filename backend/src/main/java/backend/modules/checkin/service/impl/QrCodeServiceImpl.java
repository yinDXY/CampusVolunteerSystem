package backend.modules.checkin.service.impl;

import backend.common.exception.AppException;
import backend.common.result.ResultCode;
import backend.modules.activity.entity.Activity;
import backend.modules.activity.mapper.ActivityMapper;
import backend.modules.checkin.dto.QrCodeVO;
import backend.modules.checkin.entity.QrToken;
import backend.modules.checkin.mapper.QrTokenMapper;
import backend.modules.checkin.service.QrCodeService;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class QrCodeServiceImpl implements QrCodeService {

    private static final int QR_WIDTH  = 300;
    private static final int QR_HEIGHT = 300;

    private final QrTokenMapper qrTokenMapper;
    private final ActivityMapper activityMapper;

    @Override
    @Transactional
    public QrCodeVO getQrCode(Long activityId) {
        Activity activity = activityMapper.selectById(activityId);
        if (activity == null) throw new AppException(ResultCode.ACTIVITY_NOT_FOUND);

        // 生成新令牌并 upsert（首次创建 or 立即刷新）
        String token = UUID.randomUUID().toString();
        LocalDateTime expireTime = LocalDateTime.now().plusMinutes(1);

        QrToken qrToken = QrToken.builder()
                .activityId(activityId)
                .token(token)
                .expireTime(expireTime)
                .build();
        qrTokenMapper.upsert(qrToken);

        // 二维码内容：JSON格式，包含 activityId + token，供签到端解析
        String content = String.format("{\"activityId\":%d,\"token\":\"%s\"}", activityId, token);
        String base64 = generateQrBase64(content);

        QrCodeVO vo = new QrCodeVO();
        vo.setQrCodeBase64(base64);
        vo.setExpireTime(expireTime);
        return vo;
    }

    /**
     * 将文本内容编码为二维码 PNG，返回 data:image/png;base64,... 字符串
     */
    public static String generateQrBase64(String content) {
        try {
            Map<EncodeHintType, Object> hints = Map.of(
                    EncodeHintType.CHARACTER_SET, "UTF-8",
                    EncodeHintType.MARGIN, 1
            );
            BitMatrix matrix = new MultiFormatWriter()
                    .encode(content, BarcodeFormat.QR_CODE, QR_WIDTH, QR_HEIGHT, hints);
            BufferedImage image = MatrixToImageWriter.toBufferedImage(matrix);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "PNG", baos);
            String base64 = Base64.getEncoder().encodeToString(baos.toByteArray());
            return "data:image/png;base64," + base64;
        } catch (WriterException | IOException e) {
            throw new AppException(500, "二维码生成失败：" + e.getMessage());
        }
    }
}
