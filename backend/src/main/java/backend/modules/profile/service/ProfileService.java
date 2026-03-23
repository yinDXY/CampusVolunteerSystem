package backend.modules.profile.service;

import backend.modules.profile.dto.ProfileVO;

public interface ProfileService {

    /** 获取当前登录志愿者的画像（含雷达图）*/
    ProfileVO getMyProfile();

    /** 管理员查看任意用户画像（含雷达图）*/
    ProfileVO getProfile(Long userId);
}
