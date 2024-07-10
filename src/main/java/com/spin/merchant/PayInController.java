package com.spin.merchant;

import com.google.gson.Gson;
import org.apache.commons.codec.binary.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.TreeMap;


@Controller
public class PayInController {
    private static final Logger log = LoggerFactory.getLogger(PayInController.class);
    private final WebClient webClient;

    @Autowired
    public PayInController(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("http://localhost:8080").build();
    }

    @GetMapping("/pay-in-page")
    public Mono<String> payInPage(Model model) throws NoSuchAlgorithmException, InvalidAlgorithmParameterException, NoSuchPaddingException, UnsupportedEncodingException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException {
        model.addAttribute("message", "Merchant Hosted Pay In Page");
        model.addAttribute("payId", Config.payId);
        model.addAttribute("encData", getEncData());
        model.addAttribute("payInUrl", Config.payInUrl);
        return Mono.just("pay-in-page");
    }

    private String getEncData() throws NoSuchAlgorithmException, InvalidAlgorithmParameterException, NoSuchPaddingException, UnsupportedEncodingException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException {
        String payId = Config.payId;
        String salt = Config.salt;
        Map<String, String> treeMap = getParameterMap(payId);
        StringBuilder allFields = new StringBuilder();
        for (String key : treeMap.keySet()) {
            allFields.append(key);
            allFields.append("=");
            allFields.append(treeMap.get(key));
            allFields.append("~");
        }
        String encStringWoHash = allFields.toString();

        int tildeLastCount = allFields.lastIndexOf("~");
        allFields.replace(tildeLastCount, tildeLastCount + 1, "");
        allFields.append(salt);

        String createHash  = allFields.toString();
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        digest.update(createHash.getBytes());
        String generatedHash = new String(Hex.encodeHex(digest.digest(), false));

        String encString = encStringWoHash + "HASH=" + generatedHash;
//        String encString = encStringWoHash;
        String encData = EncDataUtil.encrypt(encString);
        Gson gson = new Gson();
        Map<String, String> jsonMap = new TreeMap<>();
        jsonMap.put("PAY_ID", payId);
        jsonMap.put("ENCDATA", encData);
        String json = gson.toJson(jsonMap);
        System.out.println(json);
        System.out.println(encData);
        return encData;
    }

    private static Map<String, String> getParameterMap(String payId) {
        Map<String, String> treeMap = new TreeMap<>();
        treeMap.put("PAY_ID", payId);
        treeMap.put("PAY_TYPE", "FIAT");
        treeMap.put("CUST_NAME", "spin");
//        treeMap.put("CUST_FIRST_NAME", "spin");
//        treeMap.put("CUST_LAST_NAME", "liao");
//        treeMap.put("CUST_STREET_ADDRESS1", "taiwan");
//        treeMap.put("CUST_CITY", "taipei");
//        treeMap.put("CUST_STATE", "taipei");
//        treeMap.put("CUST_COUNTRY", "taiwan");
//        treeMap.put("CUST_ZIP", "110001");
        treeMap.put("CUST_PHONE", "12345678");
        treeMap.put("CUST_EMAIL", "spin.liso@btse.com");
        treeMap.put("AMOUNT", "10000");
        treeMap.put("TXNTYPE", "SALE");
        treeMap.put("CURRENCY_CODE", "356");
        treeMap.put("PRODUCT_DESC", "test");
        LocalDateTime currentDateTime = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddhhmmss");
        treeMap.put("ORDER_ID", "SPINPI"+currentDateTime.format(formatter));
        treeMap.put("RETURN_URL", "https://localhost:8443/payInCallBack");
        treeMap.put("PAYMENT_TYPE", "UP"); // UP NB WL CARD
        treeMap.put("PAYER_ADDRESS", "abc@ybl");
        treeMap.put("MOP_TYPE", "UP"); // 1030
//        treeMap.put("CARD_HOLDER_NAME", "spin liao");
//        treeMap.put("CARD_NUMBER", "4539148803436467");
//        treeMap.put("CARD_EXP_DT", "072025");
//        treeMap.put("CVV", "123");
        return treeMap;
    }
}
