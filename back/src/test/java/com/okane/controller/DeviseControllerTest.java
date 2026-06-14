package com.okane.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.okane.controller.DeviseController;
import com.okane.dto.requestDto.DeviseRequestDTO;
import com.okane.dto.responseDto.DeviseResponseDTO;
import com.okane.service.impl.DeviseServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class DeviseControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private DeviseServiceImpl deviseService;

    @InjectMocks
    private DeviseController deviseController;

    private DeviseResponseDTO madDTO;
    private DeviseResponseDTO eurDTO;
    private Long madId;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();

        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setDefaultCharset(StandardCharsets.UTF_8);

        mockMvc = MockMvcBuilders
                .standaloneSetup(deviseController)
                .setMessageConverters(converter)
                .build();

        madId = 1L;

        madDTO = DeviseResponseDTO.builder()
                .id(madId)
                .code("MAD")
                .symbole("DH")
                .nom("Dirham Marocain")
                .build();

        eurDTO = DeviseResponseDTO.builder()
                .id(2L)
                .code("EUR")
                .symbole("€")
                .nom("Euro")
                .build();
    }

    @Test
    void getAll_ShouldReturnListOfDevises() throws Exception {
        when(deviseService.findAll()).thenReturn(Arrays.asList(madDTO, eurDTO));

        mockMvc.perform(get("/api/v1/admin/currencies")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].code").value("MAD"))
                .andExpect(jsonPath("$[0].symbole").value("DH"))
                .andExpect(jsonPath("$[1].code").value("EUR"));

        verify(deviseService).findAll();
    }

    @Test
    void getById_ShouldReturnDevise() throws Exception {
        when(deviseService.findById(madId)).thenReturn(madDTO);

        mockMvc.perform(get("/api/v1/admin/currencies/{id}", madId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("MAD"))
                .andExpect(jsonPath("$.symbole").value("DH"));

        verify(deviseService).findById(madId);
    }

    @Test
    void create_ShouldReturnCreatedDevise() throws Exception {
        DeviseRequestDTO requestDTO = DeviseRequestDTO.builder()
                .code("EUR")
                .symbole("€")
                .nom("Euro")
                .build();

        when(deviseService.save(any(DeviseRequestDTO.class))).thenReturn(eurDTO);

        mockMvc.perform(post("/api/v1/admin/currencies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("EUR"))
                .andExpect(jsonPath("$.id").exists());

        verify(deviseService).save(any(DeviseRequestDTO.class));
    }

    @Test
    void update_ShouldReturnUpdatedDevise() throws Exception {
        DeviseRequestDTO requestDTO = DeviseRequestDTO.builder()
                .code("MAD")
                .symbole("MAD")
                .nom("Dirham Marocain")
                .build();

        DeviseResponseDTO updatedDTO = DeviseResponseDTO.builder()
                .id(madId)
                .code("MAD")
                .symbole("MAD")
                .nom("Dirham Marocain")
                .build();

        when(deviseService.update(eq(madId), any(DeviseRequestDTO.class))).thenReturn(updatedDTO);

        mockMvc.perform(put("/api/v1/admin/currencies/{id}", madId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.symbole").value("MAD"));

        verify(deviseService).update(eq(madId), any(DeviseRequestDTO.class));
    }

    @Test
    void delete_ShouldReturnNoContent() throws Exception {
        doNothing().when(deviseService).delete(madId);

        mockMvc.perform(delete("/api/v1/admin/currencies/{id}", madId))
                .andExpect(status().isNoContent());

        verify(deviseService).delete(madId);
    }
}