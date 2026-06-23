package com.workfootprint.dingtalk;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Base64;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class DingtalkCrypto {
    private final String token;
    private final String appKey;
    private final byte[] aesKey;
    private final IvParameterSpec iv;

    public DingtalkCrypto(String token, String aesKey43, String appKey) {
        this.token = token;
        this.appKey = appKey;
        this.aesKey = Base64.getDecoder().decode(aesKey43 + "=");
        this.iv = new IvParameterSpec(Arrays.copyOfRange(this.aesKey, 0, 16));
    }

    public String decrypt(String encrypt) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(aesKey, "AES"), iv);
        byte[] plain = cipher.doFinal(Base64.getDecoder().decode(encrypt));

        ByteBuffer buf = ByteBuffer.wrap(plain);
        byte[] random16 = new byte[16];
        buf.get(random16);
        int msgLen = buf.getInt();
        byte[] msg = new byte[msgLen];
        buf.get(msg);
        byte[] corpId = new byte[buf.remaining()];
        buf.get(corpId);

        String corp = new String(corpId, StandardCharsets.UTF_8);
        if (appKey != null && !appKey.isBlank() && !appKey.equals(corp)) {
            throw new IllegalArgumentException("invalid appKey");
        }
        return new String(msg, StandardCharsets.UTF_8);
    }

    public Map<String, String> encryptResponse(String plaintext, String timestamp, String nonce) throws Exception {
        String encrypt = encrypt(plaintext);
        String signature = sign(token, timestamp, nonce, encrypt);
        Map<String, String> map = new HashMap<>();
        map.put("msg_signature", signature);
        map.put("encrypt", encrypt);
        map.put("timeStamp", timestamp);
        map.put("nonce", nonce);
        return map;
    }

    public String encrypt(String plaintext) throws Exception {
        byte[] random16 = new byte[16];
        new Random().nextBytes(random16);
        byte[] msg = plaintext.getBytes(StandardCharsets.UTF_8);
        byte[] corp = appKey.getBytes(StandardCharsets.UTF_8);

        ByteBuffer buf = ByteBuffer.allocate(16 + 4 + msg.length + corp.length);
        buf.put(random16);
        buf.putInt(msg.length);
        buf.put(msg);
        buf.put(corp);

        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(aesKey, "AES"), iv);
        byte[] encrypted = cipher.doFinal(buf.array());
        return Base64.getEncoder().encodeToString(encrypted);
    }

    public static String sign(String token, String timestamp, String nonce, String encrypt) throws Exception {
        String[] arr = new String[]{token, timestamp, nonce, encrypt};
        Arrays.sort(arr, Comparator.naturalOrder());
        String plain = String.join("", arr);
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        byte[] digest = md.digest(plain.getBytes(StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        for (byte b : digest) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}

