package com.okane.controller;

import com.okane.dto.NotificationPreferenceDto;
import com.okane.entity.User;
import com.okane.repository.UserRepository;
import com.okane.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class NotificationControllerTest {
    private MockMvc mockMvc;
    @Mock
    private NotificationService notificationService;
    @Mock
    private UserRepository userRepository;
    @InjectMocks
    private NotificationController notificationController;
    private User user;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(notificationController).build();
        user = new User();
        user.setEmail("test@example.com");
    }

    @Test
    void getNotifications_ShouldReturnOk() throws Exception {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(notificationService.getUserNotifications(any(User.class))).thenReturn(null);
        mockMvc.perform(get("/api/v1/notifications").header("X-User-Email", "test@example.com"))
                .andExpect(status().isOk());
    }

    @Test
    void markAsRead_ShouldReturnOk() throws Exception {
        doNothing().when(notificationService).markAsRead(anyLong());
        mockMvc.perform(patch("/api/v1/notifications/1/read"))
                .andExpect(status().isOk());
    }

    @Test
    void updatePrefs_ShouldReturnOk() throws Exception {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        doNothing().when(notificationService).updatePreferences(any(User.class), any(NotificationPreferenceDto.class));
        mockMvc.perform(put("/api/v1/clients/notifications/prefs")
                .header("X-User-Email", "test@example.com")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isOk());
    }
}
