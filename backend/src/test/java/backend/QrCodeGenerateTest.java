package backend;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.Map;

/**
 * 独立运行，生成二维码 PNG 到项目根目录
 */
class QrCodeGenerateTest {

    @Test
    void generateQrCodeFile() throws Exception {
        String content = "{\"activityId\":1,\"token\":\"test-token-demo-1234\"}";
        Map<EncodeHintType, Object> hints = Map.of(
                EncodeHintType.CHARACTER_SET, "UTF-8",
                EncodeHintType.MARGIN, 1
        );
        BitMatrix matrix = new MultiFormatWriter()
                .encode(content, BarcodeFormat.QR_CODE, 300, 300, hints);

        Path output = Path.of("../qrcode_demo.png").toAbsolutePath().normalize();
        MatrixToImageWriter.writeToPath(matrix, "PNG", output);
        System.out.println("二维码已保存至：" + output);
    }
}
