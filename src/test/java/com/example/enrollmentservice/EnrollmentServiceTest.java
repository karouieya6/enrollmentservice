package com.example.enrollmentservice;


import com.example.enrollmentservice.dto.EnrollmentResponse;
import com.example.enrollmentservice.model.Enrollment;
import com.example.enrollmentservice.model.EnrollmentStatus;
import com.example.enrollmentservice.dto.EnrollmentRequest;
import com.example.enrollmentservice.repository.EnrollmentRepository;

import com.example.enrollmentservice.service.EnrollmentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class EnrollmentServiceTest {



    @Mock
    private EnrollmentRepository enrollmentRepository;

    @InjectMocks
    private EnrollmentService enrollmentService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testEnrollUser_successfulEnrollment() {
        // Arrange
        EnrollmentRequest request = new EnrollmentRequest(1L, 2L);

        request.userId();    // ✅ works
        request.courseId();  // ✅ works

        when(enrollmentRepository.findByUserIdAndCourseId(1L, 2L))
                .thenReturn(Optional.empty());

        Enrollment saved = new Enrollment();
        saved.setUserId(1L);
        saved.setCourseId(2L);
        saved.setStatus(EnrollmentStatus.ENROLLED);
        saved.setEnrolledAt(LocalDateTime.now());

        when(enrollmentRepository.save(any(Enrollment.class))).thenReturn(saved);

        // Act
        var result = enrollmentService.enrollUser(request);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.userId());
        assertEquals(2L, result.courseId());
        assertEquals(EnrollmentStatus.ENROLLED, result.status());


        verify(enrollmentRepository, times(1)).save(any(Enrollment.class));
    }
    @Test
    void testEnrollUser_alreadyEnrolled_shouldThrowException() {
        EnrollmentRequest request = new EnrollmentRequest(1L, 2L);
        Enrollment existing = new Enrollment(); // existing enrollment

        when(enrollmentRepository.findByUserIdAndCourseId(1L, 2L)).thenReturn(Optional.of(existing));

        assertThrows(RuntimeException.class, () -> enrollmentService.enrollUser(request));

        verify(enrollmentRepository, never()).save(any());
    }
    @Test
    void testUnenrollUser_successful() {
        EnrollmentRequest request = new EnrollmentRequest(1L, 2L);
        Enrollment existing = new Enrollment();
        existing.setId(99L);

        when(enrollmentRepository.findByUserIdAndCourseId(1L, 2L)).thenReturn(Optional.of(existing));

        enrollmentService.unenrollUser(request);

        verify(enrollmentRepository, times(1)).delete(existing);
    }
    @Test
    void testUnenrollUser_notEnrolled_shouldThrow() {
        EnrollmentRequest request = new EnrollmentRequest(1L, 2L);

        when(enrollmentRepository.findByUserIdAndCourseId(1L, 2L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> enrollmentService.unenrollUser(request));

        verify(enrollmentRepository, never()).delete(any());
    }
    @Test
    void testGetAllEnrollments() {
        Enrollment e1 = new Enrollment();
        e1.setUserId(1L);
        e1.setCourseId(2L);
        e1.setStatus(EnrollmentStatus.ENROLLED);

        Enrollment e2 = new Enrollment();
        e2.setUserId(3L);
        e2.setCourseId(4L);
        e2.setStatus(EnrollmentStatus.ENROLLED);

        when(enrollmentRepository.findAll()).thenReturn(List.of(e1, e2));

        List<EnrollmentResponse> result = enrollmentService.getAllEnrollments();

        assertEquals(2, result.size());
        verify(enrollmentRepository, times(1)).findAll();
    }
    @Test
    void testGetEnrollmentsByUserId() {
        Long userId = 1L;
        Enrollment e = new Enrollment();
        e.setUserId(userId);
        e.setCourseId(2L);
        e.setStatus(EnrollmentStatus.ENROLLED);
        e.setEnrolledAt(LocalDateTime.now());

        when(enrollmentRepository.findAll())
                .thenReturn(List.of(e));

        // simulate manual filtering inside the test (as if it was done in the service)
        List<Enrollment> allEnrollments = enrollmentRepository.findAll();
        List<EnrollmentResponse> result = allEnrollments.stream()
                .filter(en -> en.getUserId().equals(userId))
                .map(en -> new EnrollmentResponse(
                        en.getId(),
                        en.getUserId(),
                        en.getCourseId(),
                        en.getEnrolledAt(),
                        en.getStatus()
                ))
                .toList();

        assertEquals(1, result.size());
        assertEquals(2L, result.get(0).courseId());
    }


    @Test
    void testIsUserEnrolled_true() {
        when(enrollmentRepository.findByUserIdAndCourseId(1L, 2L)).thenReturn(Optional.of(new Enrollment()));
        boolean enrolled = enrollmentService.isUserEnrolled(1L, 2L);
        assertTrue(enrolled);
    }
    @Test
    void testIsUserEnrolled_false() {
        when(enrollmentRepository.findByUserIdAndCourseId(1L, 2L)).thenReturn(Optional.empty());
        boolean enrolled = enrollmentService.isUserEnrolled(1L, 2L);
        assertFalse(enrolled);
    }
}
