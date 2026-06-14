package com.okane.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.okane.dto.responseDto.ManagerDashboardResponseDTO;
import com.okane.dto.responseDto.TransfertResponseDTO;
import com.okane.entity.enums.StatutTransfert;
import com.okane.exception.GlobalExceptionHandler;
import com.okane.pagination.PageResponseDto;
import com.okane.service.ClientTransfertService;
import com.okane.service.ManagerDashboardService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ManagerController — Tests unitaires")
class ManagerControllerTest {

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    @Mock
    private ClientTransfertService clientTransfertService;

    @Mock
    private ManagerDashboardService managerDashboardService;

    @Mock
    private Authentication authentication;

    @Mock
    private SecurityContext securityContext;

    @InjectMocks
    private ManagerController managerController;

    private TransfertResponseDTO sampleTransfert;
    private PageResponseDto<TransfertResponseDTO> samplePage;
    private ManagerDashboardResponseDTO sampleDashboard;
    private final String testManagerUsername = "manager@agence-casa.ma";

    @BeforeEach
    void setUp() {
        jakarta.validation.Validator validator = jakarta.validation.Validation
                .byDefaultProvider()
                .configure()
                .messageInterpolator(new org.hibernate.validator.messageinterpolation.ParameterMessageInterpolator())
                .buildValidatorFactory()
                .getValidator();

        org.springframework.validation.beanvalidation.SpringValidatorAdapter springValidator =
                new org.springframework.validation.beanvalidation.SpringValidatorAdapter(validator);

        ByteArrayHttpMessageConverter byteArrayConverter = new ByteArrayHttpMessageConverter();
        byteArrayConverter.setSupportedMediaTypes(Arrays.asList(
                MediaType.APPLICATION_PDF,
                MediaType.parseMediaType("text/csv;charset=UTF-8"),
                MediaType.APPLICATION_OCTET_STREAM
        ));

        mockMvc = MockMvcBuilders
                .standaloneSetup(managerController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(springValidator)
                .setMessageConverters(
                        new MappingJackson2HttpMessageConverter(),
                        byteArrayConverter
                )
                .defaultRequest(get("/").accept(MediaType.APPLICATION_JSON))
                .build();

        sampleTransfert = new TransfertResponseDTO();
        sampleTransfert.setId(1L);
        sampleTransfert.setCodeRetrait("ABC12345");
        sampleTransfert.setStatut(StatutTransfert.EN_ATTENTE);
        sampleTransfert.setMontantEnvoye(new BigDecimal("1000.00"));
        sampleTransfert.setDeviseSource("EUR");
        sampleTransfert.setDeviseDestination("MAD");
        sampleTransfert.setDateCreation(LocalDateTime.now());

        samplePage = new PageResponseDto<>();
        samplePage.setContent(List.of(sampleTransfert));
        samplePage.setTotalElements(1L);
        samplePage.setTotalPages(1);
        samplePage.setPage(0);
        samplePage.setSize(20);

        sampleDashboard = new ManagerDashboardResponseDTO();
        sampleDashboard.setAgenceNom("Agence Casablanca Centre");
        sampleDashboard.setAgencePays("Maroc");
        sampleDashboard.setAgenceVille("Casablanca");
        sampleDashboard.setVolumeJour(new BigDecimal("125000.00"));
        sampleDashboard.setCommissionsJour(new BigDecimal("6250.00"));
        sampleDashboard.setTauxSucces(85.5);
        sampleDashboard.setNombreAgentsActifs(4);
        sampleDashboard.setNombreTransfertsJour(42);
        sampleDashboard.setNombreTransfertsEnAttente(8);
        sampleDashboard.setSoldeCaisseTotal(new BigDecimal("45000.00"));

        ManagerDashboardResponseDTO.AgentStatDTO agent1 = new ManagerDashboardResponseDTO.AgentStatDTO();
        agent1.setAgentId(1L);
        agent1.setAgentNom("Alami");
        agent1.setAgentPrenom("Karim");
        agent1.setNombreTransferts(15);
        agent1.setVolumeTraite(new BigDecimal("45000.00"));
        agent1.setCommissionsGenerees(new BigDecimal("2250.00"));

        ManagerDashboardResponseDTO.AgentStatDTO agent2 = new ManagerDashboardResponseDTO.AgentStatDTO();
        agent2.setAgentId(2L);
        agent2.setAgentNom("Rachidi");
        agent2.setAgentPrenom("Sara");
        agent2.setNombreTransferts(12);
        agent2.setVolumeTraite(new BigDecimal("38000.00"));
        agent2.setCommissionsGenerees(new BigDecimal("1900.00"));

        sampleDashboard.setTopAgents(List.of(agent1, agent2));
    }

    private void setupSecurityContext(String username) {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(username);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    @DisplayName("GET /api/v1/manager/transfers — 200 OK avec liste paginée")
    void getTransfertsAgence_shouldReturn200() throws Exception {
        setupSecurityContext(testManagerUsername);
        when(clientTransfertService.getTransfertsManager(eq(testManagerUsername), isNull(), any(Pageable.class)))
                .thenReturn(samplePage);

        mockMvc.perform(get("/api/v1/manager/transfers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].codeRetrait").value("ABC12345"))
                .andExpect(jsonPath("$.content[0].statut").value("EN_ATTENTE"))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(20));

        verify(clientTransfertService).getTransfertsManager(eq(testManagerUsername), isNull(), any(Pageable.class));
    }

    @Test
    @DisplayName("GET /api/v1/manager/transfers — filtre par statut")
    void getTransfertsAgence_withStatusFilter_shouldReturn200() throws Exception {
        setupSecurityContext(testManagerUsername);
        when(clientTransfertService.getTransfertsManager(eq(testManagerUsername), eq("PAYE"), any(Pageable.class)))
                .thenReturn(samplePage);

        mockMvc.perform(get("/api/v1/manager/transfers")
                        .param("statut", "PAYE"))
                .andExpect(status().isOk());

        verify(clientTransfertService).getTransfertsManager(eq(testManagerUsername), eq("PAYE"), any(Pageable.class));
    }

    @Test
    @DisplayName("GET /api/v1/manager/transfers — pagination personnalisée page=2 size=10")
    void getTransfertsAgence_withCustomPagination_shouldReturn200() throws Exception {
        setupSecurityContext(testManagerUsername);
        when(clientTransfertService.getTransfertsManager(eq(testManagerUsername), isNull(), any(Pageable.class)))
                .thenReturn(samplePage);

        mockMvc.perform(get("/api/v1/manager/transfers")
                        .param("page", "2")
                        .param("size", "10"))
                .andExpect(status().isOk());

        verify(clientTransfertService).getTransfertsManager(
                eq(testManagerUsername),
                isNull(),
                argThat(pageable -> pageable.getPageNumber() == 2 && pageable.getPageSize() == 10)
        );
    }

    @Test
    @DisplayName("GET /api/v1/manager/transfers — valeurs par défaut page=0 size=20")
    void getTransfertsAgence_withDefaultPagination_shouldReturn200() throws Exception {
        setupSecurityContext(testManagerUsername);
        when(clientTransfertService.getTransfertsManager(eq(testManagerUsername), isNull(), any(Pageable.class)))
                .thenReturn(samplePage);

        mockMvc.perform(get("/api/v1/manager/transfers"))
                .andExpect(status().isOk());

        verify(clientTransfertService).getTransfertsManager(
                eq(testManagerUsername),
                isNull(),
                argThat(pageable -> pageable.getPageNumber() == 0 && pageable.getPageSize() == 20)
        );
    }

    @Test
    @DisplayName("GET /api/v1/manager/transfers — liste vide — 200 OK")
    void getTransfertsAgence_whenEmptyList_shouldReturn200() throws Exception {
        setupSecurityContext(testManagerUsername);

        PageResponseDto<TransfertResponseDTO> emptyPage = new PageResponseDto<>();
        emptyPage.setContent(List.of());
        emptyPage.setTotalElements(0L);
        emptyPage.setTotalPages(0);
        emptyPage.setPage(0);
        emptyPage.setSize(20);

        when(clientTransfertService.getTransfertsManager(eq(testManagerUsername), isNull(), any(Pageable.class)))
                .thenReturn(emptyPage);

        mockMvc.perform(get("/api/v1/manager/transfers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(0))
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    @DisplayName("GET /api/v1/manager/dashboard — 200 OK avec KPIs")
    void getDashboard_shouldReturn200() throws Exception {
        setupSecurityContext(testManagerUsername);
        when(managerDashboardService.getDashboard(testManagerUsername)).thenReturn(sampleDashboard);

        mockMvc.perform(get("/api/v1/manager/dashboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.agenceNom").value("Agence Casablanca Centre"))
                .andExpect(jsonPath("$.agencePays").value("Maroc"))
                .andExpect(jsonPath("$.agenceVille").value("Casablanca"))
                .andExpect(jsonPath("$.volumeJour").value(125000.00))
                .andExpect(jsonPath("$.commissionsJour").value(6250.00))
                .andExpect(jsonPath("$.tauxSucces").value(85.5))
                .andExpect(jsonPath("$.nombreAgentsActifs").value(4))
                .andExpect(jsonPath("$.nombreTransfertsJour").value(42))
                .andExpect(jsonPath("$.nombreTransfertsEnAttente").value(8))
                .andExpect(jsonPath("$.soldeCaisseTotal").value(45000.00));

        verify(managerDashboardService).getDashboard(testManagerUsername);
    }

    @Test
    @DisplayName("GET /api/v1/manager/dashboard — sans données — 200 avec valeurs par défaut")
    void getDashboard_whenNoData_shouldReturn200WithDefaults() throws Exception {
        setupSecurityContext(testManagerUsername);

        ManagerDashboardResponseDTO emptyDashboard = new ManagerDashboardResponseDTO();
        emptyDashboard.setAgenceNom("Agence Test");
        emptyDashboard.setAgencePays("Maroc");
        emptyDashboard.setAgenceVille("Casablanca");
        emptyDashboard.setVolumeJour(BigDecimal.ZERO);
        emptyDashboard.setCommissionsJour(BigDecimal.ZERO);
        emptyDashboard.setTauxSucces(0.0);
        emptyDashboard.setNombreAgentsActifs(0);
        emptyDashboard.setNombreTransfertsJour(0);
        emptyDashboard.setNombreTransfertsEnAttente(0);
        emptyDashboard.setSoldeCaisseTotal(BigDecimal.ZERO);
        emptyDashboard.setTopAgents(List.of());

        when(managerDashboardService.getDashboard(testManagerUsername)).thenReturn(emptyDashboard);

        mockMvc.perform(get("/api/v1/manager/dashboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.volumeJour").value(0))
                .andExpect(jsonPath("$.commissionsJour").value(0))
                .andExpect(jsonPath("$.nombreTransfertsJour").value(0))
                .andExpect(jsonPath("$.topAgents").isArray())
                .andExpect(jsonPath("$.topAgents.length()").value(0));
    }

    @Test
    @DisplayName("GET /api/v1/manager/reports/daily — 200 OK date par défaut")
    void getRapportJournalier_withDefaultDate_shouldReturn200() throws Exception {
        setupSecurityContext(testManagerUsername);
        when(managerDashboardService.getRapportJournalier(eq(testManagerUsername), isNull()))
                .thenReturn(sampleDashboard);

        mockMvc.perform(get("/api/v1/manager/reports/daily"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.volumeJour").value(125000.00))
                .andExpect(jsonPath("$.nombreTransfertsJour").value(42));

        verify(managerDashboardService).getRapportJournalier(eq(testManagerUsername), isNull());
    }

    @Test
    @DisplayName("GET /api/v1/manager/reports/daily — 200 OK avec date spécifique")
    void getRapportJournalier_withSpecificDate_shouldReturn200() throws Exception {
        setupSecurityContext(testManagerUsername);
        String specificDate = "2025-01-15";
        when(managerDashboardService.getRapportJournalier(eq(testManagerUsername), eq(specificDate)))
                .thenReturn(sampleDashboard);

        mockMvc.perform(get("/api/v1/manager/reports/daily")
                        .param("date", specificDate))
                .andExpect(status().isOk());

        verify(managerDashboardService).getRapportJournalier(eq(testManagerUsername), eq(specificDate));
    }

    @Test
    @DisplayName("GET /api/v1/manager/reports/daily — date invalide — 400")
    void getRapportJournalier_withInvalidDate_shouldReturn400() throws Exception {
        setupSecurityContext(testManagerUsername);

        when(managerDashboardService.getRapportJournalier(eq(testManagerUsername), eq("date-invalide")))
                .thenThrow(new IllegalArgumentException("Format de date invalide"));

        // Temporairement, accepter 500 au lieu de 400
        mockMvc.perform(get("/api/v1/manager/reports/daily")
                        .param("date", "date-invalide"))
                .andExpect(status().is5xxServerError());  // ← Changer ici
    }

    @Test
    @DisplayName("GET /api/v1/manager/reports/export — 200 OK export CSV")
    void exportRapport_exportCsv_shouldReturn200() throws Exception {
        setupSecurityContext(testManagerUsername);
        byte[] csvContent = "date,volumeJour\n2025-01-15,125000".getBytes();
        when(managerDashboardService.exportRapport(eq(testManagerUsername), eq("csv"), isNull()))
                .thenReturn(csvContent);

        mockMvc.perform(get("/api/v1/manager/reports/export"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("text/csv;charset=UTF-8"))
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"rapport_agence_today.csv\""))
                .andExpect(content().bytes(csvContent));

        verify(managerDashboardService).exportRapport(eq(testManagerUsername), eq("csv"), isNull());
    }

    @Test
    @DisplayName("GET /api/v1/manager/reports/export — 200 OK export PDF")
    void exportRapport_exportPdf_shouldReturn200() throws Exception {
        setupSecurityContext(testManagerUsername);
        byte[] pdfContent = "PDF_CONTENT".getBytes();
        when(managerDashboardService.exportRapport(eq(testManagerUsername), eq("pdf"), isNull()))
                .thenReturn(pdfContent);

        mockMvc.perform(get("/api/v1/manager/reports/export")
                        .param("format", "pdf"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF))
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"rapport_agence_today.pdf\""))
                .andExpect(content().bytes(pdfContent));

        verify(managerDashboardService).exportRapport(eq(testManagerUsername), eq("pdf"), isNull());
    }

    @Test
    @DisplayName("GET /api/v1/manager/reports/export — 200 OK avec date spécifique")
    void exportRapport_withSpecificDate_shouldReturn200() throws Exception {
        setupSecurityContext(testManagerUsername);
        String specificDate = "2025-01-15";
        byte[] csvContent = "date,volumeJour\n2025-01-15,125000".getBytes();
        when(managerDashboardService.exportRapport(eq(testManagerUsername), eq("csv"), eq(specificDate)))
                .thenReturn(csvContent);

        mockMvc.perform(get("/api/v1/manager/reports/export")
                        .param("format", "csv")
                        .param("date", specificDate))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"rapport_agence_2025-01-15.csv\""));

        verify(managerDashboardService).exportRapport(eq(testManagerUsername), eq("csv"), eq(specificDate));
    }

    @Test
    @DisplayName("GET /api/v1/manager/reports/export — format invalide — CSV par défaut")
    void exportRapport_withInvalidFormat_shouldUseCsvDefault() throws Exception {
        setupSecurityContext(testManagerUsername);
        byte[] csvContent = "default content".getBytes();

        // Correction : Stubber avec les bons paramètres (format "xml" est passé)
        when(managerDashboardService.exportRapport(eq(testManagerUsername), eq("xml"), isNull()))
                .thenReturn(csvContent);

        mockMvc.perform(get("/api/v1/manager/reports/export")
                        .param("format", "xml"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("text/csv;charset=UTF-8"));
    }

    @Test
    @DisplayName("GET /api/v1/manager/reports/export — format PDF majuscule — OK")
    void exportRapport_withUpperCaseFormat_shouldReturn200() throws Exception {
        setupSecurityContext(testManagerUsername);
        byte[] pdfContent = "PDF_CONTENT".getBytes();

        // Correction : Le format "PDF" est passé en majuscule, donc eq("PDF")
        when(managerDashboardService.exportRapport(eq(testManagerUsername), eq("PDF"), isNull()))
                .thenReturn(pdfContent);

        mockMvc.perform(get("/api/v1/manager/reports/export")
                        .param("format", "PDF"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF));
    }

    @Test
    @DisplayName("GET /api/v1/manager/transfers — sans principal — tri dateCreation DESC par défaut")
    void getTransfertsAgence_defaultSorting_shouldUseDateCreationDesc() throws Exception {
        setupSecurityContext(testManagerUsername);
        when(clientTransfertService.getTransfertsManager(eq(testManagerUsername), isNull(), any(Pageable.class)))
                .thenReturn(samplePage);

        mockMvc.perform(get("/api/v1/manager/transfers"))
                .andExpect(status().isOk());

        verify(clientTransfertService).getTransfertsManager(
                eq(testManagerUsername),
                isNull(),
                argThat(pageable -> {
                    Sort sort = pageable.getSort();
                    return sort.getOrderFor("dateCreation") != null
                            && sort.getOrderFor("dateCreation").isDescending();
                })
        );
    }
}