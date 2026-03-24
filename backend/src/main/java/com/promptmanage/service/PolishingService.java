package com.promptmanage.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.promptmanage.common.BusinessException;
import com.promptmanage.entity.PolishingHistory;
import com.promptmanage.mapper.PolishingHistoryMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 提示词润色服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PolishingService {

    private final AiService aiService;
    private final PolishingHistoryMapper polishingHistoryMapper;

    /** 策略定义 */
    private static final Map<String, StrategyInfo> STRATEGIES = Map.of(
            "professional", new StrategyInfo(
                    "专业术语增强",
                    "你是一位资深的内容优化专家。请对用户提供的提示词进行专业化润色：\n" +
                    "1. 补充相关行业词汇和专业术语，提升内容的严谨性和专业度\n" +
                    "2. 替换口语化表达为书面化表达\n" +
                    "3. 确保术语使用准确、一致\n" +
                    "4. 保留原文的核心意图不变\n" +
                    "请直接输出润色后的完整提示词，不要添加额外解释。"
            ),
            "structured", new StrategyInfo(
                    "逻辑结构化",
                    "你是一位资深的内容结构化专家。请对用户提供的提示词进行逻辑结构化润色：\n" +
                    "1. 将内容分点梳理，使逻辑更加清晰\n" +
                    "2. 添加明确的步骤编号或层级结构\n" +
                    "3. 补充必要的上下文和约束条件\n" +
                    "4. 确保每个点表达单一、明确的要求\n" +
                    "请直接输出润色后的完整提示词，不要添加额外解释。"
            ),
            "specific", new StrategyInfo(
                    "语气具体化",
                    "你是一位资深的提示词优化专家。请对用户提供的提示词进行语气具体化润色：\n" +
                    "1. 将模糊的指令细化为具体、可执行的要求\n" +
                    "2. 适当调整语气的强度（更明确、更友好或更严格）\n" +
                    "3. 补充输出格式、长度、风格等具体约束\n" +
                    "4. 消除歧义，使 AI 能更准确地理解指令\n" +
                    "请直接输出润色后的完整提示词，不要添加额外解释。"
            )
    );

    /**
     * 执行润色
     */
    public PolishingHistory polish(String originalText, String strategy) {
        if (originalText == null || originalText.trim().isEmpty()) {
            throw new BusinessException("原始文本不能为空");
        }

        StrategyInfo strategyInfo = STRATEGIES.get(strategy);
        if (strategyInfo == null) {
            throw new BusinessException("无效的润色策略: " + strategy);
        }

        // 调用 AI 润色
        String polishedText = aiService.chat(strategyInfo.systemPrompt, originalText.trim());

        // 保存历史
        PolishingHistory history = new PolishingHistory();
        history.setOriginalText(originalText.trim());
        history.setPolishedText(polishedText);
        history.setStrategy(strategy);
        history.setStrategyName(strategyInfo.name);
        history.setCreatedAt(LocalDateTime.now());
        polishingHistoryMapper.insert(history);

        return history;
    }

    /**
     * 查询历史记录（分页）
     */
    public Page<PolishingHistory> listHistory(Integer pageNum, Integer pageSize) {
        Page<PolishingHistory> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<PolishingHistory> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(PolishingHistory::getCreatedAt);
        return polishingHistoryMapper.selectPage(page, wrapper);
    }

    /**
     * 删除历史记录
     */
    public void deleteHistory(Long id) {
        if (polishingHistoryMapper.selectById(id) == null) {
            throw new BusinessException("记录不存在");
        }
        polishingHistoryMapper.deleteById(id);
    }

    /** 策略信息 */
    private record StrategyInfo(String name, String systemPrompt) {}
}
