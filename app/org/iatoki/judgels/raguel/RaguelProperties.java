package org.iatoki.judgels.raguel;

import com.typesafe.config.Config;

import java.io.File;

public final class RaguelProperties {

    private static RaguelProperties INSTANCE;

    private final Config config;

    private String raguelBaseUrl;
    private File raguelBaseDataDir;

    private String jophielBaseUrl;
    private String jophielClientJid;
    private String jophielClientSecret;

    private RaguelProperties(Config config) {
        this.config = config;
    }

    public static synchronized void buildInstance(Config config) {
        if (INSTANCE != null) {
            throw new UnsupportedOperationException("RaguelProperties instance has already been built");
        }

        INSTANCE = new RaguelProperties(config);
        INSTANCE.build();
    }

    public static RaguelProperties getInstance() {
        if (INSTANCE == null) {
            throw new UnsupportedOperationException("RaguelProperties instance has not been built");
        }
        return INSTANCE;
    }

    public String getRaguelBaseUrl() {
        return raguelBaseUrl;
    }

    public String getJophielBaseUrl() {
        return jophielBaseUrl;
    }

    public String getJophielClientJid() {
        return jophielClientJid;
    }

    public String getJophielClientSecret() {
        return jophielClientSecret;
    }

    private void build() {
        raguelBaseUrl = requireStringValue("raguel.baseUrl");
        raguelBaseDataDir = requireDirectoryValue("raguel.baseDataDir");

        jophielBaseUrl = requireStringValue("jophiel.baseUrl");
        jophielClientJid = requireStringValue("jophiel.clientJid");
        jophielClientSecret = requireStringValue("jophiel.clientSecret");
    }

    private String getStringValue(String key) {
        if (!config.hasPath(key)) {
            return null;
        }
        return config.getString(key);
    }

    private String requireStringValue(String key) {
        return config.getString(key);
    }

    private Boolean getBooleanValue(String key) {
        if (!config.hasPath(key)) {
            return null;
        }
        return config.getBoolean(key);
    }

    private boolean requireBooleanValue(String key) {
        return config.getBoolean(key);
    }

    private File requireDirectoryValue(String key) {
        String filename = config.getString(key);

        File dir = new File(filename);
        if (!dir.isDirectory()) {
            throw new RuntimeException("Directory " + dir.getAbsolutePath() + " does not exist");
        }
        return dir;
    }
}
