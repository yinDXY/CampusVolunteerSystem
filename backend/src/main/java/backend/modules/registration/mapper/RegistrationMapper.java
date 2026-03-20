package backend.modules.registration.mapper;

import backend.modules.registration.dto.RegistrationQueryDTO;
import backend.modules.registration.dto.RegistrationVO;
import backend.modules.registration.entity.Registration;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface RegistrationMapper {

    int insert(Registration registration);

    Registration selectById(@Param("id") Long id);

    /** 查询某用户对某活动的报名记录（用于防重复） */
    Registration selectByActivityAndUser(@Param("activityId") Long activityId,
                                         @Param("userId") Long userId);

    /** 志愿者查询自己的报名列表（分页） */
    List<RegistrationVO> selectPageByUser(@Param("userId") Long userId,
                                           @Param("query") RegistrationQueryDTO query);

    long countPageByUser(@Param("userId") Long userId,
                          @Param("query") RegistrationQueryDTO query);

    /** 管理员查询某活动的报名列表（分页） */
    List<RegistrationVO> selectPageByActivity(@Param("activityId") Long activityId,
                                               @Param("query") RegistrationQueryDTO query);

    long countPageByActivity(@Param("activityId") Long activityId,
                              @Param("query") RegistrationQueryDTO query);

    /** 更新报名状态 */
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    /** 管理员给志愿者打分 */
    int updateScore(@Param("id") Long id, @Param("score") Integer score);

    /** 取消后重新报名：复用已有记录，避免唯一键冲突 */
    int reApply(@Param("id") Long id, @Param("remark") String remark);

    /** 派岗：设置 positionId 并将状态改为 3（已派岗）；触发器自动维护 position.assigned_count++ */
    int assignPosition(@Param("id") Long id, @Param("positionId") Long positionId);

    /** 取消派岗：清除 positionId 并将状态回退至 1（已通过）；触发器自动维护 position.assigned_count-- */
    int cancelAssign(@Param("id") Long id);
}
