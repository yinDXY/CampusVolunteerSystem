package backend.modules.profile.service.impl;

import backend.common.exception.AppException;
import backend.common.result.ResultCode;
import backend.modules.profile.dto.FlaskRadarResponse;
import backend.modules.profile.dto.ProfileVO;
import backend.modules.profile.mapper.ProfileMapper;
import backend.modules.profile.service.ProfileService;
import backend.security.SecurityUtil;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProfileServiceImpl implements ProfileService {

    private final ProfileMapper profileMapper;
    private final RestClient.Builder restClientBuilder;

    /** Flask 雷达图端点，默认本机 5001，可在 application.properties 覆盖 */
    @Value("${flask.radar.url:http://localhost:5001/radar}")
    private String flaskRadarUrl;

    private RestClient restClient;

    @PostConstruct
    private void initRestClient() {
        this.restClient = restClientBuilder.build();
    }

    @Override
    public ProfileVO getMyProfile() {
        Long userId = SecurityUtil.getCurrentUserId();
        return getProfile(userId);
    }

    @Override
    public ProfileVO getProfile(Long userId) {
        ProfileVO profile = profileMapper.selectByUserId(userId);
        if (profile == null) {
            throw new AppException(ResultCode.PROFILE_NOT_FOUND);
        }

        // 调用 Flask 获取雷达图 base64
        try {
            Map<String, Object> flaskReq = Map.of(
                    "userId",          userId,
                    "realName",        profile.getRealName()        != null ? profile.getRealName()        : "",
                    "totalHours",      profile.getTotalHours()      != null ? profile.getTotalHours()      : 0,
                    "activityCount",   profile.getActivityCount()   != null ? profile.getActivityCount()   : 0,
                    "typeDiversity",   profile.getTypeDiversity()   != null ? profile.getTypeDiversity()   : 0,
                    "avgScore",        profile.getAvgScore()        != null ? profile.getAvgScore()        : 0,
                    "punctualityRate", profile.getPunctualityRate() != null ? profile.getPunctualityRate() : 0
            );

            FlaskRadarResponse resp = restClient.post()
                    .uri(flaskRadarUrl)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(flaskReq)
                    .retrieve()
                    .body(FlaskRadarResponse.class);

            if (resp != null && resp.getImageBase64() != null) {
                profile.setRadarImageBase64("data:image/png;base64," + resp.getImageBase64());
            }
        } catch (Exception e) {
            // Flask 不可用时降级：返回画像数据但不含图片
            log.warn("Flask 雷达图服务不可用，跳过图像生成 [userId={}]: {}", userId, e.getMessage());
        }

        return profile;
    }
}
