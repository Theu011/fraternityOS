package com.fraternityos.server.house.api;

import com.fraternityos.server.house.application.PositionService;
import com.fraternityos.server.house.application.dto.PositionResponse;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Lists the global position catalog (for member position-assignment dropdowns). */
@RestController
@RequestMapping("/positions")
public class PositionController {

    private final PositionService positionService;

    public PositionController(PositionService positionService) {
        this.positionService = positionService;
    }

    @GetMapping
    public List<PositionResponse> positions() {
        return positionService.list();
    }
}
