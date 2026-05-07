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

    // ---- tag 操作 ----
    /** 查询技能标签 ID；不存在返回 null */
    Long findTagIdByNameAndCategory(@Param("name") String name, @Param("category") String category);

    /** INSERT IGNORE：tag 已存在时不报错 */
    void insertTagIfNotExists(@Param("name") String name, @Param("category") String category);

    /** 写入 position_tag 关联；IGNORE 防重复 */
    void insertPositionTag(@Param("positionId") Long positionId, @Param("tagId") Long tagId);

    /** 删除某岗位的全部 tag 关联 */
    void deletePositionTagsByPositionId(@Param("positionId") Long positionId);
}
