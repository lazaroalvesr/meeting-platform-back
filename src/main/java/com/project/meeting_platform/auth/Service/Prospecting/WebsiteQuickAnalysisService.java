package com.project.meeting_platform.auth.Service.Prospecting;

import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Faz uma leitura leve da página inicial pública de um lead. Não executa JavaScript,
 * não tenta autenticar e não acessa URLs privadas da infraestrutura.
 */
@Service
public class WebsiteQuickAnalysisService {

    private static final int MAX_HTML_BYTES = 512_000;
    private static final int MAX_REDIRECTS = 3;
    private static final Pattern VIEWPORT_PATTERN = Pattern.compile("(?is)<meta[^>]+name\\s*=\\s*['\"]viewport['\"][^>]*>");
    private static final Pattern FORM_PATTERN = Pattern.compile("(?is)<form(?:\\s|>)");
    private static final Pattern CONTACT_LINK_PATTERN = Pattern.compile("(?is)(?:href\\s*=\\s*['\"](?:tel:|https?://(?:wa\\.me|api\\.whatsapp\\.com|web\\.whatsapp\\.com))|whatsapp)");

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(4))
            .followRedirects(HttpClient.Redirect.NEVER)
            .build();

    public List<String> inspect(String rawUrl) {
        try {
            URI current = safeUri(rawUrl);

            for (int redirects = 0; redirects <= MAX_REDIRECTS; redirects++) {
                HttpResponse<InputStream> response = httpClient.send(
                        HttpRequest.newBuilder(current)
                                .GET()
                                .timeout(Duration.ofSeconds(7))
                                .header("User-Agent", "AlvesR-Workspace-WebsiteAudit/1.0")
                                .header("Accept", "text/html,application/xhtml+xml")
                                .build(),
                        HttpResponse.BodyHandlers.ofInputStream()
                );

                int status = response.statusCode();
                if (isRedirect(status)) {
                    Optional<String> location = response.headers().firstValue("location");
                    closeQuietly(response.body());
                    if (location.isEmpty()) {
                        return List.of("O site público respondeu com redirecionamento sem destino informado (HTTP " + status + ").");
                    }
                    current = safeUri(current.resolve(location.get()).toString());
                    continue;
                }

                try (InputStream body = response.body()) {
                    if (status < 200 || status >= 400) {
                        return List.of("O site público respondeu com HTTP " + status + " ao abrir a página inicial.");
                    }

                    String contentType = response.headers().firstValue("content-type").orElse("");
                    if (!contentType.isBlank() && !contentType.toLowerCase(Locale.ROOT).contains("html")) {
                        return List.of("O endereço público não retornou uma página HTML para análise rápida.");
                    }

                    return observationsFromHtml(readLimited(body));
                }
            }

            return List.of("O site público fez redirecionamentos demais para uma análise rápida.");
        } catch (IllegalArgumentException exception) {
            return List.of("O link público informado não pôde ser analisado com segurança.");
        } catch (IOException exception) {
            return List.of("Não foi possível carregar a página pública do site para a análise rápida.");
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            return List.of("A análise rápida do site foi interrompida antes de terminar.");
        }
    }

    private URI safeUri(String rawUrl) {
        try {
            URI uri = new URI(rawUrl.trim());
            if (!"http".equalsIgnoreCase(uri.getScheme()) && !"https".equalsIgnoreCase(uri.getScheme())) {
                throw new IllegalArgumentException("Only HTTP(S) URLs are allowed.");
            }
            if (uri.getHost() == null || uri.getHost().isBlank()) {
                throw new IllegalArgumentException("URL host is required.");
            }
            if (uri.getPort() != -1 && uri.getPort() != 80 && uri.getPort() != 443) {
                throw new IllegalArgumentException("Only public web ports are allowed.");
            }
            for (InetAddress address : InetAddress.getAllByName(uri.getHost())) {
                if (!isPublicAddress(address)) {
                    throw new IllegalArgumentException("Private network address is not allowed.");
                }
            }
            return uri;
        } catch (URISyntaxException | IOException exception) {
            throw new IllegalArgumentException("Invalid URL.", exception);
        }
    }

    private boolean isPublicAddress(InetAddress address) {
        if (address.isAnyLocalAddress()
                || address.isLoopbackAddress()
                || address.isLinkLocalAddress()
                || address.isSiteLocalAddress()
                || address.isMulticastAddress()) {
            return false;
        }

        if (address instanceof Inet4Address) {
            byte[] bytes = address.getAddress();
            int first = Byte.toUnsignedInt(bytes[0]);
            int second = Byte.toUnsignedInt(bytes[1]);
            return first != 0
                    && first != 10
                    && first != 127
                    && !(first == 100 && second >= 64 && second <= 127)
                    && !(first == 169 && second == 254)
                    && !(first == 172 && second >= 16 && second <= 31)
                    && !(first == 192 && second == 168);
        }

        return true;
    }

    private List<String> observationsFromHtml(String html) {
        String lowerHtml = html.toLowerCase(Locale.ROOT);
        List<String> observations = new ArrayList<>();

        if (!VIEWPORT_PATTERN.matcher(html).find()) {
            observations.add("O HTML da página inicial não possui meta viewport para adaptação a telas de celular.");
        }

        boolean hasForm = FORM_PATTERN.matcher(html).find();
        boolean hasContactLink = CONTACT_LINK_PATTERN.matcher(lowerHtml).find();
        if (!hasForm && !hasContactLink) {
            observations.add("No HTML inicial não foram encontrados formulário, link de telefone ou link de WhatsApp.");
        } else if (hasForm && hasContactLink) {
            // Há canais de contato; isso é neutro e não deve virar argumento de venda.
        }
        return observations.stream().limit(3).toList();
    }

    private String readLimited(InputStream inputStream) throws IOException {
        byte[] buffer = new byte[8_192];
        int total = 0;
        StringBuilder html = new StringBuilder();
        int read;
        while ((read = inputStream.read(buffer)) != -1 && total < MAX_HTML_BYTES) {
            int available = Math.min(read, MAX_HTML_BYTES - total);
            html.append(new String(buffer, 0, available, StandardCharsets.UTF_8));
            total += available;
        }
        return html.toString();
    }

    private boolean isRedirect(int status) {
        return status == 301 || status == 302 || status == 303 || status == 307 || status == 308;
    }

    private void closeQuietly(InputStream stream) {
        try {
            stream.close();
        } catch (IOException ignored) {
            // The redirect request has already finished; there is nothing else to do here.
        }
    }
}
