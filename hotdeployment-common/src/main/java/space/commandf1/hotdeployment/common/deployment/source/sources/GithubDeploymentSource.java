package space.commandf1.hotdeployment.common.deployment.source.sources;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import lombok.val;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okio.Okio;
import space.commandf1.hotdeployment.common.deployment.detector.DetectorManager;
import space.commandf1.hotdeployment.common.deployment.detector.detectors.PluginClassDetector;
import space.commandf1.hotdeployment.common.deployment.provider.DetectorProvider;
import space.commandf1.hotdeployment.common.deployment.source.DeploymentSourceType;
import space.commandf1.hotdeployment.common.plugin.HotDeploymentPlugin;
import space.commandf1.hotdeployment.common.plugin.IPluginController;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.concurrent.Executors;

public class GithubDeploymentSource extends AbstractGitDeploymentSource {
    private static final OkHttpClient client = new OkHttpClient();
    private static final ObjectMapper mapper = new ObjectMapper();

    public GithubDeploymentSource(DeploymentSourceType.SourceConfig config) {
        super(config.getPlugin(), config.getPort(), config.getSecret(), config.getPrefix());
    }

    public GithubDeploymentSource(HotDeploymentPlugin<?> plugin, int port, String secret, String prefix) {
        super(plugin, port, secret, prefix);
    }

    private void sendResponse(HttpExchange exchange, int statusCode) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=UTF-8");
        exchange.sendResponseHeaders(statusCode, 0);
        exchange.getResponseBody().close();
    }

    private void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=UTF-8");
        exchange.sendResponseHeaders(statusCode, response.getBytes(StandardCharsets.UTF_8).length);
        try (var outputStream = exchange.getResponseBody()) {
            outputStream.write(response.getBytes(StandardCharsets.UTF_8));
        }
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            this.sendResponse(exchange, 405, "Only POST is acceptable");
            return;
        }

        try {
            if (!this.verifySignature(exchange)) {
                this.sendResponse(exchange, 401, "Signature verification failed");
                return;
            }

            val requestBody = exchange.getRequestBody();
            val payload = mapper.readTree(requestBody);

            String eventType = exchange.getRequestHeaders().getFirst("X-GitHub-Event");

            if ("ping".equals(eventType)) {
                this.sendResponse(exchange, 200, "Webhook configured successfully");
                return;
            }

            if ("release".equals(eventType)) {
                String action = payload.get("action").asText();

                if ("released".equals(action)) {
                    val release = payload.get("release");
//                    val tagName = release.get("tag_name").asText(); // reserved for future logs
                    val assets = release.get("assets");

                    Executors.newSingleThreadExecutor().submit(() -> {
                        for (val asset : assets) {
                            val downloadUrl = asset.get("browser_download_url").asText();

                            val downloadRequest = new Request.Builder()
                                    .url(downloadUrl)
                                    .build();

                            try (val downloadResponse = client.newCall(downloadRequest).execute()) {
                                if (!downloadResponse.isSuccessful()) {
                                    IPluginController.getController().getLogger()
                                            .warning("Download failed: " + downloadResponse.code());
                                    return;
                                }

                                val byteArrayOutputStream = new ByteArrayOutputStream();
                                val bufferedOutputStream = new BufferedOutputStream(byteArrayOutputStream);

                                try (val sink = Okio.buffer(Okio.sink(bufferedOutputStream))) {
                                    sink.writeAll(downloadResponse.body().source());
                                } catch (IOException e) {
                                    IPluginController.getController().getLogger()
                                            .warning("Stream copy error: " + e.getMessage());
                                    return;
                                }
                                
                                val bytes = byteArrayOutputStream.toByteArray();

                                DetectorManager.getInstance().getDetector(PluginClassDetector.class).ifPresent(detector ->
                                        detector.detect(new DetectorProvider<>(bytes, this.getPlugin()))
                                );
                                
                            } catch (IOException e) {
                                IPluginController.getController().getLogger()
                                        .warning("Download error: " + e.getMessage());
                            }
                        }
                    });

                    this.sendResponse(exchange, 200, "Processing");
                    return;
                }
            }

            this.sendResponse(exchange, 200, "Copy that");

        } catch (Exception e) {
            IPluginController.getController().getLogger()
                    .severe("Webhook handle error: " + e.getMessage());
            this.sendResponse(exchange, 500, "Server error: " + e.getMessage());
        }
    }

    private boolean verifySignature(HttpExchange exchange) throws Exception {
        val signature = exchange.getRequestHeaders().getFirst("X-Hub-Signature-256");
        if (signature == null) {
            return false;
        }

        val body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);

        val mac = Mac.getInstance("HmacSHA256");
        val keySpec = new SecretKeySpec(this.getSecret().getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        mac.init(keySpec);
        val hash = mac.doFinal(body.getBytes(StandardCharsets.UTF_8));

        val expected = "sha256=" + bytesToHex(hash);
        return MessageDigest.isEqual(expected.getBytes(StandardCharsets.UTF_8), signature.getBytes(StandardCharsets.UTF_8));
    }

    private static String bytesToHex(byte[] bytes) {
        val result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }
}
