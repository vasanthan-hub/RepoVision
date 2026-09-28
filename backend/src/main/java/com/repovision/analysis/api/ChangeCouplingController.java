package com.repovision.analysis.api;

import com.repovision.analysis.api.dto.ChangeCouplingResponse;
import com.repovision.analysis.application.ChangeCouplingService;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/repositories")
public class ChangeCouplingController {

    private final ChangeCouplingService changeCouplingService;

    public ChangeCouplingController(ChangeCouplingService changeCouplingService) {
        this.changeCouplingService = changeCouplingService;
    }

    @GetMapping("/{repositoryId}/analytics/coupling")
    public ChangeCouplingResponse getChangeCoupling(@PathVariable UUID repositoryId) {
        return changeCouplingService.getChangeCoupling(repositoryId);
    }
}
