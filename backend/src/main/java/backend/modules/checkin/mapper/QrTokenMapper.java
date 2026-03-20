package backend.modules.checkin.mapper;

import backend.modules.checkin.entity.QrToken;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface QrTokenMapper {

    /** 查询活动的当前令牌（不存在则返回 null） */
    QrToken selectByActivityId(@Param("activityId") Long activityId);

    /** 存在则更新，不存在则插入（activity_id 有唯一键） */
    int upsert(QrToken qrToken);

    /** 定时刷新：更新所有现有令牌 */
    int refreshAll();

    /** 查询所有现有令牌（供刷新任务日志用） */
    List<Long> selectAllActivityIds();
}
