package com.example.event_review.Controller;

import com.example.event_review.DTO.ApprovalHistoryDTO;
import com.example.event_review.Service.ApprovalHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/approval-history")
@CrossOrigin(origins = "*")
public class ApprovalHistoryController {
    @Autowired
    private ApprovalHistoryService historyService;

    @GetMapping("/proposal/{proposalId}")
    public List<ApprovalHistoryDTO> getHistoryByProposalId(@PathVariable Long proposalId) {
        return historyService.getHistoryByProposalId(proposalId);
    }
}