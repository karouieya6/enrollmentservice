package com.example.enrollmentservice.service;

import com.example.enrollmentservice.model.Enrollment;
import com.example.enrollmentservice.dto.EnrollmentRequest;
import com.example.enrollmentservice.dto.EnrollmentResponse;
import com.example.enrollmentservice.model.EnrollmentStatus;
import com.example.enrollmentservice.repository.EnrollmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;



    public EnrollmentResponse enrollUser(EnrollmentRequest request) {
        Optional<Enrollment> existing = enrollmentRepository.findByUserIdAndCourseId(request.userId(), request.courseId());

        if (existing.isPresent()) {
            log.warn("❌ Duplicate enrollment attempt: user {} already in course {}", request.userId(), request.courseId());
            throw new ResponseStatusException(HttpStatus.CONFLICT, "User is already enrolled in this course.");
        }

        Enrollment enrollment = new Enrollment();
        enrollment.setUserId(request.userId());
        enrollment.setCourseId(request.courseId());
        enrollment.setEnrolledAt(LocalDateTime.now());
        enrollment.setStatus(EnrollmentStatus.ENROLLED);

        Enrollment saved = enrollmentRepository.save(enrollment);

        log.info("✅ User {} enrolled in course {}", saved.getUserId(), saved.getCourseId());
        return mapToResponse(saved);
    }
    @Transactional
    public void unenrollUser(EnrollmentRequest request) {
        Enrollment enrollment = enrollmentRepository.findByUserIdAndCourseId(request.userId(), request.courseId())
                .orElseThrow(() -> {
                    log.warn("❌ Tried to unenroll non-existing enrollment: user {}, course {}", request.userId(), request.courseId());
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "Enrollment not found.");
                });

        enrollmentRepository.delete(enrollment);
        log.info("🗑️ User {} unenrolled from course {}", request.userId(), request.courseId());
    }

    public Page<EnrollmentResponse> getUserEnrollments(Long userId, Pageable pageable) {
        return enrollmentRepository.findByUserId(userId, pageable)
                .map(this::mapToResponse);
    }


    public boolean isUserEnrolled(Long userId, Long courseId) {
        boolean enrolled = enrollmentRepository.findByUserIdAndCourseId(userId, courseId).isPresent();
        log.info("🔍 Enrollment check - user: {}, course: {} → enrolled: {}", userId, courseId, enrolled);
        return enrolled;
    }
    public List<EnrollmentResponse> getAllEnrollments() {
        List<Enrollment> enrollments = enrollmentRepository.findAll();
        return enrollments.stream()
                .map(this::mapToResponse)
                .toList();
    }


    private EnrollmentResponse mapToResponse(Enrollment enrollment) {
        return new EnrollmentResponse(
                enrollment.getId(),
                enrollment.getUserId(),
                enrollment.getCourseId(),
                enrollment.getEnrolledAt(),
                enrollment.getStatus()
        );
    }
}