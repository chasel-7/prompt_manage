package com.promptmanage.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.promptmanage.common.BusinessException;
import com.promptmanage.entity.PromptTag;
import com.promptmanage.entity.Tag;
import com.promptmanage.mapper.PromptTagMapper;
import com.promptmanage.mapper.TagMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 标签服务
 */
@Service
@RequiredArgsConstructor
public class TagService {

    private final TagMapper tagMapper;
    private final PromptTagMapper promptTagMapper;

    /**
     * 获取所有标签
     */
    public List<Tag> listTags() {
        LambdaQueryWrapper<Tag> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(Tag::getCreatedAt);
        return tagMapper.selectList(wrapper);
    }

    /**
     * 创建标签
     */
    public Tag createTag(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new BusinessException("标签名称不能为空");
        }
        name = name.trim();

        // 检查名称是否重复
        LambdaQueryWrapper<Tag> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Tag::getName, name);
        if (tagMapper.selectCount(wrapper) > 0) {
            throw new BusinessException("标签名称已存在");
        }

        Tag tag = new Tag();
        tag.setName(name);
        tag.setCreatedAt(LocalDateTime.now());
        tagMapper.insert(tag);
        return tag;
    }

    /**
     * 删除标签
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteTag(Long id) {
        Tag tag = tagMapper.selectById(id);
        if (tag == null) {
            throw new BusinessException("标签不存在");
        }

        // 删除关联关系
        LambdaQueryWrapper<PromptTag> ptWrapper = new LambdaQueryWrapper<>();
        ptWrapper.eq(PromptTag::getTagId, id);
        promptTagMapper.delete(ptWrapper);

        tagMapper.deleteById(id);
    }
}
