package com.example.event_review.Repo;

import com.example.event_review.Entity.Proposal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ProposalRepo extends JpaRepository<Proposal, Long> {
    List<Proposal> findByUser_UserId(Long userId); //This refers to the user property in the Proposal entity and then userId property of the User entity.
    List<Proposal> findByCurrentApprover_UserId(Long approverId); // This is similar to the previous method but finds proposals based on the currentApprover property of the Proposal entity.
    List<Proposal> findByStatus(String status); // This method is used to find all proposals that have a specific status, such as "pending", "approved", or "rejected".
    List<Proposal> findByDepartment_DeptId(Long deptId);  // This method is used to find all proposals associated with a specific department.
    List<Proposal> findByProposalDateBetween(LocalDateTime startDate, LocalDateTime endDate); // This method is used to find all proposals created between two specific dates.
}

