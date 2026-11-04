package com.fitness.agent.service.impl;

import com.fitness.agent.common.BusinessException;
import com.fitness.agent.dto.AiAnalysisResponse;
import com.fitness.agent.entity.User;
import com.fitness.agent.mapper.BodyMetricMapper;
import com.fitness.agent.mapper.CheckinMapper;
import com.fitness.agent.mapper.PlanItemMapper;
import com.fitness.agent.mapper.PlanMapper;
import com.fitness.agent.mapper.UserMapper;
import com.fitness.agent.service.AiCoachService;
import com.fitness.agent.service.ai.AiTools;
import com.fitness.agent.service.ai.KnowledgeRetriever;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

/**
 * AI 教练服务——本项目的 Agent 核心，一条请求里的完整链路：
 *
 * 1. 用户消息进来，先做 RAG 检索：从健身知识库按关键词捞相关片段；
 * 2. 组装 System Prompt：教练人设 + 用户画像 + 检索到的知识片段；
 * 3. 注册工具（AiTools 绑定当前用户）：模型发现"需要看数据"时会主动调用工具拿真实数据；
 * 4. 大模型综合人设/知识/数据生成回答。
 *
 * 这四步就是 Agent 与普通 API 调用的分水岭：模型不只是"翻译"输入，
 * 而是拥有知识来源和行动能力（工具），可以按需获取上下文再作答。
 */
@Service
@RequiredArgsConstructor
public class AiCoachServiceImpl implements AiCoachService {

    private final ChatClient.Builder chatClientBuilder;
    private final KnowledgeRetriever knowledgeRetriever;
    private final UserMapper userMapper;
    private final CheckinMapper checkinMapper;
    private final BodyMetricMapper bodyMetricMapper;
    private final PlanMapper planMapper;
    private final PlanItemMapper planItemMapper;
    private final ObjectMapper objectMapper;

    @Override
    public String chat(Long userId, String message) {
        User user = loadUser(userId);
        String rag = knowledgeRetriever.retrieve(message, 3);
        String systemPrompt = """
                你是 FitAgent 的 AI 健身教练，兼任注册营养师，性格专业且鼓励。
                用户画像：训练目标=%s，身高=%s。
                回答规则：
                1. 需要用户数据时必须调用工具获取，绝不允许编造数字；
                2. 优先参考下面知识库资料回答，资料与用户情况冲突时以资料为准；
                3. 用中文，先给结论再给理由，建议要具体可执行（带数字）；
                4. 回答控制在 200 字以内，不要重复用户的话。

                知识库资料：
                %s
                """.formatted(user.getGoal(), user.getHeight() == null ? "未知" : user.getHeight() + "cm", rag);
        return client().prompt()
                .system(systemPrompt)
                .user(message)
                .tools(tools(userId))
                .call()
                .content();
    }

    @Override
    public AiAnalysisResponse analysis(Long userId) {
        User user = loadUser(userId);
        String rag = knowledgeRetriever.retrieve("训练 营养 恢复 体重 体脂 建议", 3);
        String systemPrompt = """
                你是 FitAgent 的 AI 健身教练兼营养师。基于用户真实数据做阶段性分析。
                请只输出 JSON（不要 markdown 代码块、不要多余文字），格式：
                {"summary":"总体评价(1-2句)", "highlights":"做得好的地方", "suggestions":"需要改进的地方", "nextStep":"下一步最该做的一件事"}

                用户画像：目标=%s，身高=%s。
                知识库资料：
                %s
                """.formatted(user.getGoal(), user.getHeight() == null ? "未知" : user.getHeight() + "cm", rag);
        String content = client().prompt()
                .system(systemPrompt)
                .user("请分析我的健身进展")
                .tools(tools(userId))
                .call()
                .content();
        return parseAnalysis(content);
    }

    private ChatClient client() {
        return chatClientBuilder.build();
    }

    /** 每次请求创建绑定当前 userId 的工具集，保证数据隔离。 */
    private AiTools tools(Long userId) {
        return new AiTools(userId, checkinMapper, bodyMetricMapper, planMapper, planItemMapper);
    }

    private User loadUser(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        return user;
    }

    private AiAnalysisResponse parseAnalysis(String content) {
        if (content == null) {
            return new AiAnalysisResponse("暂无数据", "", "先多记录几次数据", "坚持记录一周后再分析");
        }
        try {
            JsonNode node = objectMapper.readTree(extractJson(content));
            return new AiAnalysisResponse(
                    node.path("summary").asText(""),
                    node.path("highlights").asText(""),
                    node.path("suggestions").asText(""),
                    node.path("nextStep").asText(""));
        } catch (Exception e) {
            // 模型没按 JSON 格式输出时兜底：整段文本放入 summary，不把解析错误抛给用户
            return new AiAnalysisResponse(content, "", "", "");
        }
    }

    /** 从模型输出里截取第一个 { 到最后一个 }，容忍 markdown 等噪音。 */
    private String extractJson(String content) {
        int start = content.indexOf('{');
        int end = content.lastIndexOf('}');
        if (start >= 0 && end > start) {
            return content.substring(start, end + 1);
        }
        return content;
    }
}
