package com.github.walkvoid.zone.ai.knowledge;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * classpath knowledge Markdown 灌库与检索配置。
 */
@ConfigurationProperties(prefix = "zone.ai.knowledge")
public class KnowledgeProperties {

    /** 是否启用知识库灌库/检索能力 */
    private boolean enabled = true;

    /** classpath 下 knowledge 根路径（Ant 风格） */
    private String locationPattern = "classpath*:knowledge/**/*.md";

    /** 单段最大字符数（LangChain4j splitter） */
    private int maxSegmentSize = 800;

    /** 段重叠字符数 */
    private int maxOverlapSize = 100;

    /** 检索默认 topK */
    private int defaultTopK = 5;

    /** 相似度阈值，0~1，越大越严 */
    private double similarityThreshold = 0.45;

    /** metadata 标记，用于删除/过滤本模块写入的文档 */
    private String kbTag = "knowledge";

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getLocationPattern() {
        return locationPattern;
    }

    public void setLocationPattern(String locationPattern) {
        this.locationPattern = locationPattern;
    }

    public int getMaxSegmentSize() {
        return maxSegmentSize;
    }

    public void setMaxSegmentSize(int maxSegmentSize) {
        this.maxSegmentSize = maxSegmentSize;
    }

    public int getMaxOverlapSize() {
        return maxOverlapSize;
    }

    public void setMaxOverlapSize(int maxOverlapSize) {
        this.maxOverlapSize = maxOverlapSize;
    }

    public int getDefaultTopK() {
        return defaultTopK;
    }

    public void setDefaultTopK(int defaultTopK) {
        this.defaultTopK = defaultTopK;
    }

    public double getSimilarityThreshold() {
        return similarityThreshold;
    }

    public void setSimilarityThreshold(double similarityThreshold) {
        this.similarityThreshold = similarityThreshold;
    }

    public String getKbTag() {
        return kbTag;
    }

    public void setKbTag(String kbTag) {
        this.kbTag = kbTag;
    }
}
