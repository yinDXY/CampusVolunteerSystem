package backend.modules.profile.mapper;

import backend.modules.profile.dto.ProfileVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ProfileMapper {

    /**
     * 从 v_volunteer_profile 视图查询志愿者画像数据
     * @param userId 用户 ID
     * @return ProfileVO（不含 radarImageBase64），用户不存在或非志愿者时返回 null
     */
    ProfileVO selectByUserId(@Param("userId") Long userId);
}
