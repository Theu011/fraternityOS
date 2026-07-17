package com.fraternityos.server.house.application;

import com.fraternityos.server.house.application.dto.PositionResponse;
import com.fraternityos.server.house.infrastructure.PositionRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PositionService {

    private final PositionRepository positionRepository;

    public PositionService(PositionRepository positionRepository) {
        this.positionRepository = positionRepository;
    }

    /** The global position catalog (for member position-assignment dropdowns). */
    @Transactional(readOnly = true)
    public List<PositionResponse> list() {
        return positionRepository.findAllByOrderByNameAsc().stream()
                .map(PositionResponse::from)
                .toList();
    }
}
