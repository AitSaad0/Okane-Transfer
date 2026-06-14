package com.okane.service.chatbot;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.okane.dto.chatbot.CallResult;
import com.okane.dto.chatbot.RechercheCorridorResultat;
import com.okane.dto.chatbot.RechercheGrilleTarifaireResultat;
import com.okane.dto.chatbot.RechercheTransfertResultat;
import com.okane.dto.chatbot.ToolCall;
import com.okane.dto.chatbot.ToolDefinition;
import com.okane.entity.Corridor;
import com.okane.entity.GrilleTarifaire;
import com.okane.entity.Transfert;
import com.okane.entity.TransfertMobile;
import com.okane.repository.*;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;

@Component
public class ToolRegistry {

    private final TransfertRepository transfertRepository;
    private final TransfertMobileRepository transfertMobileRepository;
    private final ClientRepository clientRepository;
    private final CorridorRepository corridorRepository;
    private final GrilleTarifaireRepository grilleTarifaireRepository;
    private final ObjectMapper objectMapper;

    public ToolRegistry(TransfertRepository transfertRepository,
                         TransfertMobileRepository transfertMobileRepository,
                         ClientRepository clientRepository,
                         CorridorRepository corridorRepository,
                         GrilleTarifaireRepository grilleTarifaireRepository) {
        this.transfertRepository = transfertRepository;
        this.transfertMobileRepository = transfertMobileRepository;
        this.clientRepository = clientRepository;
        this.corridorRepository = corridorRepository;
        this.grilleTarifaireRepository = grilleTarifaireRepository;
        this.objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    public List<ToolDefinition> getDefinitions() {
        return List.of(
                toolDefinition("getTransfertParReference",
                        "Chercher un transfert par sa référence unique",
                        params(Map.of("reference", schemaString("La référence du transfert"))), List.of("reference")),

                toolDefinition("getTransfertParCodeRetrait",
                        "Chercher un transfert par le code de retrait",
                        params(Map.of("codeRetrait", schemaString("Le code de retrait à 8 caractères alphanumériques"))), List.of("codeRetrait")),

                toolDefinition("getTransfertsParTelephoneExpediteur",
                        "Lister les transferts liés à un numéro de téléphone de l'expéditeur",
                        params(Map.of("telephone", schemaString("Numéro de téléphone de l'expéditeur"))), List.of("telephone")),

                toolDefinition("getTransfertsParTelephoneBeneficiaire",
                        "Lister les transferts liés à un numéro de téléphone du bénéficiaire",
                        params(Map.of("telephone", schemaString("Numéro de téléphone du bénéficiaire"))), List.of("telephone")),

                toolDefinition("getTransfertsParCINExpediteur",
                        "Lister les transferts liés au CIN/passeport de l'expéditeur",
                        params(Map.of("cin", schemaString("Numéro de CIN ou passeport de l'expéditeur"))), List.of("cin")),

                toolDefinition("getTransfertsParCINBeneficiaire",
                        "Lister les transferts liés au CIN/passeport du bénéficiaire",
                        params(Map.of("cin", schemaString("Numéro de CIN ou passeport du bénéficiaire"))), List.of("cin")),

                toolDefinition("getTransfertsDuClient",
                        "Lister les derniers transferts du client connecté",
                        params(Map.of("limite", schemaInteger("Nombre maximum de transferts à retourner (défaut: 5)"))), List.of()),

                toolDefinition("getInfoClient",
                        "Obtenir les informations personnelles du client connecté",
                        params(Map.of()), List.of()),

                toolDefinition("getCorridors",
                        "Lister tous les corridors disponibles (pays source → pays destination)",
                        params(Map.of(
                                "paysSource", schemaString("Filtrer par pays source (optionnel)"),
                                "paysDestination", schemaString("Filtrer par pays destination (optionnel)")
                        )), List.of()),

                toolDefinition("getTranchesGrilleTarifaire",
                        "Obtenir les tranches de frais pour un corridor donné",
                        params(Map.of("corridorId", schemaInteger("ID du corridor"))), List.of("corridorId"))
        );
    }

    public CallResult executer(ToolCall toolCall, Long clientId) {
        try {
            CallResult cr = switch (toolCall.functionName()) {
                case "getTransfertParReference" -> execGetTransfertParReference(toolCall, clientId);
                case "getTransfertParCodeRetrait" -> execGetTransfertParCodeRetrait(toolCall, clientId);
                case "getTransfertsParTelephoneExpediteur" -> execGetTransfertsParTelephone(toolCall, clientId, true);
                case "getTransfertsParTelephoneBeneficiaire" -> execGetTransfertsParTelephone(toolCall, clientId, false);
                case "getTransfertsParCINExpediteur" -> execGetTransfertsParCIN(toolCall, clientId, true);
                case "getTransfertsParCINBeneficiaire" -> execGetTransfertsParCIN(toolCall, clientId, false);
                case "getTransfertsDuClient" -> execGetTransfertsDuClient(toolCall, clientId);
                case "getInfoClient" -> execGetInfoClient(clientId);
                case "getCorridors" -> execGetCorridors(toolCall);
                case "getTranchesGrilleTarifaire" -> execGetTranchesGrilleTarifaire(toolCall);
                default -> new CallResult(toolCall.id(), toolCall.functionName(), toolCall.arguments(),
                        "{\"error\": \"Fonction inconnue: " + toolCall.functionName() + "\"}");
            };
            return cr;
        } catch (Exception e) {
            return new CallResult(toolCall.id(), toolCall.functionName(), toolCall.arguments(),
                    "{\"error\": \"" + e.getMessage() + "\"}");
        }
    }

    private CallResult execGetTransfertParReference(ToolCall tc, Long clientId) throws Exception {
        String ref = argString(tc, "reference");
        Long id = Long.parseLong(ref.replaceAll("\\D", ""));
        Optional<Transfert> opt = transfertRepository.findById(id);
        if (opt.isEmpty()) {
            return jsonResult(tc, "null");
        }
        Transfert t = opt.get();
        if (!t.getExpediteur().getId().equals(clientId) && !t.getBeneficiaire().getId().equals(clientId)) {
            return jsonResult(tc, "null");
        }
        return transfertToResult(tc, t);
    }

    private CallResult execGetTransfertParCodeRetrait(ToolCall tc, Long clientId) throws Exception {
        String code = argString(tc, "codeRetrait");
        Optional<Transfert> opt = transfertRepository.findByCodeRetraitAndClientId(code, clientId);
        if (opt.isEmpty()) {
            return jsonResult(tc, "null");
        }
        return transfertToResult(tc, opt.get());
    }

    private CallResult execGetTransfertsParTelephone(ToolCall tc, Long clientId, boolean expediteur) throws Exception {
        String tel = argString(tc, "telephone");
        List<Transfert> list = expediteur
                ? transfertRepository.findByExpediteurTelephoneAndClientId(tel, clientId)
                : transfertRepository.findByBeneficiaireTelephoneAndClientId(tel, clientId);
        return transfertsToListResult(tc, list);
    }

    private CallResult execGetTransfertsParCIN(ToolCall tc, Long clientId, boolean expediteur) throws Exception {
        String cin = argString(tc, "cin");
        List<Transfert> list = expediteur
                ? transfertRepository.findByExpediteurCINAndClientId(cin, clientId)
                : transfertRepository.findByBeneficiaireCINAndClientId(cin, clientId);
        return transfertsToListResult(tc, list);
    }

    private CallResult execGetTransfertsDuClient(ToolCall tc, Long clientId) throws Exception {
        int limite = 5;
        try {
            limite = Integer.parseInt(argString(tc, "limite"));
        } catch (Exception ignored) {}
        List<Transfert> expediteur = transfertRepository.findByExpediteurIdOrderByDateCreationDesc(clientId);
        List<Transfert> beneficiaire = transfertRepository.findByBeneficiaireIdOrderByDateCreationDesc(clientId);
        Set<Transfert> merged = new LinkedHashSet<>();
        merged.addAll(expediteur);
        merged.addAll(beneficiaire);
        List<Transfert> list = new ArrayList<>(merged);
        list.sort((a, b) -> b.getDateCreation().compareTo(a.getDateCreation()));
        if (list.size() > limite) list = list.subList(0, limite);
        return transfertsToListResult(tc, list);
    }

    private CallResult execGetInfoClient(Long clientId) throws Exception {
        var opt = clientRepository.findById(clientId);
        if (opt.isEmpty()) return new CallResult("", "getInfoClient", "", "{}");
        var c = opt.get();
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("nom", c.getNom());
        data.put("prenom", c.getPrenom());
        data.put("email", c.getEmail());
        data.put("telephone", c.getTelephone());
        data.put("cin", c.getNumPieceIdentite());
        data.put("dateNaissance", c.getDateNaissance() != null ? c.getDateNaissance().toString() : null);
        return new CallResult("", "getInfoClient", "", objectMapper.writeValueAsString(data));
    }

    private CallResult execGetCorridors(ToolCall tc) throws Exception {
        List<Corridor> corridors;
        try {
            String paysSrc = argString(tc, "paysSource");
            String paysDst = argString(tc, "paysDestination");
            corridors = corridorRepository.findByActifTrue();
        } catch (Exception e) {
            corridors = corridorRepository.findByActifTrue();
        }
        List<RechercheCorridorResultat> results = corridors.stream().map(c -> new RechercheCorridorResultat(
                c.getId(),
                c.getPaysOrigine().getNom(),
                c.getPaysDestination().getNom(),
                c.getDeviseSource().getCode(),
                c.getDeviseDestination().getCode(),
                c.getActif()
        )).toList();
        return new CallResult(tc.id(), tc.functionName(), tc.arguments(), objectMapper.writeValueAsString(results));
    }

    private CallResult execGetTranchesGrilleTarifaire(ToolCall tc) throws Exception {
        long corridorId = Long.parseLong(argString(tc, "corridorId"));
        List<GrilleTarifaire> grilles = grilleTarifaireRepository.findByCorridorId(corridorId);
        List<RechercheGrilleTarifaireResultat> results = grilles.stream().map(g -> new RechercheGrilleTarifaireResultat(
                corridorId, g.getMontantMin(), g.getMontantMax(),
                g.getFraisFixe(), BigDecimal.valueOf(g.getPourcentageFrais()),
                g.getCorridor().getDeviseSource().getCode()
        )).toList();
        return new CallResult(tc.id(), tc.functionName(), tc.arguments(), objectMapper.writeValueAsString(results));
    }

    private CallResult transfertToResult(ToolCall tc, Transfert t) throws Exception {
        String type = "CLASSIQUE";
        Optional<TransfertMobile> mobOpt = transfertMobileRepository.findById(t.getId());
        if (mobOpt.isPresent()) type = "MOBILE";

        RechercheTransfertResultat r = new RechercheTransfertResultat(
                String.valueOf(t.getId()),
                type,
                t.getStatut().name(),
                t.getMontantEnvoye(),
                t.getCorridor().getDeviseSource().getCode(),
                t.getBeneficiaire().getPrenom() + " " + t.getBeneficiaire().getNom(),
                t.getExpediteur().getPrenom() + " " + t.getExpediteur().getNom(),
                t.getCodeRetrait(),
                t.getDateCreation()
        );
        return new CallResult(tc.id(), tc.functionName(), tc.arguments(), objectMapper.writeValueAsString(r));
    }

    private CallResult transfertsToListResult(ToolCall tc, List<Transfert> list) throws Exception {
        List<RechercheTransfertResultat> results = new ArrayList<>();
        for (Transfert t : list) {
            String type = "CLASSIQUE";
            Optional<TransfertMobile> mobOpt = transfertMobileRepository.findByTransfertId(t.getId());
            if (mobOpt.isPresent()) type = "MOBILE";
            results.add(new RechercheTransfertResultat(
                    String.valueOf(t.getId()),
                    type,
                    t.getStatut().name(),
                    t.getMontantEnvoye(),
                    t.getCorridor().getDeviseSource().getCode(),
                    t.getBeneficiaire().getPrenom() + " " + t.getBeneficiaire().getNom(),
                    t.getExpediteur().getPrenom() + " " + t.getExpediteur().getNom(),
                    t.getCodeRetrait(),
                    t.getDateCreation()
            ));
        }
        return new CallResult(tc.id(), tc.functionName(), tc.arguments(), objectMapper.writeValueAsString(results));
    }

    private CallResult jsonResult(ToolCall tc, String json) {
        return new CallResult(tc.id(), tc.functionName(), tc.arguments(), json);
    }

    private ToolDefinition toolDefinition(String name, String description, Map<String, Object> properties, List<String> required) {
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("type", "object");
        params.put("properties", properties);
        params.put("required", required);
        return new ToolDefinition(name, description, params);
    }

    private Map<String, Object> params(Map<String, Object> props) {
        return props;
    }

    private Map<String, Object> schemaString(String description) {
        Map<String, Object> s = new LinkedHashMap<>();
        s.put("type", "string");
        s.put("description", description);
        return s;
    }

    private Map<String, Object> schemaInteger(String description) {
        Map<String, Object> s = new LinkedHashMap<>();
        s.put("type", "integer");
        s.put("description", description);
        return s;
    }

    private String argString(ToolCall tc, String key) throws JsonProcessingException {
        var args = objectMapper.readTree(tc.arguments());
        return args.path(key).asText();
    }
}
