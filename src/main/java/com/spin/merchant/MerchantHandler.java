package com.spin.merchant;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.ui.Model;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

@Component
public class MerchantHandler {

    private static final Logger log = LoggerFactory.getLogger(MerchantHandler.class);

    public Mono<ServerResponse> payInCallBack(ServerRequest request) {
        log.info("payInCallBack");
        return request.formData()
                .flatMap(formData -> {
                    String payId = formData.getFirst("PAY_ID");
                    String encData = formData.getFirst("ENCDATA");

                    log.info("Received payId: {}, encData:{}", payId, encData);

                    String parsedEncDataValue = "";
                    try {
                        parsedEncDataValue = EncDataUtil.decrypt(encData);
                    } catch (IllegalBlockSizeException | BadPaddingException | UnsupportedEncodingException |
                             NoSuchAlgorithmException | NoSuchPaddingException | InvalidAlgorithmParameterException |
                             InvalidKeyException e) {
                        throw new RuntimeException(e);
                    }
                    return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).bodyValue(convert(parsedEncDataValue));
                });
    }

    public Mono<ServerResponse> payInCallBackGet(ServerRequest request) {
        log.info("payInCallBackGet");

        // Extracting query parameters
        String payId = request.queryParam("PAY_ID").orElse("");
        String encData = request.queryParam("ENCDATA").orElse("");

        log.info("Received payId: {}, encData:{}", payId, encData);

        String parsedEncDataValue = "";
        try {
            parsedEncDataValue = EncDataUtil.decrypt(encData);
        } catch (IllegalBlockSizeException | BadPaddingException | UnsupportedEncodingException |
                 NoSuchAlgorithmException | NoSuchPaddingException | InvalidAlgorithmParameterException |
                 InvalidKeyException e) {
            throw new RuntimeException(e);
        }

        return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(convert(parsedEncDataValue));
    }

    public Mono<ServerResponse> payOutCallBack(ServerRequest request) {
        log.info("payOutCallBack");
        return request.formData()
                .flatMap(formData -> {
                    return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).bodyValue("this is payOutCallBack");
                });
    }

    public Mono<ServerResponse> payInWebhook(ServerRequest serverRequest) {
        log.info("payInWebhook");
        return serverRequest.bodyToMono(PayInWebhookMessage.class)
                .doOnNext(message -> {
                    // Process the received message
                    log.info("Processing PayInWebhookMessage: {}", message);
                })
                .flatMap(message -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue("payInWebhook processed successfully"))
                .onErrorResume(e -> {
                    log.error("Error processing payInWebhook", e);
                    return ServerResponse.badRequest()
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue("Invalid request payload");
                });
    }

    public Mono<ServerResponse> payOutWebhook(ServerRequest serverRequest) {
        log.info("payOutWebhook");
        return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).bodyValue("payOutWebhook received");
    }

    public record CallBackMessage(
            @JsonProperty("PAY_ID") String payId,
            @JsonProperty("ENCDATA") String encData,
            @JsonProperty("RESPONSE_CODE") String responseCode,
            @JsonProperty("ORDER_ID") String orderId,
            @JsonProperty("RESPONSE_MESSAGE") String responseMessage
    ) { }

    public record PayInWebhookMessage(
            @JsonProperty("PAY_ID") String payId,
            @JsonProperty("AMOUNT") String encData,
            @JsonProperty("STATUS") String responseCode,
            @JsonProperty("ORDER_ID") String orderId,
            @JsonProperty("PG_REF_NUM") String pgRefNum
    ) { }

    private String convert(String data){

        // Split the string into key-value pairs
        String[] pairs = data.split("~");

        // Create a map to store the key-value pairs
        Map<String, String> map = new HashMap<>();
        for (String pair : pairs) {
            String[] keyValue = pair.split("=", 2);
            if (keyValue.length == 2) {
                map.put(keyValue[0], keyValue[1]);
            } else {
                map.put(keyValue[0], "");  // handle cases where the value might be missing
            }
        }

        // Convert the map to a JSON string using Jackson
        try {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            return gson.toJson(map);
        } catch (Exception e) {
            log.info("error: ", e);
        }
        return "";
    }
}