package com.example.enrollmentservice.dto;

import com.example.enrollmentservice.model.EnrollmentStatus;
import java.time.LocalDateTime;

public record EnrollmentResponse(
        Long id,
        Long userId,
        Long courseId,
        LocalDateTime enrolledAt,
        EnrollmentStatus status
) { }
