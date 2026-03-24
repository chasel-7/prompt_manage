package com.promptmanage.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.promptmanage.entity.ChatMessage;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ChatMessageMapper extends BaseMapper<ChatMessage> {
}
