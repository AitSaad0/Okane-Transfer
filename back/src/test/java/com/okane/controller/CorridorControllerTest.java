package com.okane.controller;

import com.okane.controller.CorridorController;
import com.okane.dto.requestDto.CorridorRequestDTO;
import com.okane.dto.responseDto.CorridorResponseDTO;
import com.okane.service.CorridorService;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import java.math.BigDecimal;
import java.util.Arrays;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class CorridorControllerTest {

    private MockMvc mockMvc;

    @Mock
    private CorridorService corridorService;

    @InjectMocks
    private CorridorController corridorController;

    private ObjectMapper objectMapper;
    private CorridorResponseDTO responseDTO;
    private CorridorRequestDTO requestDTO;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        mockMvc = MockMvcBuilders
                .standaloneSetup(corridorController)
                .setMessageConverters(new MappingJackson2HttpMessageConverter())
                .build();

        responseDTO = CorridorResponseDTO.builder()
                .id(1L)
                .tauxChange(new BigDecimal("655.957"))
                .actif(true)
                .paysOrigineNom("Sénégal")
                .paysDestinationNom("France")
                .deviseSourceCode("XOF")
                .deviseDestinationCode("EUR")
                .build();

        requestDTO = CorridorRequestDTO.builder()
                .paysOrigineId(1L)
                .paysDestinationId(2L)
                .deviseSourceId(1L)
                .deviseDestinationId(2L)
                .tauxChange(new BigDecimal("655.957"))
                .actif(true)
                .build();
    }

    @Test
    void getAll_shouldReturn200() throws Exception {
        when(corridorService.findAll()).thenReturn(Arrays.asList(responseDTO));

        mockMvc.perform(get("/api/v1/admin/corridors")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].paysOrigineNom").value("Sénégal"));
    }

    @Test
    void getActive_shouldReturn200() throws Exception {
        when(corridorService.findActive()).thenReturn(Arrays.asList(responseDTO));

        mockMvc.perform(get("/api/v1/admin/corridors/active")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void getById_shouldReturn200() throws Exception {
        when(corridorService.findById(1L)).thenReturn(responseDTO);

        mockMvc.perform(get("/api/v1/admin/corridors/1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void create_shouldReturn201() throws Exception {
        when(corridorService.save(any())).thenReturn(responseDTO);

        mockMvc.perform(post("/api/v1/admin/corridors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.paysOrigineNom").value("Sénégal"));
    }

    @Test
    void update_shouldReturn200() throws Exception {
        when(corridorService.update(eq(1L), any())).thenReturn(responseDTO);

        mockMvc.perform(put("/api/v1/admin/corridors/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk());
    }

    @Test
    void delete_shouldReturn204() throws Exception {
        doNothing().when(corridorService).delete(1L);

        mockMvc.perform(delete("/api/v1/admin/corridors/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void toggleStatus_shouldReturn200() throws Exception {
        doNothing().when(corridorService).toggleStatus(1L);
        when(corridorService.findById(1L)).thenReturn(responseDTO);

        mockMvc.perform(patch("/api/v1/admin/corridors/1/status")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
}