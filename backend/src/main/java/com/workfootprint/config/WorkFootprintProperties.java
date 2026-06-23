package com.workfootprint.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "workfootprint")
public class WorkFootprintProperties {
    private final Auth auth = new Auth();
    private final Ai ai = new Ai();
    private final Dingtalk dingtalk = new Dingtalk();

    public Auth getAuth() {
        return auth;
    }

    public Ai getAi() {
        return ai;
    }

    public Dingtalk getDingtalk() {
        return dingtalk;
    }

    public static class Auth {
        private String jwtSecret;
        private long jwtExpireSeconds;
        private String defaultUsername;
        private String defaultPassword;

        public String getJwtSecret() {
            return jwtSecret;
        }

        public void setJwtSecret(String jwtSecret) {
            this.jwtSecret = jwtSecret;
        }

        public long getJwtExpireSeconds() {
            return jwtExpireSeconds;
        }

        public void setJwtExpireSeconds(long jwtExpireSeconds) {
            this.jwtExpireSeconds = jwtExpireSeconds;
        }

        public String getDefaultUsername() {
            return defaultUsername;
        }

        public void setDefaultUsername(String defaultUsername) {
            this.defaultUsername = defaultUsername;
        }

        public String getDefaultPassword() {
            return defaultPassword;
        }

        public void setDefaultPassword(String defaultPassword) {
            this.defaultPassword = defaultPassword;
        }
    }

    public static class Ai {
        private String baseUrl;
        private String apiKey;
        private String model;

        public String getBaseUrl() {
            return baseUrl;
        }

        public void setBaseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
        }

        public String getApiKey() {
            return apiKey;
        }

        public void setApiKey(String apiKey) {
            this.apiKey = apiKey;
        }

        public String getModel() {
            return model;
        }

        public void setModel(String model) {
            this.model = model;
        }
    }

    public static class Dingtalk {
        private boolean enabled;
        private String token;
        private String aesKey;
        private String appKey;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }

        public String getAesKey() {
            return aesKey;
        }

        public void setAesKey(String aesKey) {
            this.aesKey = aesKey;
        }

        public String getAppKey() {
            return appKey;
        }

        public void setAppKey(String appKey) {
            this.appKey = appKey;
        }
    }
}

