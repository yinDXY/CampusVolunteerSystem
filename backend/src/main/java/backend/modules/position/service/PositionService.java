package backend.modules.position.service;

import backend.modules.position.dto.PositionCreateDTO;
import backend.modules.position.dto.PositionUpdateDTO;
import backend.modules.position.dto.PositionVO;

import java.util.List;

public interface PositionService {

    Long createPosition(PositionCreateDTO dto, Long operatorId, Integer operatorRole);

    List<PositionVO> listByActivity(Long activityId);

    void updatePosition(Long id, PositionUpdateDTO dto, Long operatorId, Integer operatorRole);

    void deletePosition(Long id, Long operatorId, Integer operatorRole);
}
