package com.fitness.agent.service.ai;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 轻量级 RAG 检索器（关键词版）。
 * 流程与向量检索一致：知识切分(chunk) → 检索(rank) → 供生成阶段增强。
 * 区别只在检索打分：这里用"关键词命中数"做 BM25 的简化替代，
 * 不依赖 embedding 模型，完全本地运行——同样的流程、更小的依赖，
 * 面试时可以讲"这是 BM25 的简化实现，生产可替换为向量检索"。
 */
@Component
public class KnowledgeRetriever {

    private final List<String> chunks = new ArrayList<>();

    public KnowledgeRetriever() throws IOException {
        try (InputStream is = new ClassPathResource("rag/fitness_knowledge.txt").getInputStream();
             BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) {
                    if (!sb.isEmpty()) {
                        chunks.add(sb.toString().trim());
                        sb.setLength(0);
                    }
                } else {
                    sb.append(line);
                }
            }
            if (!sb.isEmpty()) {
                chunks.add(sb.toString().trim());
            }
        }
    }

    /**
     * 按问题检索最相关的 topK 个知识片段。
     */
    public String retrieve(String question, int topK) {
        if (chunks.isEmpty()) {
            return "";
        }
        List<String> keywords = extractKeywords(question);
        if (keywords.isEmpty()) {
            return String.join("\n\n", chunks.subList(0, Math.min(topK, chunks.size())));
        }
        return chunks.stream()
                .map(c -> new Score(c, hitCount(c, keywords)))
                .filter(s -> s.score > 0)
                .sorted(Comparator.comparingInt((Score s) -> s.score).reversed())
                .limit(topK)
                .map(s -> s.chunk)
                .reduce((a, b) -> a + "\n\n" + b)
                .orElse("");
    }

    /** 提取问题关键词：2 字以上中文词组 + 3 字母以上英文单词。 */
    private List<String> extractKeywords(String question) {
        List<String> keywords = new ArrayList<>();
        Matcher cn = Pattern.compile("[\\u4e00-\\u9fa5]{2,}").matcher(question);
        while (cn.find()) {
            keywords.add(cn.group());
        }
        Matcher en = Pattern.compile("[a-zA-Z]{3,}").matcher(question);
        while (en.find()) {
            keywords.add(en.group().toLowerCase());
        }
        return keywords;
    }

    private int hitCount(String chunk, List<String> keywords) {
        String lower = chunk.toLowerCase();
        int hits = 0;
        for (String kw : keywords) {
            String k = kw.toLowerCase();
            int idx = 0;
            while ((idx = lower.indexOf(k, idx)) >= 0) {
                hits++;
                idx += k.length();
            }
        }
        return hits;
    }

    private record Score(String chunk, int score) {
    }
}
