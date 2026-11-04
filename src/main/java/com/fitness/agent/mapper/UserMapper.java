package com.fitness.agent.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fitness.agent.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * MyBatis-Plus 数据访问层：继承 BaseMapper 就自带 insert / selectById /
 * selectOne / selectCount 等通用方法，不用写 SQL；复杂查询再加 @Select 或 XML。
 * 与 JPA 的 Repository 等价，区别是这里明确基于 MyBatis，SQL 可控。
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
}
