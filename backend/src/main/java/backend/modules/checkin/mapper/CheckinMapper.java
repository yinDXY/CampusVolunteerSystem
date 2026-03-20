package backend.modules.checkin.mapper;

import backend.modules.checkin.dto.CheckinVO;
import backend.modules.checkin.entity.Checkin;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface CheckinMapper {

    int insert(Checkin checkin);

    /** 查询用户在某活动的签到记录（用于防重复签到、签退查找） */
    Checkin selectByActivityAndUser(@Param("activityId") Long activityId,
                                    @Param("userId") Long userId);

    /** 更新签退时间和时长（触发器依赖 duration_hours 累加用户总时长） */
    int updateCheckout(@Param("id") Long id,
                       @Param("checkoutTime") LocalDateTime checkoutTime,
                       @Param("durationHours") BigDecimal durationHours);

    /** 管理员查看活动全量签到列表 */
    List<CheckinVO> selectListByActivity(@Param("activityId") Long activityId);
}
