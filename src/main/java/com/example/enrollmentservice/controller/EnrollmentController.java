package com.example.enrollmentservice.controller;

import com.example.enrollmentservice.dto.ApiResponse;
import com.example.enrollmentservice.dto.EnrollmentRequest;
import com.example.enrollmentservice.dto.EnrollmentResponse;
import com.example.enrollmentservice.service.EnrollmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/enrollments")
@RequiredArgsConstructor
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    @PostMapping
    public ResponseEntity<ApiResponse<EnrollmentResponse>> enroll(@RequestBody EnrollmentRequest request) {
        EnrollmentResponse response = enrollmentService.enrollUser(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }


    @DeleteMapping
    public ResponseEntity<Void> unenroll(@RequestBody EnrollmentRequest request) {
        enrollmentService.unenrollUser(request);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<Page<EnrollmentResponse>>> getUserEnrollments(
            @PathVariable Long userId,
            @PageableDefault(size = 5) Pageable pageable
    ) {
        Page<EnrollmentResponse> enrollments = enrollmentService.getUserEnrollments(userId, pageable);
        return ResponseEntity.ok(ApiResponse.success(enrollments));
    }



    @GetMapping("/check")
    public ResponseEntity<Boolean> checkEnrollment(@RequestParam Long userId, @RequestParam Long courseId) {
        return ResponseEntity.ok(enrollmentService.isUserEnrolled(userId, courseId));
    }
}
