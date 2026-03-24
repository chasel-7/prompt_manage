package com.promptmanage.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.promptmanage.common.BusinessException;
import com.promptmanage.dto.GroupRequest;
import com.promptmanage.entity.Prompt;
import com.promptmanage.entity.PromptGroup;
import com.promptmanage.mapper.PromptGroupMapper;
import com.promptmanage.mapper.PromptMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 提示词分组服务
 */
@Service
@RequiredArgsConstructor
public class PromptGroupService {

    private final PromptGroupMapper promptGroupMapper;
    private final PromptMapper promptMapper;

    /**
     * 获取所有分组（按排序序号升序）
     */
    public List<PromptGroup> listGroups() {
        LambdaQueryWrapper<PromptGroup> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(PromptGroup::getSortOrder)
                .orderByDesc(PromptGroup::getCreatedAt);
        List<PromptGroup> groups = promptGroupMapper.selectList(wrapper);

        // 填充每个分组下的提示词数量
        for (PromptGroup group : groups) {
            LambdaQueryWrapper<Prompt> countWrapper = new LambdaQueryWrapper<>();
            countWrapper.eq(Prompt::getGroupId, group.getId());
            long count = promptMapper.selectCount(countWrapper);
            group.setPromptCount((int) count);
        }

        return groups;
    }

    /**
     * 创建分组
     */
    public PromptGroup createGroup(GroupRequest request) {
        // 检查名称是否重复
        LambdaQueryWrapper<PromptGroup> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PromptGroup::getName, request.getName());
        if (promptGroupMapper.selectCount(wrapper) > 0) {
            throw new BusinessException("分组名称已存在");
        }

        PromptGroup group = new PromptGroup();
        group.setName(request.getName());
        group.setDescription(request.getDescription());
        group.setSortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0);
        group.setCreatedAt(LocalDateTime.now());
        group.setUpdatedAt(LocalDateTime.now());
        promptGroupMapper.insert(group);
        return group;
    }

    /**
     * 更新分组
     */
    public PromptGroup updateGroup(Long id, GroupRequest request) {
        PromptGroup group = promptGroupMapper.selectById(id);
        if (group == null) {
            throw new BusinessException("分组不存在");
        }

        // 检查名称是否与其他分组重复
        LambdaQueryWrapper<PromptGroup> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PromptGroup::getName, request.getName())
                .ne(PromptGroup::getId, id);
        if (promptGroupMapper.selectCount(wrapper) > 0) {
            throw new BusinessException("分组名称已存在");
        }

        group.setName(request.getName());
        group.setDescription(request.getDescription());
        if (request.getSortOrder() != null) {
            group.setSortOrder(request.getSortOrder());
        }
        group.setUpdatedAt(LocalDateTime.now());
        promptGroupMapper.updateById(group);
        return group;
    }

    /**
     * 删除分组
     */
    public void deleteGroup(Long id) {
        PromptGroup group = promptGroupMapper.selectById(id);
        if (group == null) {
            throw new BusinessException("分组不存在");
        }

        // 检查分组下是否有提示词
        LambdaQueryWrapper<Prompt> countWrapper = new LambdaQueryWrapper<>();
        countWrapper.eq(Prompt::getGroupId, id);
        long count = promptMapper.selectCount(countWrapper);
        if (count > 0) {
            throw new BusinessException("该分组下还有 " + count + " 个提示词，无法删除");
        }

        promptGroupMapper.deleteById(id);
    }
}
