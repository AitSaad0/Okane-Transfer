package com.okane.controller;

import com.okane.dto.requestDto.GrilleTarifaireRequestDTO;
import com.okane.dto.requestDto.SimulationRequestDTO;
import com.okane.dto.responseDto.GrilleTarifaireResponseDTO;
import com.okane.dto.responseDto.SimulationResponseDTO;
import com.okane.service.GrilleTarifaireService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
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
class GrilleTarifaireControllerTest {

    private MockMvc mockMvc;

    @Mock
    private GrilleTarifaireService grilleTarifaireService;

    @InjectMocks
    private GrilleTarifaireController grilleTarifaireController;

    private ObjectMapper objectMapper;
    private GrilleTarifaireResponseDTO responseDTO;
    private GrilleTarifaireRequestDTO requestDTO;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();

        // ByteArrayHttpMessageConverter is required for byte[] export endpoints
        ByteArrayHttpMessageConverter byteArrayConverter = new ByteArrayHttpMessageConverter();
        byteArrayConverter.setSupportedMediaTypes(Arrays.asList(
                MediaType.APPLICATION_PDF,
                MediaType.TEXT_PLAIN,
                MediaType.APPLICATION_OCTET_STREAM
        ));

        mockMvc = MockMvcBuilders
                .standaloneSetup(grilleTarifaireController)
                .setMessageConverters(
                        new MappingJackson2HttpMessageConverter(),
                        byteArrayConverter
                )
                .build();

        responseDTO = GrilleTarifaireResponseDTO.builder()
                .id(1L)
                .montantMin(new BigDecimal("0"))
                .montantMax(new BigDecimal("500"))
                .fraisFixe(new BigDecimal("25"))
                .pourcentageFrais(0.0)
                .partAgence(70.0)
                .corridorId(1L)
                .corridorPaysOrigineNom("Sénégal")
                .corridorPaysDestinationNom("France")
                .build();

        requestDTO = GrilleTarifaireRequestDTO.builder()
                .corridorId(1L)
                .montantMin(new BigDecimal("0"))
                .montantMax(new BigDecimal("500"))
                .fraisFixe(new BigDecimal("25"))
                .pourcentageFrais(0.0)
                .partAgence(70.0)
                .build();
    }

    @Test
    void getAll_shouldReturn200() throws Exception {
        when(grilleTarifaireService.findAll()).thenReturn(Arrays.asList(responseDTO));

        mockMvc.perform(get("/api/v1/admin/fee-grids")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].corridorPaysOrigineNom").value("Sénégal"));
    }

    @Test
    void getById_shouldReturn200() throws Exception {
        when(grilleTarifaireService.findById(1L)).thenReturn(responseDTO);

        mockMvc.perform(get("/api/v1/admin/fee-grids/1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void getByCorridor_shouldReturn200() throws Exception {
        when(grilleTarifaireService.findByCorridorId(1L)).thenReturn(Arrays.asList(responseDTO));

        mockMvc.perform(get("/api/v1/admin/fee-grids/corridor/1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void create_shouldReturn201() throws Exception {
        when(grilleTarifaireService.save(any())).thenReturn(responseDTO);

        mockMvc.perform(post("/api/v1/admin/fee-grids")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.fraisFixe").value(25));
    }

    @Test
    void update_shouldReturn200() throws Exception {
        when(grilleTarifaireService.update(eq(1L), any())).thenReturn(responseDTO);

        mockMvc.perform(put("/api/v1/admin/fee-grids/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk());
    }

    @Test
    void delete_shouldReturn204() throws Exception {
        doNothing().when(grilleTarifaireService).delete(1L);

        mockMvc.perform(delete("/api/v1/admin/fee-grids/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void simulate_shouldReturn200() throws Exception {
        SimulationRequestDTO simDTO = SimulationRequestDTO.builder()
                .corridorId(1L)
                .montant(new BigDecimal("400"))
                .build();

        SimulationResponseDTO simResponse = SimulationResponseDTO.builder()
                .montantEnvoye(new BigDecimal("400"))
                .fraisFixe(new BigDecimal("25"))
                .fraisTotal(new BigDecimal("25"))
                .montantRecu(new BigDecimal("375"))
                .build();

        when(grilleTarifaireService.simulate(any())).thenReturn(simResponse);

        mockMvc.perform(post("/api/v1/fees/simulate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(simDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fraisFixe").value(25));
    }

    @Test
    void exportCsv_shouldReturn200() throws Exception {
        when(grilleTarifaireService.exportCsv()).thenReturn("csv,data".getBytes());

        mockMvc.perform(get("/api/v1/admin/fee-grids/export?format=csv")
                        .accept(MediaType.TEXT_PLAIN, MediaType.APPLICATION_OCTET_STREAM))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"grilles_tarifaires.csv\""));
    }

    @Test
    void exportPdf_shouldReturn200() throws Exception {
        when(grilleTarifaireService.exportPdf()).thenReturn("pdf".getBytes());

        mockMvc.perform(get("/api/v1/admin/fee-grids/export?format=pdf")
                        .accept(MediaType.APPLICATION_PDF, MediaType.APPLICATION_OCTET_STREAM))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"grilles_tarifaires.pdf\""));
    }
}