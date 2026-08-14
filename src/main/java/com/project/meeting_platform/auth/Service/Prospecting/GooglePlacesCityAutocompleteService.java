package com.project.meeting_platform.auth.Service.Prospecting;

import com.fasterxml.jackson.databind.JsonNode;
import com.project.meeting_platform.auth.dto.Prospecting.CitySuggestion;
import com.project.meeting_platform.config.GooglePlacesProperties;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class GooglePlacesCityAutocompleteService {

    private static final Map<String, String> BRAZILIAN_STATES = Map.ofEntries(
            Map.entry("ACRE", "AC"), Map.entry("ALAGOAS", "AL"), Map.entry("AMAPÁ", "AP"),
            Map.entry("AMAZONAS", "AM"), Map.entry("BAHIA", "BA"), Map.entry("CEARÁ", "CE"),
            Map.entry("DISTRITO FEDERAL", "DF"), Map.entry("ESPÍRITO SANTO", "ES"),
            Map.entry("GOIÁS", "GO"), Map.entry("MARANHÃO", "MA"), Map.entry("MATO GROSSO", "MT"),
            Map.entry("MATO GROSSO DO SUL", "MS"), Map.entry("MINAS GERAIS", "MG"),
            Map.entry("PARÁ", "PA"), Map.entry("PARAÍBA", "PB"), Map.entry("PARANÁ", "PR"),
            Map.entry("PERNAMBUCO", "PE"), Map.entry("PIAUÍ", "PI"), Map.entry("RIO DE JANEIRO", "RJ"),
            Map.entry("RIO GRANDE DO NORTE", "RN"), Map.entry("RIO GRANDE DO SUL", "RS"),
            Map.entry("RONDÔNIA", "RO"), Map.entry("RORAIMA", "RR"), Map.entry("SANTA CATARINA", "SC"),
            Map.entry("SÃO PAULO", "SP"), Map.entry("SERGIPE", "SE"), Map.entry("TOCANTINS", "TO")
    );

    private final RestClient restClient;
    private final GooglePlacesProperties properties;

    public GooglePlacesCityAutocompleteService(
            RestClient.Builder restClientBuilder,
            GooglePlacesProperties properties
    ) {
        this.properties = properties;

        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory();
        requestFactory.setReadTimeout(Duration.ofSeconds(10));
        this.restClient = restClientBuilder
                .baseUrl(properties.baseUrl())
                .requestFactory(requestFactory)
                .build();
    }

    public List<CitySuggestion> suggest(String input, String state) {
        if (properties.placesApiKey() == null || properties.placesApiKey().isBlank()) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "A busca de cidades ainda não está configurada.");
        }

        try {
            JsonNode response = restClient.post()
                    .uri("/places:autocomplete")
                    .header("X-Goog-Api-Key", properties.placesApiKey())
                    .body(Map.of(
                            "input", query(input, state),
                            "languageCode", "pt-BR",
                            "includedRegionCodes", List.of("br"),
                            "includedPrimaryTypes", List.of("(cities)")
                    ))
                    .retrieve()
                    .body(JsonNode.class);

            return mapSuggestions(response, state);
        } catch (RestClientException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Não foi possível sugerir cidades agora.", exception);
        }
    }

    private List<CitySuggestion> mapSuggestions(JsonNode response, String requestedState) {
        List<CitySuggestion> suggestions = new ArrayList<>();
        if (response == null) {
            return suggestions;
        }

        for (JsonNode suggestion : response.path("suggestions")) {
            JsonNode prediction = suggestion.path("placePrediction");
            String city = text(prediction.path("structuredFormat").path("mainText"), "text");
            String label = text(prediction.path("text"), "text");
            String details = text(prediction.path("structuredFormat").path("secondaryText"), "text");

            if (city == null || city.isBlank()) {
                continue;
            }

            String state = stateFrom(label + " " + details);
            if (hasState(requestedState) && state != null && !requestedState.trim().equalsIgnoreCase(state)) {
                continue;
            }

            suggestions.add(new CitySuggestion(city, state, label));
        }

        return suggestions;
    }

    private String query(String input, String state) {
        if (!hasState(state)) {
            return input.trim() + ", Brasil";
        }
        return input.trim() + ", " + state.trim().toUpperCase(Locale.ROOT) + ", Brasil";
    }

    private boolean hasState(String state) {
        return state != null && state.trim().matches("^[A-Za-z]{2}$");
    }

    private String stateFrom(String text) {
        String normalized = text == null ? "" : text.toUpperCase(Locale.ROOT);
        if (normalized.contains("MATO GROSSO DO SUL")) {
            return "MS";
        }
        for (Map.Entry<String, String> state : BRAZILIAN_STATES.entrySet()) {
            if (normalized.contains(state.getKey())) {
                return state.getValue();
            }
        }
        return null;
    }

    private String text(JsonNode node, String field) {
        JsonNode value = node.path(field);
        return value.isMissingNode() || value.isNull() ? null : value.asText();
    }
}
