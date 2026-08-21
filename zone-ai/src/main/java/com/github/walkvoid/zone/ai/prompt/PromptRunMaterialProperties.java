package com.github.walkvoid.zone.ai.prompt;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Prompt 运行附件（临时向量材料）配置。
 */
@ConfigurationProperties(prefix = "zone.ai.prompt.run-material")
public class PromptRunMaterialProperties {

    /** 向量 metadata.kb，与长期 knowledge 隔离 */
    private String kbTag = "prompt_run";

    /** 短于该字符数且非 docx 时直接全文注入，不入向量库 */
    private int fullTextMaxChars = 8000;

    /** RAG topK */
    private int ragTopK = 8;

    /** 相似度阈值 */
    private double similarityThreshold = 0.3;

    /** 单次运行最多附件数 */
    private int maxFiles = 5;

    public String getKbTag() {
        return kbTag;
    }

    public void setKbTag(String kbTag) {
        this.kbTag = kbTag;
    }

    public int getFullTextMaxChars() {
        return fullTextMaxChars;
    }

    public void setFullTextMaxChars(int fullTextMaxChars) {
        this.fullTextMaxChars = fullTextMaxChars;
    }

    public int getRagTopK() {
        return ragTopK;
    }

    public void setRagTopK(int ragTopK) {
        this.ragTopK = ragTopK;
    }

    public double getSimilarityThreshold() {
        return similarityThreshold;
    }

    public void setSimilarityThreshold(double similarityThreshold) {
        this.similarityThreshold = similarityThreshold;
    }

    public int getMaxFiles() {
        return maxFiles;
    }

    public void setMaxFiles(int maxFiles) {
        this.maxFiles = maxFiles;
    }
}
