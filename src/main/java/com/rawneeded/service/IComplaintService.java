package com.rawneeded.service;

import com.rawneeded.dto.complaint.ComplaintMessageRequestDto;
import com.rawneeded.dto.complaint.ComplaintMessageResponseDto;
import com.rawneeded.dto.complaint.ComplaintResponseDto;
import com.rawneeded.dto.complaint.CreateComplaintRequestDto;
import com.rawneeded.enumeration.ComplaintStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IComplaintService {
    ComplaintResponseDto createComplaint(CreateComplaintRequestDto requestDto);
    ComplaintResponseDto getComplaintById(String complaintId);
    Page<ComplaintResponseDto> getMyComplaints(Pageable pageable);
    Page<ComplaintResponseDto> getAllComplaints(Pageable pageable);
    Page<ComplaintResponseDto> getComplaintsByStatus(ComplaintStatus status, Pageable pageable);
    ComplaintMessageResponseDto addMessage(String complaintId, ComplaintMessageRequestDto requestDto);
    ComplaintResponseDto closeComplaint(String complaintId);
}
