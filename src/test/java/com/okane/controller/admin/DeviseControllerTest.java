package com.okane.controller.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.okane.entity.Devise;
import com.okane.service.DeviseService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class DeviseControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private DeviseService deviseService;

    @InjectMocks
    private DeviseController deviseController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(deviseController).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void getAll_ShouldReturnListOfDevises() throws Exception {
        Devise mad = Devise.builder()
                .id(UUID.randomUUID())
                .code("MAD")
                .symbole("DH")
                .build();

        when(deviseService.findAll()).thenReturn(Arrays.asList(mad));

        mockMvc.perform(get("/api/v1/admin/currencies"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].code").value("MAD"))
                .andExpect(jsonPath("$[0].symbole").value("DH"));

        verify(deviseService).findAll();
    }

    @Test
    void create_ShouldReturnCreatedDevise() throws Exception {
        Devise input = Devise.builder()
                .code("EUR")
                .symbole("€")
                .build();

        Devise saved = Devise.builder()
                .id(UUID.randomUUID())
                .code("EUR")
                .symbole("€")
                .build();

        when(deviseService.save(any(Devise.class))).thenReturn(saved);

        mockMvc.perform(post("/api/v1/admin/currencies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("EUR"))
                .andExpect(jsonPath("$.id").exists());

        verify(deviseService).save(any(Devise.class));
    }

    @Test
    void getById_ShouldReturnDevise() throws Exception {
        UUID id = UUID.randomUUID();
        Devise mad = Devise.builder()
                .id(id)
                .code("MAD")
                .symbole("DH")
                .build();

        when(deviseService.findById(id)).thenReturn(mad);

        mockMvc.perform(get("/api/v1/admin/currencies/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("MAD"));
    }
}