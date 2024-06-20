package com.spin.merchant;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.codec.binary.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Component
public class MerchantHandler {

    private static final Logger log = LoggerFactory.getLogger(MerchantHandler.class);

    public Mono<ServerResponse> payInCallBack(ServerRequest request) {

        return request.formData()
                .flatMap(formData -> {
                    String payId = formData.getFirst("PAY_ID");
                    String encData = formData.getFirst("ENCDATA");

                    System.out.println("Received payId: " + payId);
                    System.out.println("Received encData: " + encData);

                    String keySalt = "cd9ee8b0395f4177";

                    MessageDigest messageDigest = null;
                    try {
                        messageDigest = MessageDigest.getInstance("SHA-256");
                    } catch (NoSuchAlgorithmException e) {
                        throw new RuntimeException(e);
                    }
                    messageDigest.update((keySalt + payId).getBytes());
                    String response = new String(Hex.encodeHex(messageDigest.digest())).toUpperCase();
                    String generatedKey = response.substring(0, 32);

                    String ivString = generatedKey.substring(0,16);
                    IvParameterSpec iv = null;
                    try {
                        iv = new IvParameterSpec(ivString.getBytes("UTF-8"));
                    } catch (UnsupportedEncodingException e) {
                        throw new RuntimeException(e);
                    }
                    Cipher cipher = null;
                    try {
                        cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
                    } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
                        throw new RuntimeException(e);
                    }
                    try {
                        cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(generatedKey.getBytes(), "AES"), iv);
                    } catch (InvalidKeyException | InvalidAlgorithmParameterException e) {
                        throw new RuntimeException(e);
                    }

                    byte[] decodedData = Base64.getDecoder().decode(encData);
                    byte[] decValue = null;
                    try {
                        decValue = cipher.doFinal(decodedData);
                    } catch (IllegalBlockSizeException | BadPaddingException e) {
                        throw new RuntimeException(e);
                    }

                    return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).bodyValue(convert(new String(decValue)));
                });
    }
    public record CallBackMessage(
            @JsonProperty("PAY_ID") String payId,
            @JsonProperty("ENCDATA") String encData,
            @JsonProperty("RESPONSE_CODE") String responseCode,
            @JsonProperty("ORDER_ID") String orderId,
            @JsonProperty("RESPONSE_MESSAGE") String responseMessage
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