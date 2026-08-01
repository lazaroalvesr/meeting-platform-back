package com.project.meeting_platform.auth.controller.Asaas;

import com.fasterxml.jackson.databind.JsonNode;
import com.project.meeting_platform.auth.Service.Asaas.AsaasWebhookService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/webhooks/asaas")
public class AsaasWebhookController {

    private final AsaasWebhookService asaasWebhookService;

    public AsaasWebhookController(AsaasWebhookService asaasWebhookService) {
        this.asaasWebhookService = asaasWebhookService;
    }

    @PostMapping
    public ResponseEntity<Void> receive(
            @RequestHeader(value = "asaas-access-token", required = false) String asaasAccessToken,
            @RequestBody JsonNode payload
    ) {
        asaasWebhookService.process(asaasAccessToken, payload);
        return ResponseEntity.noContent().build();
    }
}
