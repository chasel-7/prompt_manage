package com.promptmanage.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.promptmanage.entity.PromptTag;
import org.apache.ibatis.annotations.Mapper;

/**
 * 提示词-标签关联 Mapper
 */
@Mapper
public interface PromptTagMapper extends BaseMapper<PromptTag> {
}
