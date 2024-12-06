package com.example.event_review.Service;

import com.example.event_review.DTO.ApprovalHistoryDTO;
import com.example.event_review.Entity.ApprovalHistory;
import com.example.event_review.Entity.FundingSource;
import com.example.event_review.Entity.Proposal;
import com.example.event_review.Entity.User;
import com.example.event_review.Repo.ApprovalHistoryRepo;
import com.example.event_review.Repo.FundingSourceRepo;
import com.example.event_review.Repo.ProposalRepo;
import com.example.event_review.Repo.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ApprovalHistoryService {
    @Autowired
    private ApprovalHistoryRepo historyRepo;

    @Autowired
    private ProposalRepo proposalRepo;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private FundingSourceRepo fundingSourceRepo;

    public List<ApprovalHistoryDTO> getHistoryByProposalId(Long proposalId) {
        return historyRepo.findByProposal_ProposalIdOrderByActionDateDesc(proposalId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public void addHistoryEntry(Long proposalId, Long approverId, Long fundingSourceId, String oldStatus, 
                              String newStatus, String comments) {
        Proposal proposal = proposalRepo.findById(proposalId).orElse(null);
        User approver = userRepo.findById(approverId).orElse(null);
        FundingSource fundingSource = fundingSourceRepo.findById(fundingSourceId).orElse(null);

        if (proposal != null && approver != null && fundingSource != null) {
            ApprovalHistory history = new ApprovalHistory();
            history.setProposal(proposal);
            history.setApprover(approver);
            history.setFundingSource(fundingSource);
            history.setOldStatus(oldStatus);
            history.setNewStatus(newStatus);
            history.setComments(comments);
            history.setActionDate(LocalDateTime.now());
            
            historyRepo.save(history);
        }
    }

    private ApprovalHistoryDTO convertToDTO(ApprovalHistory history) {
        ApprovalHistoryDTO dto = new ApprovalHistoryDTO();
        dto.setId(history.getId());
        dto.setProposalId(history.getProposal().getProposalId());
        dto.setApproverId(history.getApprover().getUserId());
        dto.setFundingSourceId(history.getFundingSource().getSourceId());
        dto.setOldStatus(history.getOldStatus());
        dto.setNewStatus(history.getNewStatus());
        dto.setComments(history.getComments());
        dto.setActionDate(history.getActionDate());
        return dto;
    }
}
