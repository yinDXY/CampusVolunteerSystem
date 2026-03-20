package backend.modules.checkin.service;

import backend.modules.checkin.dto.QrCodeVO;

public interface QrCodeService {

    /**
     * 获取活动当前的动态二维码。
     * 若该活动尚无令牌记录则自动创建；若令牌已过期则刷新后返回。
     *
     * @param activityId 活动 ID
     * @return 包含 base64 图片和过期时间的 VO
     */
    QrCodeVO getQrCode(Long activityId);
}
