package backend.modules.position.mapper;

import backend.modules.position.dto.PositionVO;
import backend.modules.position.entity.Position;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PositionMapper {

    int insert(Position position);

    Position selectById(@Param("id") Long id);

    List<PositionVO> selectByActivityId(@Param("activityId") Long activityId);

    int updateById(Position position);

    /** 逻辑删除 */
    int deleteById(@Param("id") Long id);
}
