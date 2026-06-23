package com.workfootprint.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.workfootprint.config.WorkFootprintProperties;
import com.workfootprint.dingtalk.DingtalkCrypto;
import com.workfootprint.entity.UserEntity;
import com.workfootprint.service.UserService;
import com.workfootprint.service.WorkRecordService;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/dingtalk")
public class DingtalkController {
    private final WorkFootprintProperties properties;
    private final ObjectMapper objectMapper;
    private final UserService userService;
    private final WorkRecordService workRecordService;

    public DingtalkController(WorkFootprintProperties properties, ObjectMapper objectMapper, UserService userService, WorkRecordService workRecordService) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.userService = userService;
        this.workRecordService = workRecordService;
    }

    @PostMapping(value = "/callback", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Object callback(
            @RequestParam(value = "timestamp", required = false) String timestamp,
            @RequestParam(value = "timeStamp", required = false) String timeStamp2,
            @RequestParam(value = "nonce", required = false) String nonce,
            @RequestParam(value = "msg_signature", required = false) String msgSignature,
            @RequestParam(value = "signature", required = false) String signature2,
            @RequestBody Map<String, Object> body
    ) throws Exception {
        if (!properties.getDingtalk().isEnabled()) {
            return Map.of("success", true);
        }

        String ts = StringUtils.hasText(timestamp) ? timestamp : timeStamp2;
        if (!StringUtils.hasText(ts)) {
            ts = String.valueOf(Instant.now().getEpochSecond());
        }
        String n = StringUtils.hasText(nonce) ? nonce : UUID.randomUUID().toString().replace("-", "");
        String sig = StringUtils.hasText(msgSignature) ? msgSignature : signature2;

        UserEntity user = userService.ensureDefaultUser();

        Object encryptObj = body.get("encrypt");
        if (encryptObj instanceof String encrypt && StringUtils.hasText(encrypt)) {
            DingtalkCrypto crypto = new DingtalkCrypto(properties.getDingtalk().getToken(), properties.getDingtalk().getAesKey(), properties.getDingtalk().getAppKey());
            if (StringUtils.hasText(sig)) {
                String expected = DingtalkCrypto.sign(properties.getDingtalk().getToken(), ts, n, encrypt);
                if (!expected.equals(sig)) {
                    return crypto.encryptResponse("success", ts, n);
                }
            }
            String plaintext = crypto.decrypt(encrypt);
            JsonNode event = objectMapper.readTree(plaintext);
            handleEvent(user.getId(), event);
            return crypto.encryptResponse("success", ts, n);
        }

        JsonNode event = objectMapper.valueToTree(body);
        handleEvent(user.getId(), event);
        return Map.of("success", true);
    }

    private void handleEvent(long userId, JsonNode event) {
        String text = null;
        JsonNode t = event.path("text").path("content");
        if (!t.isMissingNode() && !t.isNull()) {
            text = t.asText();
        }
        if (!StringUtils.hasText(text)) {
            JsonNode c1 = event.path("content");
            if (!c1.isMissingNode() && !c1.isNull()) {
                text = c1.asText();
            }
        }
        if (!StringUtils.hasText(text)) {
            JsonNode c2 = event.path("Content");
            if (!c2.isMissingNode() && !c2.isNull()) {
                text = c2.asText();
            }
        }
        if (!StringUtils.hasText(text)) {
            return;
        }
        workRecordService.create(userId, "DINGTALK", text, null);
    }
}

