package com.promptmanage.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.promptmanage.common.BusinessException;
import com.promptmanage.dto.PromptQueryParam;
import com.promptmanage.dto.PromptRequest;
import com.promptmanage.entity.Prompt;
import com.promptmanage.entity.PromptTag;
import com.promptmanage.entity.Tag;
import com.promptmanage.mapper.PromptMapper;
import com.promptmanage.mapper.PromptTagMapper;
import com.promptmanage.mapper.TagMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 提示词服务
 */
@Service
@RequiredArgsConstructor
public class PromptService {

    private final PromptMapper promptMapper;
    private final PromptTagMapper promptTagMapper;
    private final TagMapper tagMapper;

    /**
     * 分页查询提示词
     */
    public Page<Prompt> listPrompts(PromptQueryParam param) {
        Page<Prompt> page = new Page<>(param.getPageNum(), param.getPageSize());

        // 如果按标签过滤，先查出关联的 promptId
        Set<Long> filteredPromptIds = null;
        if (param.getTagId() != null) {
            LambdaQueryWrapper<PromptTag> ptWrapper = new LambdaQueryWrapper<>();
            ptWrapper.eq(PromptTag::getTagId, param.getTagId());
            List<PromptTag> ptList = promptTagMapper.selectList(ptWrapper);
            filteredPromptIds = ptList.stream().map(PromptTag::getPromptId).collect(Collectors.toSet());
            if (filteredPromptIds.isEmpty()) {
                return page; // 没有匹配的提示词
            }
        }

        LambdaQueryWrapper<Prompt> wrapper = new LambdaQueryWrapper<>();

        // 关键词模糊搜索（标题、描述、内容）
        if (StringUtils.hasText(param.getKeyword())) {
            String kw = param.getKeyword().trim();
            wrapper.and(w -> w
                    .like(Prompt::getTitle, kw)
                    .or().like(Prompt::getDescription, kw)
                    .or().like(Prompt::getContent, kw));
        }

        // 分组过滤
        if (param.getGroupId() != null) {
            wrapper.eq(Prompt::getGroupId, param.getGroupId());
        }

        // 标签过滤
        if (filteredPromptIds != null) {
            wrapper.in(Prompt::getId, filteredPromptIds);
        }

        // 收藏过滤
        if (Boolean.TRUE.equals(param.getFavorite())) {
            wrapper.eq(Prompt::getIsFavorite, 1);
        }

        // 排序：收藏优先，然后按指定字段排序
        wrapper.orderByDesc(Prompt::getIsFavorite);
        String sortBy = param.getSortBy();
        boolean isAsc = "asc".equalsIgnoreCase(param.getSortOrder());
        switch (sortBy) {
            case "created_at" -> {
                if (isAsc) {
                    wrapper.orderByAsc(Prompt::getCreatedAt);
                } else {
                    wrapper.orderByDesc(Prompt::getCreatedAt);
                }
            }
            case "title" -> {
                if (isAsc) {
                    wrapper.orderByAsc(Prompt::getTitle);
                } else {
                    wrapper.orderByDesc(Prompt::getTitle);
                }
            }
            default -> {
                if (isAsc) {
                    wrapper.orderByAsc(Prompt::getUpdatedAt);
                } else {
                    wrapper.orderByDesc(Prompt::getUpdatedAt);
                }
            }
        }

        promptMapper.selectPage(page, wrapper);

        // 填充标签信息
        fillTags(page.getRecords());

        return page;
    }

    /**
     * 查询提示词详情
     */
    public Prompt getPromptById(Long id) {
        Prompt prompt = promptMapper.selectById(id);
        if (prompt == null) {
            throw new BusinessException("提示词不存在");
        }
        fillTags(Collections.singletonList(prompt));
        return prompt;
    }

    /**
     * 创建提示词
     */
    @Transactional(rollbackFor = Exception.class)
    public Prompt createPrompt(PromptRequest request) {
        Prompt prompt = new Prompt();
        prompt.setTitle(request.getTitle());
        prompt.setContent(request.getContent());
        prompt.setDescription(request.getDescription());
        prompt.setGroupId(request.getGroupId());
        prompt.setIsFavorite(0);
        prompt.setIsDeleted(0);
        prompt.setCreatedAt(LocalDateTime.now());
        prompt.setUpdatedAt(LocalDateTime.now());
        promptMapper.insert(prompt);

        // 保存标签关联
        savePromptTags(prompt.getId(), request.getTagIds());

        fillTags(Collections.singletonList(prompt));
        return prompt;
    }

    /**
     * 更新提示词
     */
    @Transactional(rollbackFor = Exception.class)
    public Prompt updatePrompt(Long id, PromptRequest request) {
        Prompt prompt = promptMapper.selectById(id);
        if (prompt == null) {
            throw new BusinessException("提示词不存在");
        }

        prompt.setTitle(request.getTitle());
        prompt.setContent(request.getContent());
        prompt.setDescription(request.getDescription());
        prompt.setGroupId(request.getGroupId());
        prompt.setUpdatedAt(LocalDateTime.now());
        promptMapper.updateById(prompt);

        // 更新标签关联：先删后增
        LambdaQueryWrapper<PromptTag> delWrapper = new LambdaQueryWrapper<>();
        delWrapper.eq(PromptTag::getPromptId, id);
        promptTagMapper.delete(delWrapper);
        savePromptTags(id, request.getTagIds());

        fillTags(Collections.singletonList(prompt));
        return prompt;
    }

    /**
     * 逻辑删除提示词
     */
    public void deletePrompt(Long id) {
        Prompt prompt = promptMapper.selectById(id);
        if (prompt == null) {
            throw new BusinessException("提示词不存在");
        }
        promptMapper.deleteById(id);
    }

    /**
     * 切换收藏状态
     */
    public Prompt toggleFavorite(Long id) {
        Prompt prompt = promptMapper.selectById(id);
        if (prompt == null) {
            throw new BusinessException("提示词不存在");
        }
        prompt.setIsFavorite(prompt.getIsFavorite() == 1 ? 0 : 1);
        prompt.setUpdatedAt(LocalDateTime.now());
        promptMapper.updateById(prompt);
        fillTags(Collections.singletonList(prompt));
        return prompt;
    }

    /**
     * 查询回收站列表
     */
    public Page<Prompt> listTrash(Integer pageNum, Integer pageSize) {
        Page<Prompt> page = new Page<>(pageNum, pageSize);
        return promptMapper.selectTrashPage(page);
    }

    /**
     * 恢复已删除的提示词
     */
    public void restorePrompt(Long id) {
        int rows = promptMapper.restoreById(id);
        if (rows == 0) {
            throw new BusinessException("提示词不存在或已恢复");
        }
    }

    /**
     * 永久删除提示词
     */
    @Transactional(rollbackFor = Exception.class)
    public void forceDeletePrompt(Long id) {
        // 删除标签关联
        LambdaQueryWrapper<PromptTag> ptWrapper = new LambdaQueryWrapper<>();
        ptWrapper.eq(PromptTag::getPromptId, id);
        promptTagMapper.delete(ptWrapper);

        // 物理删除（绕过逻辑删除）
        promptMapper.forceDeleteById(id);
    }

    // ==================== 私有方法 ====================

    /**
     * 保存提示词-标签关联
     */
    private void savePromptTags(Long promptId, List<Long> tagIds) {
        if (!CollectionUtils.isEmpty(tagIds)) {
            for (Long tagId : tagIds) {
                PromptTag pt = new PromptTag();
                pt.setPromptId(promptId);
                pt.setTagId(tagId);
                promptTagMapper.insert(pt);
            }
        }
    }

    /**
     * 填充提示词的标签信息
     */
    private void fillTags(List<Prompt> prompts) {
        if (CollectionUtils.isEmpty(prompts)) {
            return;
        }

        Set<Long> promptIds = prompts.stream().map(Prompt::getId).collect(Collectors.toSet());

        // 查询所有关联
        LambdaQueryWrapper<PromptTag> ptWrapper = new LambdaQueryWrapper<>();
        ptWrapper.in(PromptTag::getPromptId, promptIds);
        List<PromptTag> ptList = promptTagMapper.selectList(ptWrapper);

        if (ptList.isEmpty()) {
            prompts.forEach(p -> {
                p.setTagIds(new ArrayList<>());
                p.setTagNames(new ArrayList<>());
            });
            return;
        }

        // 查询所有标签
        Set<Long> tagIds = ptList.stream().map(PromptTag::getTagId).collect(Collectors.toSet());
        Map<Long, String> tagNameMap;
        if (!tagIds.isEmpty()) {
            List<Tag> tags = tagMapper.selectBatchIds(tagIds);
            tagNameMap = tags.stream().collect(Collectors.toMap(Tag::getId, Tag::getName));
        } else {
            tagNameMap = Collections.emptyMap();
        }

        // 按 promptId 分组
        Map<Long, List<PromptTag>> ptMap = ptList.stream()
                .collect(Collectors.groupingBy(PromptTag::getPromptId));

        for (Prompt prompt : prompts) {
            List<PromptTag> pts = ptMap.getOrDefault(prompt.getId(), Collections.emptyList());
            prompt.setTagIds(pts.stream().map(PromptTag::getTagId).collect(Collectors.toList()));
            prompt.setTagNames(pts.stream()
                    .map(pt -> tagNameMap.getOrDefault(pt.getTagId(), ""))
                    .filter(name -> !name.isEmpty())
                    .collect(Collectors.toList()));
        }
    }
}
