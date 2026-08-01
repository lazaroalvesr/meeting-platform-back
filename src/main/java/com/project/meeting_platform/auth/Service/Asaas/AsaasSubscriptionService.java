package com.project.meeting_platform.auth.Service.Asaas;

import com.project.meeting_platform.Model.Client;
import com.project.meeting_platform.Model.Project;
import com.project.meeting_platform.config.AsaasProperties;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class AsaasSubscriptionService {

    private final AsaasProperties properties;

    public AsaasSubscriptionService(AsaasProperties properties) {
        this.properties = properties;
    }

    public boolean syncMaintenanceSubscription(Project project) {
        if (!properties.isApiConfigured()) {
            return false;
        }

        if (!project.isMaintenanceActive()) {
            cancelMaintenanceSubscription(project);
            return true;
        }

        String customerId = ensureCustomer(project.getClient());
        if (StringUtils.hasText(project.getAsaasSubscriptionId())) {
            updateSubscription(project, customerId);
        } else {
            createSubscription(project, customerId);
        }
        return true;
    }

    public void cancelMaintenanceSubscription(Project project) {
        if (!properties.isApiConfigured() || !StringUtils.hasText(project.getAsaasSubscriptionId())) {
            return;
        }

        try {
            client().delete()
                    .uri("/subscriptions/{id}", project.getAsaasSubscriptionId())
                    .retrieve()
                    .toBodilessEntity();
            project.unlinkAsaasSubscription();
        } catch (RestClientException exception) {
            throw asaasUnavailable(exception);
        }
    }

    private String ensureCustomer(Client client) {
        if (StringUtils.hasText(client.getAsaasCustomerId())) {
            return client.getAsaasCustomerId();
        }
        if (!StringUtils.hasText(client.getDocument())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Informe o CPF ou CNPJ do cliente antes de ativar a manutencao no Asaas.");
        }
        if (!StringUtils.hasText(client.getEmail())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Informe o e-mail do cliente antes de ativar a manutencao no Asaas.");
        }

        Map<String, Object> request = new LinkedHashMap<>();
        request.put("name", client.getName());
        request.put("cpfCnpj", digitsOnly(client.getDocument()));
        request.put("email", client.getEmail());
        request.put("externalReference", client.getId().toString());
        if (StringUtils.hasText(client.getPhone())) {
            request.put("mobilePhone", digitsOnly(client.getPhone()));
        }

        try {
            AsaasIdResponse response = client().post()
                    .uri("/customers")
                    .body(request)
                    .retrieve()
                    .body(AsaasIdResponse.class);
            if (response == null || !StringUtils.hasText(response.id())) {
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                        "O Asaas nao retornou o identificador do cliente.");
            }
            client.linkAsaasCustomer(response.id());
            return response.id();
        } catch (RestClientException exception) {
            throw asaasUnavailable(exception);
        }
    }

    private void createSubscription(Project project, String customerId) {
        try {
            AsaasIdResponse response = client().post()
                    .uri("/subscriptions")
                    .body(subscriptionRequest(project, customerId))
                    .retrieve()
                    .body(AsaasIdResponse.class);
            if (response == null || !StringUtils.hasText(response.id())) {
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                        "O Asaas nao retornou o identificador da assinatura.");
            }
            project.linkAsaasSubscription(response.id());
        } catch (RestClientException exception) {
            throw asaasUnavailable(exception);
        }
    }

    private void updateSubscription(Project project, String customerId) {
        try {
            Map<String, Object> request = subscriptionRequest(project, customerId);
            request.remove("customer");
            request.put("status", "ACTIVE");
            request.put("updatePendingPayments", true);
            client().put()
                    .uri("/subscriptions/{id}", project.getAsaasSubscriptionId())
                    .body(request)
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientException exception) {
            throw asaasUnavailable(exception);
        }
    }

    private Map<String, Object> subscriptionRequest(Project project, String customerId) {
        if (project.getMaintenanceMonthlyValue() == null || project.getMaintenanceStartDate() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Informe o valor e o primeiro vencimento da manutencao.");
        }
        Map<String, Object> request = new LinkedHashMap<>();
        request.put("customer", customerId);
        request.put("billingType", "UNDEFINED");
        request.put("value", project.getMaintenanceMonthlyValue().setScale(2, RoundingMode.HALF_UP));
        request.put("nextDueDate", project.getMaintenanceStartDate().toString());
        request.put("cycle", "MONTHLY");
        request.put("description", "Manutencao mensal - " + project.getName());
        request.put("externalReference", project.getId().toString());
        return request;
    }

    private RestClient client() {
        String baseUrl = StringUtils.hasText(properties.baseUrl())
                ? properties.baseUrl().replaceAll("/+$", "")
                : "https://api-sandbox.asaas.com/v3";
        return RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("access_token", properties.apiKey())
                .defaultHeader("User-Agent", "AlvesR-Workspace/1.0")
                .build();
    }

    private ResponseStatusException asaasUnavailable(RestClientException exception) {
        return new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                "Nao foi possivel sincronizar a cobranca com o Asaas. Confira os dados do cliente e as variaveis da integracao.");
    }

    private String digitsOnly(String value) {
        return value.replaceAll("\\D", "");
    }

    private record AsaasIdResponse(String id) {
    }
}
