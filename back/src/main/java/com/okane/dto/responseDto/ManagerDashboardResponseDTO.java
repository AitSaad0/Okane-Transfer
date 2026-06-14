package com.okane.dto.responseDto;

import java.math.BigDecimal;
import java.util.List;

public class ManagerDashboardResponseDTO {

    // KPIs du jour
    private BigDecimal volumeJour;          // total MAD envoyé ce jour
    private BigDecimal commissionsJour;     // commissions générées aujourd'hui
    private double tauxSucces;             // % transferts PAYE / total
    private int nombreAgentsActifs;         // agents avec caisse ouverte ce jour
    private int nombreTransfertsJour;
    private int nombreTransfertsEnAttente;

    // Caisse globale agence (somme des caisses agents)
    private BigDecimal soldeCaisseTotal;

    // Top agents du jour
    private List<AgentStatDTO> topAgents;

    // Infos agence
    private String agenceNom;
    private String agencePays;
    private String agenceVille;

    public ManagerDashboardResponseDTO() {}

    // --- Nested DTO ---
    public static class AgentStatDTO {
        private Long agentId;
        private String agentNom;
        private String agentPrenom;
        private int nombreTransferts;
        private BigDecimal volumeTraite;
        private BigDecimal commissionsGenerees;

        public AgentStatDTO() {}

        public Long getAgentId() { return agentId; }
        public void setAgentId(Long agentId) { this.agentId = agentId; }
        public String getAgentNom() { return agentNom; }
        public void setAgentNom(String agentNom) { this.agentNom = agentNom; }
        public String getAgentPrenom() { return agentPrenom; }
        public void setAgentPrenom(String agentPrenom) { this.agentPrenom = agentPrenom; }
        public int getNombreTransferts() { return nombreTransferts; }
        public void setNombreTransferts(int nombreTransferts) { this.nombreTransferts = nombreTransferts; }
        public BigDecimal getVolumeTraite() { return volumeTraite; }
        public void setVolumeTraite(BigDecimal volumeTraite) { this.volumeTraite = volumeTraite; }
        public BigDecimal getCommissionsGenerees() { return commissionsGenerees; }
        public void setCommissionsGenerees(BigDecimal commissionsGenerees) { this.commissionsGenerees = commissionsGenerees; }
    }

    // --- Getters & Setters ---
    public BigDecimal getVolumeJour() { return volumeJour; }
    public void setVolumeJour(BigDecimal volumeJour) { this.volumeJour = volumeJour; }
    public BigDecimal getCommissionsJour() { return commissionsJour; }
    public void setCommissionsJour(BigDecimal commissionsJour) { this.commissionsJour = commissionsJour; }
    public double getTauxSucces() { return tauxSucces; }
    public void setTauxSucces(double tauxSucces) { this.tauxSucces = tauxSucces; }
    public int getNombreAgentsActifs() { return nombreAgentsActifs; }
    public void setNombreAgentsActifs(int nombreAgentsActifs) { this.nombreAgentsActifs = nombreAgentsActifs; }
    public int getNombreTransfertsJour() { return nombreTransfertsJour; }
    public void setNombreTransfertsJour(int nombreTransfertsJour) { this.nombreTransfertsJour = nombreTransfertsJour; }
    public int getNombreTransfertsEnAttente() { return nombreTransfertsEnAttente; }
    public void setNombreTransfertsEnAttente(int nombreTransfertsEnAttente) { this.nombreTransfertsEnAttente = nombreTransfertsEnAttente; }
    public BigDecimal getSoldeCaisseTotal() { return soldeCaisseTotal; }
    public void setSoldeCaisseTotal(BigDecimal soldeCaisseTotal) { this.soldeCaisseTotal = soldeCaisseTotal; }
    public List<AgentStatDTO> getTopAgents() { return topAgents; }
    public void setTopAgents(List<AgentStatDTO> topAgents) { this.topAgents = topAgents; }
    public String getAgenceNom() { return agenceNom; }
    public void setAgenceNom(String agenceNom) { this.agenceNom = agenceNom; }
    public String getAgencePays() { return agencePays; }
    public void setAgencePays(String agencePays) { this.agencePays = agencePays; }
    public String getAgenceVille() { return agenceVille; }
    public void setAgenceVille(String agenceVille) { this.agenceVille = agenceVille; }
}