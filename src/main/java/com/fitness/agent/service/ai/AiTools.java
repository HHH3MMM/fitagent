package com.fitness.agent.service.ai;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fitness.agent.entity.BodyMetric;
import com.fitness.agent.entity.Checkin;
import com.fitness.agent.entity.Plan;
import com.fitness.agent.entity.PlanItem;
import com.fitness.agent.mapper.BodyMetricMapper;
import com.fitness.agent.mapper.CheckinMapper;
import com.fitness.agent.mapper.PlanItemMapper;
import com.fitness.agent.mapper.PlanMapper;
import org.springframework.ai.tool.annotation.Tool;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDate;
import java.util.List;

/**
 * 大模型可调用的工具（Function Calling）。
 * 每次请求 new 一个实例并绑定当前 userId——工具方法内部不再关心"谁在问"，
 * 天然防止一个用户的工具调用读到另一个用户的数据（数据隔离）。
 * @Tool 方法会被 Spring AI 扫描成工具描述发给大模型，模型"决定"何时调用。
 */
public class AiTools {

    private final Long userId;
    private final CheckinMapper checkinMapper;
    private final BodyMetricMapper bodyMetricMapper;
    private final PlanMapper planMapper;
    private final PlanItemMapper planItemMapper;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AiTools(Long userId, CheckinMapper checkinMapper, BodyMetricMapper bodyMetricMapper,
                   PlanMapper planMapper, PlanItemMapper planItemMapper) {
        this.userId = userId;
        this.checkinMapper = checkinMapper;
        this.bodyMetricMapper = bodyMetricMapper;
        this.planMapper = planMapper;
        this.planItemMapper = planItemMapper;
    }

    @Tool(description = "获取用户最近指定天数的体重/胸围/腰围/臀围/体脂率记录，参数 days 表示天数")
    public String getRecentMetrics(int days) {
        LocalDate from = LocalDate.now().minusDays(days - 1L);
        List<BodyMetric> list = bodyMetricMapper.selectList(
                new LambdaQueryWrapper<BodyMetric>()
                        .eq(BodyMetric::getUserId, userId)
                        .ge(BodyMetric::getRecordDate, from)
                        .orderByAsc(BodyMetric::getRecordDate));
        return toJson(list);
    }

    @Tool(description = "查看用户今天是否已经打卡")
    public String isCheckedToday() {
        Long count = checkinMapper.selectCount(
                new LambdaQueryWrapper<Checkin>()
                        .eq(Checkin::getUserId, userId)
                        .eq(Checkin::getCheckinDate, LocalDate.now()));
        return count > 0 ? "今天已打卡" : "今天未打卡";
    }

    @Tool(description = "获取用户的连续打卡天数")
    public String getStreak() {
        List<Checkin> list = checkinMapper.selectList(
                new LambdaQueryWrapper<Checkin>()
                        .eq(Checkin::getUserId, userId)
                        .select(Checkin::getCheckinDate)
                        .orderByDesc(Checkin::getCheckinDate));
        if (list.isEmpty()) {
            return "0 天";
        }
        LocalDate expect = LocalDate.now();
        if (!list.get(0).getCheckinDate().equals(expect)) {
            expect = expect.minusDays(1);
        }
        int streak = 0;
        for (Checkin c : list) {
            if (c.getCheckinDate().equals(expect)) {
                streak++;
                expect = expect.minusDays(1);
            } else {
                break;
            }
        }
        return streak + " 天";
    }

    @Tool(description = "获取用户当前进行中的训练计划以及每周安排明细")
    public String getActivePlans() {
        List<Plan> plans = planMapper.selectList(
                new LambdaQueryWrapper<Plan>()
                        .eq(Plan::getUserId, userId)
                        .eq(Plan::getStatus, "ACTIVE"));
        if (plans.isEmpty()) {
            return "暂无进行中的训练计划";
        }
        List<Long> planIds = plans.stream().map(Plan::getId).toList();
        List<PlanItem> items = planItemMapper.selectList(
                new LambdaQueryWrapper<PlanItem>().in(PlanItem::getPlanId, planIds));
        return toJson(plans) + "\n明细:" + toJson(items);
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception e) {
            return String.valueOf(value);
        }
    }
}
