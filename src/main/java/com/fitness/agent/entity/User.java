package com.fitness.agent.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 用户表。表名用 users 而非 user：user 是 MySQL 保留字。
 * MyBatis-Plus 注解版：@TableName 映射表名，@TableId 主键（AUTO=数据库自增），
 * createdAt/updatedAt 由 MybatisPlusMetaHandler 自动填充。
 */
@TableName("users")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String username;

    /** password 存的是 BCrypt 哈希，绝不存明文（安全面试必考点） */
    private String password;

    private String nickname;

    /** 训练目标：减脂 / 增肌 / 塑形 / 保持 */
    private String goal;

    /** 身高（cm），BMI 等计算会用到 */
    private Double height;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
