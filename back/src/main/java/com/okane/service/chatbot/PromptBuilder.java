package com.okane.service.chatbot;

import com.okane.entity.Client;
import com.okane.entity.Transfert;
import com.okane.entity.chatbot.ChatbotMessage;
import com.okane.entity.chatbot.MessageRole;
import com.okane.repository.ClientRepository;
import com.okane.repository.TransfertRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class PromptBuilder {

    private final ClientRepository clientRepository;
    private final TransfertRepository transfertRepository;

    public PromptBuilder(ClientRepository clientRepository, TransfertRepository transfertRepository) {
        this.clientRepository = clientRepository;
        this.transfertRepository = transfertRepository;
    }

    public String builderContexte(Long clientId, String messageUtilisateur, List<ChatbotMessage> historique) {
        StringBuilder sb = new StringBuilder();

        Optional<Client> clientOpt = clientRepository.findById(clientId);
        if (clientOpt.isPresent()) {
            Client client = clientOpt.get();
            sb.append("Contexte client : ").append(client.getPrenom()).append(" ").append(client.getNom());
            sb.append(", email : ").append(client.getEmail());
            sb.append(", téléphone : ").append(client.getTelephone()).append("\n");
        }

        List<Transfert> transfertsExpediteur = transfertRepository.findByExpediteurIdOrderByDateCreationDesc(clientId);
        List<Transfert> transfertsBeneficiaire = transfertRepository.findByBeneficiaireIdOrderByDateCreationDesc(clientId);
        java.util.LinkedHashSet<Transfert> tousTransferts = new java.util.LinkedHashSet<>();
        tousTransferts.addAll(transfertsExpediteur);
        tousTransferts.addAll(transfertsBeneficiaire);
        List<Transfert> transferts = new java.util.ArrayList<>(tousTransferts);
        transferts.sort((a, b) -> b.getDateCreation().compareTo(a.getDateCreation()));
        if (!transferts.isEmpty()) {
            sb.append("Transferts récents du client :\n");
            transferts.stream().limit(5).forEach(t -> {
                String role = t.getExpediteur().getId().equals(clientId) ? "expéditeur" : "bénéficiaire";
                sb.append("- Réf: ").append(t.getId())
                        .append(" | Rôle: ").append(role)
                        .append(" | Montant: ").append(t.getMontantEnvoye())
                        .append(" | Statut: ").append(t.getStatut())
                        .append(" | Code retrait: ").append(t.getCodeRetrait())
                        .append(" | Date: ").append(t.getDateCreation())
                        .append("\n");
            });
        }

        if (historique != null && !historique.isEmpty()) {
            sb.append("\nHistorique de la conversation :\n");
            for (ChatbotMessage msg : historique) {
                String role = msg.getRole() == MessageRole.USER ? "Client" : "Assistant";
                sb.append(role).append(" : ").append(msg.getContent()).append("\n");
            }
        }

        return sb.toString();
    }

    public List<String> getQuickReplies(String reponseIA) {
        return List.of();
    }
}
