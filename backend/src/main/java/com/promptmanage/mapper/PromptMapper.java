package com.promptmanage.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.promptmanage.entity.Prompt;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * 提示词 Mapper
 */
@Mapper
public interface PromptMapper extends BaseMapper<Prompt> {

    /**
     * 查询回收站（已逻辑删除的提示词）
     * 注意：MyBatis-Plus 的 selectPage 默认过滤已删除记录，需手写 SQL
     */
    @Select("SELECT * FROM prompt WHERE is_deleted = 1 ORDER BY updated_at DESC")
    Page<Prompt> selectTrashPage(Page<Prompt> page);

    /**
     * 恢复已删除的提示词
     */
    @Update("UPDATE prompt SET is_deleted = 0, updated_at = NOW() WHERE id = #{id}")
    int restoreById(Long id);

    /**
     * 永久删除（物理删除）
     */
    @Update("DELETE FROM prompt WHERE id = #{id}")
    int forceDeleteById(Long id);
}
