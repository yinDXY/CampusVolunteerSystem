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

    // ---- user_tag 操作 ----
    /** 不存在则插入 tag（INSERT IGNORE） */
    void insertTagIfNotExists(@Param("name") String name, @Param("category") String category);

    /** 查询 tag.id（不存在返回 null） */
    Long findTagIdByNameAndCategory(@Param("name") String name, @Param("category") String category);

    /** 建立用户-标签关联（INSERT IGNORE 防重复） */
    void insertUserTag(@Param("userId") Long userId, @Param("tagId") Long tagId);

    /** 删除某用户的全部技能标签关联 */
    void deleteUserTagsByUserId(@Param("userId") Long userId);
}
