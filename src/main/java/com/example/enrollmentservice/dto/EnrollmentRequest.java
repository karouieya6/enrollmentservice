package com.example.enrollmentservice.dto;


import lombok.Data;


public record EnrollmentRequest(Long userId, Long courseId) {}