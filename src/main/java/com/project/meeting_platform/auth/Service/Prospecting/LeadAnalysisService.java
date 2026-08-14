package com.project.meeting_platform.auth.Service.Prospecting;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.meeting_platform.auth.dto.Prospecting.AnalyzeLeadRequest;
import com.project.meeting_platform.auth.dto.Prospecting.LeadAnalysisResponse;
import com.project.meeting_platform.Model.ProspectedLead;
import com.project.meeting_platform.config.OpenAiProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class LeadAnalysisService {

    private static final Logger LOGGER = LoggerFactory.getLogger(LeadAnalysisService.class);
    private static final ZoneId BRAZIL_ZONE = ZoneId.of("America/Sao_Paulo");

    private static final String INSTRUCTIONS = """
            Você é um agente de prospecção B2B do AlvesR, estúdio brasileiro de criação e melhoria de sites institucionais.
            Seu objetivo não é vender na primeira mensagem: é obter uma resposta simples, como "pode mandar" ou
            "me explica melhor". Você recebe somente dados públicos e observações verificadas pelo coletor.

            Todo lead chegou a partir de um perfil público do Google e possui ao menos uma observação verificável sobre nome,
            segmento e localização. Portanto use sempre status READY e gere uma mensagem. Nunca invente, suponha, elogie
            algo não verificado ou afirme que visitou/analisou um site sem uma observação correspondente.

            Quando houver observação verificável, use status READY. Escreva uma mensagem inicial de WhatsApp em português do
            Brasil com 20 a 38 palavras. Escreva em um único bloco curto. Apresente-se logo no início como "Sou o Lázaro, da
            AlvesR Sites e Sistemas Web". Não acrescente uma explicação genérica de que cria ou melhora sites; o convite comercial já deixa isso
            claro. Nunca use cidade, estado, perfil do Google, a fonte da pesquisa ou o nome da empresa como prova de que você
            encontrou o lead.
            Se houver um problema técnico verificado, cite somente esse problema e uma consequência prática diretamente ligada
            a ele. Se não houver problema técnico, não descreva o perfil do Google, não cite a URL e não invente uma crítica:
            ofereça uma pequena contribuição concreta que o Lázaro pode enviar antes de qualquer conversa. Use somente um
            motivo comercial e termine com uma pergunta fácil de responder com "sim".
            Ela precisa ser humana, específica e persuasiva,
            mas sem pressão. Não use "prezado", "venho por meio desta", urgência falsa, caixa alta, promoção, números sem fonte,
            link na primeira mensagem, promessas de resultado ou linguagem robótica. Use zero emoji, exceto no máximo um para
            segmentos informais. Não use frases de despedida ou defesa, como "Se não fizer sentido", "não incomodo mais",
            "sem compromisso" ou "fico à disposição". Termine diretamente na pergunta comercial.

            FORMATO PADRÃO OBRIGATÓRIO PARA A MENSAGEM INICIAL:
            "[saudação do horário], tudo bem? Sou o Lázaro, da AlvesR Sites e Sistemas Web. Trabalho criando e melhorando sites
            institucionais para [tipo simples de negócio]. [frase de valor específica]. Posso te enviar uma referência de como
            isso poderia funcionar para vocês?"
            Use a saudação fornecida no contexto: Bom dia, Boa tarde ou Boa noite. Não use o nome da empresa na saudação.
            Troque categorias técnicas do Google por palavras comuns: "policlínica" vira "clínica", "escritório de advocacia"
            pode virar "escritório", e quando não houver tradução natural use "empresa". Para clínicas e consultórios use
            "agendamento"; para os demais, use "contato". Este formato substitui todas as instruções anteriores sobre mensagem
            inicial. Não acrescente outra frase, não repita "serviços", "localização", "contato" ou "agendamento" na pergunta
            final e não cite o nome da empresa. Não inclua observação técnica na primeira mensagem.

            Para clínicas e consultórios, use exatamente como frase de valor: "Acredito que uma apresentação mais clara dos
            serviços e do agendamento pode facilitar o primeiro contato com a clínica." Não repita essa informação em outra
            parte da mensagem. Para outros segmentos, adapte a frase de valor ao atendimento e ao contato, sem prometer resultado.

            Use obrigatoriamente esta ordem para TODO lead READY, inclusive quando ele já possui site:
            1) "[saudação], tudo bem? Sou o Lázaro, da AlvesR Sites e Sistemas Web.";
            2) uma contextualização humana usando nome ou segmento;
            3) uma contribuição concreta ou, apenas se existir, um problema técnico verificado;
            4) uma pergunta curta e comercial, oferecendo uma referência de site para aquele tipo de negócio.
            Nunca transforme apenas a existência de um site em um problema. Para leads com site sem diagnóstico técnico,
            apresente o AlvesR de forma consultiva usando o nome da empresa e a cidade de modo natural, sem alegar que o site
            está ruim ou precisa de correção. Não fale "vi seu perfil no Google", não cite a categoria como se fosse uma
            descoberta e nunca escreva a URL na mensagem. Ofereça uma referência de site profissional adequada ao segmento.
            Não use a expressão "análise breve da página inicial" como frase pronta.
            Nunca use as expressões "primeira página", "posso te explicar", "duas sugestões objetivas" ou "estrutura do site"
            na mensagem inicial. Elas soam vagas ou técnicas. Em vez disso, deixe clara a proposta comercial: uma referência de
            site profissional para o segmento, capaz de apresentar serviços, localização e formas de atendimento. O convite deve
            ser simples, por exemplo: "Posso te enviar uma referência rápida de como isso poderia funcionar para vocês?".

            Títulos de página, endereço do site, existência de formulário ou de canais de contato são dados neutros. Nunca use
            esses dados como defeito, nem invente efeitos como "converter mais clientes", "melhorar posicionamento" ou "aumentar
            credibilidade". Só cite um problema técnico quando a observação disser exatamente que ele existe, por exemplo:
            erro HTTP, ausência de meta viewport ou ausência de formulário/link de contato no HTML inicial.

            Exemplo de tom para advocacia quando não há falha técnica confirmada: "Olá, [nome], tudo bem? Sou o Lázaro, da
            AlvesR Sites e Sistemas Web. Trabalho criando sites institucionais para escritórios de advocacia, com comunicação sóbria e informações
            claras para quem busca atendimento. Posso te enviar uma referência rápida de site para esse tipo de escritório?"
            Não copie literalmente; adapte cada mensagem, sem inventar problemas ou prometer resultados.

            Para leads sem site informado, não mencione Google, perfil, cidade, estado, falta de cadastro ou ausência de site
            como se fosse um defeito. Apresente apenas a sua proposta: criar um site profissional para o segmento, reunindo os
            serviços e formas de atendimento. Convide a empresa a receber uma referência de site adequada ao tipo de negócio.

            Gere followUp3Days somente para READY: até 25 palavras, uma única tentativa, sem insistência. Indique confidence
            como HIGH, MEDIUM ou LOW. openingChosen descreve resumidamente a abertura usada. Varie a formulação naturalmente.
            Para advocacia, saúde, contabilidade e finanças, use tom sóbrio e institucional; não prometa resultado, não faça
            captação agressiva e não invente tratamento como Dr. ou Dra. Para indústria/B2B, destaque credibilidade e catálogo;
            para comércio e serviços locais, use benefícios adequados ao segmento sem prometer desempenho.

            Não envie mensagens, não salve dados e não solicite dados sensíveis.
            """;

    private static final String FOLLOW_UP_INSTRUCTIONS = """
            Você é o agente de prospecção B2B da AlvesR. Gere somente uma mensagem de follow-up para WhatsApp em português do
            Brasil, com 18 a 32 palavras e dois blocos curtos separados por uma linha em branco. A mensagem será revisada e
            enviada manualmente pelo Lázaro.

            Use apenas os dados públicos e o contexto recebidos. Seja humano, profissional e vendedor sem ser insistente.
            Quando precisar se apresentar, use exatamente "Sou o Lázaro, da AlvesR Sites e Sistemas Web".
            Não invente problemas no site, não cite URL, não diga "vi seu perfil no Google", não use "primeira página",
            "posso te explicar" ou "duas sugestões objetivas". Não use urgência falsa, promoção, promessas de resultado,
            caixa alta, emojis ou tom robótico.

            Para o primeiro retorno, reconheça que a pessoa pode estar ocupada e retome de maneira leve.
            Para o segundo, ofereça uma referência prática relacionada à presença digital do segmento.
            Para o terceiro, deixe a porta aberta sem pressionar.
            Na reativação mensal, só retome com respeito e uma proposta nova de valor. Retorne somente JSON válido.
            """;

    private final RestClient restClient;
    private final OpenAiProperties properties;
    private final ObjectMapper objectMapper;
    private final WebsiteQuickAnalysisService websiteQuickAnalysisService;

    public LeadAnalysisService(
            RestClient.Builder restClientBuilder,
            OpenAiProperties properties,
            ObjectMapper objectMapper,
            WebsiteQuickAnalysisService websiteQuickAnalysisService
    ) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.websiteQuickAnalysisService = websiteQuickAnalysisService;

        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory();
        requestFactory.setReadTimeout(Duration.ofSeconds(30));
        this.restClient = restClientBuilder
                .baseUrl(properties.baseUrl())
                .requestFactory(requestFactory)
                .build();
    }

    public LeadAnalysisResponse analyze(AnalyzeLeadRequest lead) {
        ensureApiKey();

        try {
            List<String> observations = verifiedObservations(lead);
            JsonNode response = restClient.post()
                    .uri("/responses")
                    .header("Authorization", "Bearer " + properties.apiKey())
                    .body(Map.of(
                            "model", properties.model(),
                            "store", false,
                            "instructions", INSTRUCTIONS,
                            "input", inputFor(lead, observations),
                            "max_output_tokens", 1_200,
                            "reasoning", Map.of("effort", "minimal"),
                            "text", Map.of("format", analysisSchema())
                    ))
                    .retrieve()
                    .body(JsonNode.class);

            return parseAnalysis(response);
        } catch (RestClientException exception) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Não foi possível analisar este lead agora. Tente novamente em instantes.",
                    exception
            );
        }
    }

    public String generateFollowUpMessage(ProspectedLead lead) {
        ensureApiKey();

        try {
            JsonNode response = restClient.post()
                    .uri("/responses")
                    .header("Authorization", "Bearer " + properties.apiKey())
                    .body(Map.of(
                            "model", properties.model(),
                            "store", false,
                            "instructions", FOLLOW_UP_INSTRUCTIONS,
                            "input", followUpInput(lead),
                            "max_output_tokens", 400,
                            "reasoning", Map.of("effort", "minimal"),
                            "text", Map.of("format", followUpSchema())
                    ))
                    .retrieve()
                    .body(JsonNode.class);

            return parseFollowUpMessage(response);
        } catch (RestClientException exception) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Não foi possível gerar a mensagem de follow-up agora. Tente novamente em instantes.",
                    exception
            );
        }
    }

    private void ensureApiKey() {
        if (properties.apiKey() == null || properties.apiKey().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "A análise por IA ainda não está configurada. Defina OPENAI_API_KEY no servidor."
            );
        }
    }

    private String inputFor(AnalyzeLeadRequest lead, List<String> observations) {
        return """
                Dados públicos da empresa para análise:
                - Nome: %s
                - Categoria: %s
                - Cidade/UF: %s/%s
                - Endereço público: %s
                - Site: %s
                - Telefone público: %s
                - Fonte: %s
                - Observações verificadas pelo coletor: %s
                - Saudação obrigatória agora, no horário de Brasília: %s
                """.formatted(
                value(lead.name()),
                value(lead.category()),
                value(lead.city()),
                lead.state().trim().toUpperCase(Locale.ROOT),
                value(lead.publicAddress()),
                value(lead.website()),
                value(lead.phone()),
                value(lead.sourceUrl()),
                String.join(" | ", observations),
                greetingForCurrentTime()
        );
    }

    private List<String> verifiedObservations(AnalyzeLeadRequest lead) {
        List<String> observations = new ArrayList<>();
        observations.add("O perfil público encontrado no Google identifica a empresa como "
                + value(lead.category()) + " em " + value(lead.city()) + "/"
                + lead.state().trim().toUpperCase(Locale.ROOT) + ".");

        if (lead.website() == null || lead.website().isBlank()) {
            observations.add("O perfil público encontrado no Google não informa um site para a empresa.");
        } else {
            observations.add("O perfil público encontrado no Google informa este site: " + lead.website().trim() + ".");
            observations.addAll(websiteQuickAnalysisService.inspect(lead.website()));
        }
        return observations;
    }

    private Map<String, Object> analysisSchema() {
        return Map.of(
                "type", "json_schema",
                "name", "lead_analysis",
                "strict", true,
                "schema", Map.of(
                        "type", "object",
                        "additionalProperties", false,
                        "properties", Map.ofEntries(
                                Map.entry("status", Map.of("type", "string", "enum", List.of("READY"))),
                                Map.entry("score", Map.of("type", "integer", "minimum", 0, "maximum", 100)),
                                Map.entry("opportunity", Map.of("type", "string", "maxLength", 500)),
                                Map.entry("strengths", arraySchema(3)),
                                Map.entry("cautions", arraySchema(3)),
                                Map.entry("suggestedApproach", Map.of("type", "string", "maxLength", 500)),
                                Map.entry("observationUsed", nullableStringSchema(500)),
                                Map.entry("openingChosen", nullableStringSchema(160)),
                                Map.entry("message", Map.of("type", "string", "maxLength", 600)),
                                Map.entry("followUp3Days", nullableStringSchema(180)),
                                Map.entry("confidence", Map.of("type", "string", "enum", List.of("HIGH", "MEDIUM", "LOW")))
                        ),
                        "required", List.of("status", "score", "opportunity", "strengths", "cautions", "suggestedApproach", "observationUsed", "openingChosen", "message", "followUp3Days", "confidence")
                )
        );
    }

    private Map<String, Object> arraySchema(int maxItems) {
        return Map.of(
                "type", "array",
                "items", Map.of("type", "string", "maxLength", 220),
                "maxItems", maxItems
        );
    }

    private Map<String, Object> nullableStringSchema(int maxLength) {
        return Map.of(
                "type", List.of("string", "null"),
                "maxLength", maxLength
        );
    }

    private String followUpInput(ProspectedLead lead) {
        int followUpNumber = lead.getFollowUpCount() + 1;
        String stage = switch (followUpNumber) {
            case 1 -> "Primeiro follow-up, sete dias após a abordagem inicial.";
            case 2 -> "Segundo follow-up, quinze dias após a abordagem inicial.";
            case 3 -> "Terceiro follow-up, trinta dias após a abordagem inicial.";
            default -> "Reativação mensal após os três follow-ups iniciais.";
        };

        return """
                Contexto do lead:
                - Empresa: %s
                - Segmento: %s
                - Cidade/UF: %s/%s
                - Tem site informado: %s
                - Etapa: %s
                - Mensagem inicial já usada: %s
                - Saudação obrigatória agora, no horário de Brasília: %s
                """.formatted(
                value(lead.getName()),
                value(lead.getCategory()),
                value(lead.getCity()),
                value(lead.getState()),
                lead.getWebsite() == null || lead.getWebsite().isBlank() ? "não" : "sim",
                stage,
                value(lead.getOpeningMessage()),
                greetingForCurrentTime()
        );
    }

    private String greetingForCurrentTime() {
        int hour = ZonedDateTime.now(BRAZIL_ZONE).getHour();
        if (hour < 12) {
            return "Bom dia";
        }
        if (hour < 18) {
            return "Boa tarde";
        }
        return "Boa noite";
    }

    private Map<String, Object> followUpSchema() {
        return Map.of(
                "type", "json_schema",
                "name", "follow_up_message",
                "strict", true,
                "schema", Map.of(
                        "type", "object",
                        "additionalProperties", false,
                        "properties", Map.of(
                                "message", Map.of("type", "string", "maxLength", 600)
                        ),
                        "required", List.of("message")
                )
        );
    }

    private LeadAnalysisResponse parseAnalysis(JsonNode response) {
        String outputText = response.path("output_text").asText(null);
        if (outputText == null || outputText.isBlank()) {
            outputText = findOutputText(response.path("output"));
        }

        if (outputText == null || outputText.isBlank()) {
            String status = response.path("status").asText("desconhecido");
            String reason = response.path("incomplete_details").path("reason").asText("");
            LOGGER.warn("OpenAI response without output text. status={}, reason={}", status, reason);

            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    reason.isBlank()
                            ? "A IA não concluiu uma análise utilizável. Tente novamente."
                            : "A IA não concluiu a resposta (" + reason + "). Tente novamente."
            );
        }

        try {
            LeadAnalysisResponse analysis = objectMapper.readValue(outputText, LeadAnalysisResponse.class);
            if (analysis.message() == null || analysis.message().isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "A IA não gerou uma mensagem de prospecção utilizável.");
            }
            return analysis;
        } catch (JsonProcessingException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "A IA retornou uma análise em formato inválido.", exception);
        }
    }

    private String parseFollowUpMessage(JsonNode response) {
        String outputText = response.path("output_text").asText(null);
        if (outputText == null || outputText.isBlank()) {
            outputText = findOutputText(response.path("output"));
        }

        if (outputText == null || outputText.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "A IA não gerou uma mensagem de follow-up utilizável.");
        }

        try {
            String message = objectMapper.readTree(outputText).path("message").asText("").trim();
            if (message.isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "A IA não gerou uma mensagem de follow-up utilizável.");
            }
            return message;
        } catch (JsonProcessingException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "A IA retornou o follow-up em formato inválido.", exception);
        }
    }

    private String findOutputText(JsonNode node) {
        if (node.isObject()) {
            if ("output_text".equals(node.path("type").asText()) && node.path("text").isTextual()) {
                return node.path("text").asText();
            }

            Iterator<JsonNode> children = node.elements();
            while (children.hasNext()) {
                String text = findOutputText(children.next());
                if (text != null && !text.isBlank()) {
                    return text;
                }
            }
        }

        if (node.isArray()) {
            for (JsonNode item : node) {
                String text = findOutputText(item);
                if (text != null && !text.isBlank()) {
                    return text;
                }
            }
        }

        return null;
    }

    private String value(String input) {
        return input == null || input.isBlank() ? "Não informado" : input.trim();
    }
}
