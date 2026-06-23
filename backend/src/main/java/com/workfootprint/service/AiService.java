package com.workfootprint.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.workfootprint.ai.ParsedWorkRecord;
import com.workfootprint.config.WorkFootprintProperties;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AiService {
    private final WorkFootprintProperties properties;
    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    public AiService(WorkFootprintProperties properties, WebClient webClient, ObjectMapper objectMapper) {
        this.properties = properties;
        this.webClient = webClient;
        this.objectMapper = objectMapper;
    }

    public ParsedWorkRecord parseWorkRecord(String rawContent) {
        if (!enabled()) {
            return null;
        }
        String systemPrompt = "你是一个工作记录整理助手。把用户的一段自然语言工作描述整理成结构化 JSON。"
                + "只输出 JSON，不要输出多余文本。字段：projectName,moduleName,taskSummary,resultSummary,issueSummary,nextPlan,tags。"
                + "tags 是字符串数组，最多 5 个。没有信息就给空字符串或空数组。";

        String userPrompt = "工作内容：\n" + rawContent;

        String content = chat(systemPrompt, userPrompt, true);
        if (!StringUtils.hasText(content)) {
            return null;
        }
        try {
            return objectMapper.readValue(content, ParsedWorkRecord.class);
        } catch (Exception e) {
            return null;
        }
    }

    public String generateReport(String type, String startDate, String endDate, List<String> recordLines) {
        if (!enabled()) {
            return null;
        }
        String systemPrompt = "你是一个工作总结助手。根据用户在时间范围内的工作记录，生成可直接发送的 Markdown 总结。"
                + "输出结构包含：本期完成、关键成果、问题与风险、下阶段计划。语言简洁，条理清晰。";
        String userPrompt = "总结类型：" + type + "\n"
                + "时间范围：" + startDate + " ~ " + endDate + "\n"
                + "工作记录：\n- " + String.join("\n- ", recordLines);
        return chat(systemPrompt, userPrompt, false);
    }

    public boolean enabled() {
        return StringUtils.hasText(properties.getAi().getBaseUrl()) && StringUtils.hasText(properties.getAi().getApiKey());
    }

    private String chat(String systemPrompt, String userPrompt, boolean json) {
        String baseUrl = properties.getAi().getBaseUrl();
        String url = normalizeBaseUrl(baseUrl) + "/v1/chat/completions";

        Map<String, Object> body = new HashMap<>();
        body.put("model", properties.getAi().getModel());
        body.put("temperature", 0.2);
        body.put("messages", List.of(
                Map.of("role", "system", "content", systemPrompt),
                Map.of("role", "user", "content", userPrompt)
        ));
        if (json) {
            body.put("response_format", Map.of("type", "json_object"));
        }

        String resp = webClient.post()
                .uri(url)
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + properties.getAi().getApiKey())
                .bodyValue(body)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        if (!StringUtils.hasText(resp)) {
            return null;
        }
        try {
            JsonNode root = objectMapper.readTree(resp);
            JsonNode node = root.path("choices").path(0).path("message").path("content");
            if (node.isMissingNode() || node.isNull()) {
                return null;
            }
            return node.asText();
        } catch (Exception e) {
            return null;
        }
    }

    private String normalizeBaseUrl(String baseUrl) {
        String s = baseUrl.trim();
        while (s.endsWith("/")) {
            s = s.substring(0, s.length() - 1);
        }
        return s;
    }
}

