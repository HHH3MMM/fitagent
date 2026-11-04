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
 * 训练计划明细（子表）：某一天练什么动作、几组几次多重。
 */
@TableName("plan_items")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlanItem {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long planId;

    private Integer dayOfWeek;

    private String exercise;

    private Integer sets;

    private Integer reps;

    private Double weight;

    private String note;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
