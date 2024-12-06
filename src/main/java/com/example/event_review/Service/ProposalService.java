package com.example.event_review.Service;

import com.example.event_review.DTO.ProposalDTO;
import com.example.event_review.Entity.Department;
import com.example.event_review.Entity.Proposal;
import com.example.event_review.Entity.User;
import com.example.event_review.Repo.DepartmentRepo;
import com.example.event_review.Repo.ProposalRepo;
import com.example.event_review.Repo.UserRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ProposalService {
    private static final Logger logger = LoggerFactory.getLogger(ProposalService.class);
    //This is used to log information, warnings, and errors. It's useful for debugging and keeping track of application flow.

    @Autowired
    private ProposalRepo proposalRepo;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private DepartmentRepo departmentRepo;

    @Autowired
    private ApprovalHistoryService approvalHistoryService;

    public List<ProposalDTO> getAllProposals() {
        try {
            return proposalRepo.findAll().stream() //findAll() method is used to get all proposals from the database.   
                    .map(this::convertToDTO) //map() method is used to convert each Proposal entity to a ProposalDTO object.
                    .collect(Collectors.toList()); //collect() method is used to collect the converted ProposalDTO objects into a list.
        } catch (Exception e) {
            logger.error("Error getting all proposals: ", e);
            return null;
        }
    }

    public Optional<ProposalDTO> getProposalById(Long id) { //This method is used to get a proposal by its ID.
        try {
            return proposalRepo.findById(id) //findById() method is used to get a proposal by its ID.
                    .map(this::convertToDTO); //map() method is used to convert the Proposal entity to a ProposalDTO object.
        } catch (Exception e) {
            logger.error("Error getting proposal with id {}: ", id, e);
            return Optional.empty();
        }
    }

    public List<ProposalDTO> getProposalsByUserId(Long userId) {
        try {
            return proposalRepo.findByUser_UserId(userId).stream() //findByUser_UserId() method is used to get all proposals associated with a specific user.
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error getting proposals for user id {}: ", userId, e);
            return null;
        }
    }

    public List<ProposalDTO> getProposalsByApproverId(Long approverId) {
        try {
            return proposalRepo.findByCurrentApprover_UserId(approverId).stream() //findByCurrentApprover_UserId() method is used to get all proposals associated with a specific approver.
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error getting proposals for approver id {}: ", approverId, e);
            return null;
        }
    }

    public List<ProposalDTO> getProposalsByStatus(String status) {
        try {
            return proposalRepo.findByStatus(status).stream() //findByStatus() method is used to get all proposals with a specific status.  
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error getting proposals with status {}: ", status, e);
            return null;
        }
    }
public ProposalDTO addProposal(ProposalDTO proposalDTO) {
    try {
     
        if (proposalDTO.getProposalDate() == null) {
            proposalDTO.setProposalDate(LocalDateTime.now()); //If the proposal date is not provided, the current date and time are set.
        }

        Optional<User> user = userRepo.findById(proposalDTO.getUserId()); //findById() method is used to get the user by ID.
        Optional<Department> department = departmentRepo.findById(proposalDTO.getDepartmentId()); //findById() method is used to get the department by ID.

        if (!user.isPresent() || !department.isPresent()) { 
            logger.error("User or Department not found. UserId: {}, DeptId: {}", 
                proposalDTO.getUserId(), proposalDTO.getDepartmentId());
            return null;
        } //If the user or department is not found, an error message is logged and null is returned.

        Proposal proposal = convertToEntity(proposalDTO); //convertToEntity() method is used to convert the ProposalDTO object to a Proposal entity.
        proposal.setUser(user.get()); //The user and department are set for the proposal.
        proposal.setDepartment(department.get());
        
        Proposal savedProposal = proposalRepo.save(proposal); //    save() method is used to save the proposal to the database.
        return convertToDTO(savedProposal); //The saved proposal is converted to a ProposalDTO object and returned.
    } catch (Exception e) {
        logger.error("Error adding proposal: ", e);
        return null;
    }
}

    public ProposalDTO updateProposal(Long id, ProposalDTO proposalDTO) {
        try {
            Optional<Proposal> existingProposal = proposalRepo.findById(id);
            if (existingProposal.isPresent()) {
                Proposal proposal = existingProposal.get();
                updateProposalFromDTO(proposal, proposalDTO);
                return convertToDTO(proposalRepo.save(proposal));
            }
            return null;
        } catch (Exception e) {
            logger.error("Error updating proposal with id {}: ", id, e);
            return null;
        }
    } //This method is used to update an existing proposal. It first checks if the proposal exists, then updates the proposal with the new data provided in the ProposalDTO object.


    public ProposalDTO updateProposalStatus(Long id, String newStatus, Long approverId, Long fundingSourceId, String comments) {
        try {
            Optional<Proposal> existingProposal = proposalRepo.findById(id);
            Optional<User> approver = userRepo.findById(approverId);
    
            if (existingProposal.isPresent() && approver.isPresent()) {
                Proposal proposal = existingProposal.get();
                String oldStatus = proposal.getStatus();
                proposal.setStatus(newStatus);
                proposal.setCurrentApprover(approver.get());
    
                // Save the proposal first
                Proposal updatedProposal = proposalRepo.save(proposal);
    
                // Add the history entry
                approvalHistoryService.addHistoryEntry(id, approverId, fundingSourceId, oldStatus, newStatus, comments);
    
                // Convert and return the updated proposal
                return convertToDTO(updatedProposal);
            }
            return null;
        } catch (Exception e) {
            logger.error("Error updating proposal status with id {}: ", id, e);
            return null;
        }
    }
    //This method is used to update the status of a proposal. It first checks if the proposal and approver exist, then updates the status of the proposal with the new status provided.     


    public void deleteProposal(Long id) {
        try {
            proposalRepo.deleteById(id);
        } catch (Exception e) {
            logger.error("Error deleting proposal with id {}: ", id, e);
        }
    }

    public ProposalDTO convertToDTO(Proposal proposal) {
        ProposalDTO proposalDTO = new ProposalDTO();
        proposalDTO.setProposalId(proposal.getProposalId());
        proposalDTO.setUserId(proposal.getUser().getUserId());
        proposalDTO.setItemName(proposal.getItemName());
        proposalDTO.setCategory(proposal.getCategory());
        proposalDTO.setDescription(proposal.getDescription());
        proposalDTO.setQuantity(proposal.getQuantity());
        proposalDTO.setEstimatedCost(proposal.getEstimatedCost());
        proposalDTO.setVendorInfo(proposal.getVendorInfo());
        proposalDTO.setBusinessPurpose(proposal.getBusinessPurpose());
        proposalDTO.setStatus(proposal.getStatus());
        proposalDTO.setProposalDate(proposal.getProposalDate());
        proposalDTO.setCurrentApproverId(proposal.getCurrentApprover() != null ? proposal.getCurrentApprover().getUserId() : null);
        proposalDTO.setDepartmentId(proposal.getDepartment().getDeptId());
        return proposalDTO;
    }
    //This method is used to convert a Proposal entity to a ProposalDTO object. 
    //The Proposal entity properties are set to the corresponding properties of the ProposalDTO object.
    //The user, current approver, and department IDs are also set in the ProposalDTO object.
    //The converted ProposalDTO object is then returned.
    

    public Proposal convertToEntity(ProposalDTO proposalDTO) {
        Proposal proposal = new Proposal();
        proposal.setProposalId(proposalDTO.getProposalId());
        proposal.setItemName(proposalDTO.getItemName());
        proposal.setCategory(proposalDTO.getCategory());
        proposal.setDescription(proposalDTO.getDescription());
        proposal.setQuantity(proposalDTO.getQuantity());
        proposal.setEstimatedCost(proposalDTO.getEstimatedCost());
        proposal.setVendorInfo(proposalDTO.getVendorInfo());
        proposal.setBusinessPurpose(proposalDTO.getBusinessPurpose());
        proposal.setStatus(proposalDTO.getStatus());
        proposal.setProposalDate(proposalDTO.getProposalDate());
    
        Optional<User> user = userRepo.findById(proposalDTO.getUserId());
        user.ifPresent(proposal::setUser);
    
        Optional<User> currentApprover = userRepo.findById(proposalDTO.getCurrentApproverId());
        currentApprover.ifPresent(proposal::setCurrentApprover);
    
        Optional<Department> department = departmentRepo.findById(proposalDTO.getDepartmentId());
        department.ifPresent(proposal::setDepartment);
    
        return proposal;
    }
    

    private void updateProposalFromDTO(Proposal proposal, ProposalDTO dto) {
        proposal.setItemName(dto.getItemName());
        proposal.setCategory(dto.getCategory());
        proposal.setDescription(dto.getDescription());
        proposal.setQuantity(dto.getQuantity());
        proposal.setEstimatedCost(dto.getEstimatedCost());
        proposal.setVendorInfo(dto.getVendorInfo());
        proposal.setBusinessPurpose(dto.getBusinessPurpose());
        proposal.setStatus(dto.getStatus());
        proposal.setProposalDate(dto.getProposalDate());
    }
}

