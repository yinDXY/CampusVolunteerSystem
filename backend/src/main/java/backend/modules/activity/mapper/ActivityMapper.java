package backend.modules.activity.mapper;

import backend.modules.activity.dto.ActivityQueryDTO;
import backend.modules.activity.dto.ActivityVO;
import backend.modules.activity.entity.Activity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ActivityMapper {

    int insert(Activity activity);

    Activity selectById(@Param("id") Long id);

    ActivityVO selectVOById(@Param("id") Long id);

    int updateById(Activity activity);

    /** 逻辑删除 */
    int deleteById(@Param("id") Long id);

    /** 更新活动状态 */
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    List<ActivityVO> selectPage(ActivityQueryDTO query);

    long countPage(ActivityQueryDTO query);

    /** 报名重用时手动缓冲 signed_count +1 */
    int incrementSignedCount(@Param("id") Long id);
}
