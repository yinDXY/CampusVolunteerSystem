package backend.modules.profile.service;

import backend.modules.profile.dto.ProfileVO;

import java.util.List;

public interface ProfileService {

    /** 获取当前登录志愿者的画像（含雷达图）*/
    ProfileVO getMyProfile();

    /** 管理员查看任意用户画像（含雷达图）*/
    ProfileVO getProfile(Long userId);

    /** 更新当前登录志愿者的技能标签（全量替换）*/
    void updateMyTags(List<String> tags);

    /** 保存用户技能标签（不清空旧标签，仅新增），为注册流程使用 */
    void saveUserTagsOnRegister(Long userId, List<String> tags);
}
