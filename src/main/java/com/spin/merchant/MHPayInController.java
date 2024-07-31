package com.spin.merchant;

import com.google.gson.Gson;
import org.apache.commons.codec.binary.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
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
public class MHPayInController {
    private static final Logger log = LoggerFactory.getLogger(MHPayInController.class);

    @GetMapping("/pay-in-page")
    public Mono<String> payInPage(Model model) throws NoSuchAlgorithmException, InvalidAlgorithmParameterException, NoSuchPaddingException, UnsupportedEncodingException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException {
        model.addAttribute("message", "Merchant Hosted Pay In Page");
        model.addAttribute("payId", Config.payId);
        model.addAttribute("encDataForUP", getEncData("UP"));
        model.addAttribute("encDataForCARD", getEncData("CARD"));
        model.addAttribute("encDataForNB", getEncData("NB"));
        model.addAttribute("encDataForWL", getEncData("WL"));
        model.addAttribute("payInUrl", Config.payInUrl);
        return Mono.just("pay-in-page");
    }

    @GetMapping("/pay-in-response-page")
    public Mono<String> payInResponsePage(Model model) {
        return Mono.just("pay-in-response-page");
    }

    private String getEncData(String type) throws NoSuchAlgorithmException, InvalidAlgorithmParameterException, NoSuchPaddingException, UnsupportedEncodingException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException {
        String payId = Config.payId;
        String salt = Config.salt;
        Map<String, String> treeMap = switch (type) {
            case "UP" -> getParameterMapForUP(payId);
            case "CARD" -> getParameterMapForCARD(payId);
            case "NB" -> getParameterMapForNB(payId);
            case "WL" -> getParameterMapForWL(payId);
            default -> null;
        };
        StringBuilder allFields = new StringBuilder();
        assert treeMap != null;
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

        String createHash = allFields.toString();
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

    private static Map<String, String> getParameterMapForUP(String payId) {
        Map<String, String>  treeMap = getCommonFields(payId, 1);
        treeMap.put("PAYMENT_TYPE", "UP");
        treeMap.put("PAYER_ADDRESS", "12345@456");
        treeMap.put("MOP_TYPE", "UP");
        return treeMap;
    }

    private static Map<String, String> getParameterMapForCARD(String payId) {
        Map<String, String>  treeMap = getCommonFields(payId, 2);
        treeMap.put("PAYMENT_TYPE", "CARD");
        treeMap.put("MOP_TYPE", "CC");
        treeMap.put("CARD_HOLDER_NAME", "spin");
        treeMap.put("CARD_NUMBER", "4111110000000211"); // 4539148803436467
        treeMap.put("CARD_EXP_DT", "122030"); // 072025
        treeMap.put("CVV", "123");
        return treeMap;
    }

    private static Map<String, String> getParameterMapForNB(String payId) {
        Map<String, String> treeMap = getCommonFields(payId, 3);
        treeMap.put("PAYMENT_TYPE", "NB");
//        treeMap.put("MOP_TYPE", "1030");
        treeMap.put("MOP_TYPE", "3023");
        return treeMap;
    }

    private static Map<String, String> getParameterMapForWL(String payId) {
        Map<String, String> treeMap = getCommonFields(payId, 4);
        treeMap.put("PAYMENT_TYPE", "WL");
        treeMap.put("MOP_TYPE", "PPWL");
        return treeMap;
    }

    private static Map<String, String> getCommonFields(String payId, int seq) {
        Map<String, String> treeMap = new TreeMap<>();
        treeMap.put("PAY_ID", payId);
        treeMap.put("PAY_TYPE", "FIAT");
        treeMap.put("CUST_NAME", "Spin Liao");
        treeMap.put("CUST_FIRST_NAME", "Spin");
        treeMap.put("CUST_LAST_NAME", "LIao");
        treeMap.put("CUST_STREET_ADDRESS1", "Taiwan");
        treeMap.put("CUST_CITY", "Taipei");
        treeMap.put("CUST_STATE", "Taipei");
        treeMap.put("CUST_COUNTRY", "TW");
        treeMap.put("CUST_ZIP", "110001");
        treeMap.put("CUST_PHONE", "9454243567");
        treeMap.put("CUST_EMAIL", "spin.liao@btse.com");
        treeMap.put("AMOUNT", "10000000");
        treeMap.put("TXNTYPE", "SALE");
//        treeMap.put("CURRENCY_CODE", "356");
//        treeMap.put("CURRENCY_CODE", "978");
        treeMap.put("CURRENCY_CODE", "704");
        treeMap.put("PRODUCT_DESC", "TEST");
        LocalDateTime currentDateTime = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddhhmmss");
        treeMap.put("ORDER_ID", "SPINPI" + currentDateTime.format(formatter) + seq);
        treeMap.put("RETURN_URL", "https://localhost:8443/payInCallBack");
        return treeMap;
    }
}
