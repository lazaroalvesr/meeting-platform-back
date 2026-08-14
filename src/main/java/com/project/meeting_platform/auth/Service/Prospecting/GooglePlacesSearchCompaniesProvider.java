package com.project.meeting_platform.auth.Service.Prospecting;

import com.fasterxml.jackson.databind.JsonNode;
import com.project.meeting_platform.auth.dto.Prospecting.CompanySearchRequest;
import com.project.meeting_platform.auth.dto.Prospecting.CompanySearchResult;
import com.project.meeting_platform.config.GooglePlacesProperties;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.time.Duration;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Component
public class GooglePlacesSearchCompaniesProvider implements SearchCompaniesProvider {

    private static final String FIELD_MASK = String.join(",",
            "places.id",
            "places.displayName",
            "places.primaryTypeDisplayName",
            "places.formattedAddress",
            "places.websiteUri",
            "places.internationalPhoneNumber",
            "places.nationalPhoneNumber",
            "places.googleMapsUri"
    );

    private final RestClient restClient;
    private final GooglePlacesProperties properties;

    public GooglePlacesSearchCompaniesProvider(
            RestClient.Builder restClientBuilder,
            GooglePlacesProperties properties
    ) {
        this.properties = properties;
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory();
        requestFactory.setReadTimeout(Duration.ofSeconds(15));
        this.restClient = restClientBuilder
                .baseUrl(properties.baseUrl())
                .requestFactory(requestFactory)
                .build();
    }

    @Override
    public List<CompanySearchResult> search(CompanySearchRequest request, int limit) {
        if (properties.placesApiKey() == null || properties.placesApiKey().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "A busca de empresas ainda não está configurada. Defina GOOGLE_PLACES_API_KEY no servidor."
            );
        }

        String state = request.state().trim().toUpperCase(Locale.ROOT);
        String query = "%s em %s, %s, Brasil".formatted(request.segment().trim(), request.city().trim(), state);

        try {
            JsonNode response = restClient.post()
                    .uri("/places:searchText")
                    .header("X-Goog-Api-Key", properties.placesApiKey())
                    .header("X-Goog-FieldMask", FIELD_MASK)
                    .body(Map.of(
                            "textQuery", query,
                            "languageCode", "pt-BR",
                            "maxResultCount", limit
                    ))
                    .retrieve()
                    .body(JsonNode.class);

            return mapPlaces(response, request.city().trim(), state);
        } catch (RestClientException exception) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Não foi possível consultar o Google Places agora. Confira a chave e tente novamente.",
                    exception
            );
        }
    }

    private List<CompanySearchResult> mapPlaces(JsonNode response, String city, String state) {
        List<CompanySearchResult> results = new ArrayList<>();
        if (response == null || !response.has("places")) {
            return results;
        }

        for (JsonNode place : response.path("places")) {
            results.add(new CompanySearchResult(
                    text(place, "id"),
                    text(place.path("displayName"), "text"),
                    text(place.path("primaryTypeDisplayName"), "text"),
                    city,
                    state,
                    text(place, "formattedAddress"),
                    text(place, "websiteUri"),
                    phone(place),
                    text(place, "googleMapsUri")
            ));
        }

        return results;
    }

    private String text(JsonNode node, String field) {
        JsonNode value = node.path(field);
        return value.isMissingNode() || value.isNull() ? null : value.asText();
    }

    private String phone(JsonNode place) {
        String internationalPhone = text(place, "internationalPhoneNumber");
        return internationalPhone != null ? internationalPhone : text(place, "nationalPhoneNumber");
    }
}
