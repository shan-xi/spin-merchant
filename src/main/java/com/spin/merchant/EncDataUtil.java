package com.spin.merchant;

import org.apache.commons.codec.binary.Hex;

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

public class EncDataUtil {

    public static String decrypt(String encData) throws IllegalBlockSizeException, BadPaddingException, UnsupportedEncodingException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException, InvalidKeyException {
        String payId = "1000140627123003";
        String salt = "473531b173db4371";
        String keySalt = "2c6ece3ac5364224";

        MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
        messageDigest.update((keySalt + payId).getBytes());
        String hexString = new String(Hex.encodeHex(messageDigest.digest())).toUpperCase();
        String generatedKey = hexString.substring(0, 32);

        String ivString = generatedKey.substring(0,16);
        IvParameterSpec iv = new IvParameterSpec(ivString.getBytes("UTF-8"));
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
        cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(generatedKey.getBytes(), "AES"), iv);

        byte[] decodedData = Base64.getDecoder().decode(encData);
        byte[] decValue = cipher.doFinal(decodedData);
        return new String(decValue);
    }
}
