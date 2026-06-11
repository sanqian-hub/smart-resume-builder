package com.srb.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.srb.backend.model.entity.RememberLoginToken;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface RememberLoginTokenMapper extends BaseMapper<RememberLoginToken> {
}
