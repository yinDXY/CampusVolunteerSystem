package backend.modules.checkin.scheduled;

import backend.modules.checkin.mapper.QrTokenMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 动态二维码定时刷新任务
 *
 * 每分钟将 qr_token 表中所有记录的 token 替换为新 UUID，
 * expireTime 顺延至 NOW() + 1分钟。
 * 这样即使攻击者截获旧 token，最多 1 分钟后即失效，防止重放攻击。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class QrCodeRefreshTask {

    private final QrTokenMapper qrTokenMapper;

    /**
     * cron: 每分钟第 0 秒触发（秒 分 时 日 月 周）
     */
    @Scheduled(cron = "0 * * * * ?")
    @Transactional
    public void refresh() {
        int count = qrTokenMapper.refreshAll();
        if (count > 0) {
            log.debug("[QrCodeRefresh] 已刷新 {} 个活动的二维码令牌", count);
        }
    }
}
