package com.fitness.agent.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fitness.agent.common.BusinessException;
import com.fitness.agent.dto.PlanItemRequest;
import com.fitness.agent.dto.PlanRequest;
import com.fitness.agent.dto.PlanView;
import com.fitness.agent.entity.Plan;
import com.fitness.agent.entity.PlanItem;
import com.fitness.agent.mapper.PlanItemMapper;
import com.fitness.agent.mapper.PlanMapper;
import com.fitness.agent.service.PlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 训练计划业务。主表 plans + 明细 plan_items 一对多。
 * 两个可讲的点：
 * 1. @Transactional：插入主表后再插入多条明细，任一失败整体回滚，不会出现"有头没身子"的脏数据；
 * 2. list 里一次查出所有明细按 planId 分组，避免经典的 N+1 查询（每条计划各查一次明细）。
 */
@Service
@RequiredArgsConstructor
public class PlanServiceImpl implements PlanService {

    private final PlanMapper planMapper;
    private final PlanItemMapper planItemMapper;

    @Override
    @Transactional
    public PlanView create(Long userId, PlanRequest req) {
        Plan plan = Plan.builder()
                .userId(userId)
                .name(req.name())
                .goal(req.goal())
                .startDate(req.startDate())
                .endDate(req.endDate())
                .status("ACTIVE")
                .note(req.note())
                .build();
        planMapper.insert(plan);

        List<PlanItem> items = req.items() == null ? List.of() : req.items().stream()
                .map(i -> PlanItem.builder()
                        .planId(plan.getId())
                        .dayOfWeek(i.dayOfWeek())
                        .exercise(i.exercise())
                        .sets(i.sets())
                        .reps(i.reps())
                        .weight(i.weight())
                        .note(i.note())
                        .build())
                .toList();
        items.forEach(planItemMapper::insert);
        return PlanView.from(plan, items);
    }

    @Override
    @Transactional
    public PlanView generate(Long userId, String name, String goal) {
        List<PlanItemRequest> template = TEMPLATES.getOrDefault(goal, TEMPLATES.get("保持"));
        PlanRequest req = new PlanRequest(
                name, goal, LocalDate.now(), LocalDate.now().plusWeeks(4), "内置模板生成，可自行调整", template);
        return create(userId, req);
    }

    @Override
    public List<PlanView> list(Long userId) {
        List<Plan> plans = planMapper.selectList(
                new LambdaQueryWrapper<Plan>()
                        .eq(Plan::getUserId, userId)
                        .orderByDesc(Plan::getId));
        if (plans.isEmpty()) {
            return List.of();
        }
        List<Long> planIds = plans.stream().map(Plan::getId).toList();
        List<PlanItem> allItems = planItemMapper.selectList(
                new LambdaQueryWrapper<PlanItem>()
                        .in(PlanItem::getPlanId, planIds)
                        .orderByAsc(PlanItem::getDayOfWeek));
        Map<Long, List<PlanItem>> byPlan = allItems.stream()
                .collect(Collectors.groupingBy(PlanItem::getPlanId));
        return plans.stream()
                .map(p -> PlanView.from(p, byPlan.getOrDefault(p.getId(), List.of())))
                .toList();
    }

    @Override
    public PlanView detail(Long userId, Long id) {
        Plan plan = findOwned(userId, id);
        List<PlanItem> items = planItemMapper.selectList(
                new LambdaQueryWrapper<PlanItem>()
                        .eq(PlanItem::getPlanId, id)
                        .orderByAsc(PlanItem::getDayOfWeek));
        return PlanView.from(plan, items);
    }

    @Override
    @Transactional
    public void delete(Long userId, Long id) {
        findOwned(userId, id);
        planItemMapper.delete(new LambdaQueryWrapper<PlanItem>().eq(PlanItem::getPlanId, id));
        planMapper.deleteById(id);
    }

    private Plan findOwned(Long userId, Long id) {
        Plan plan = planMapper.selectOne(
                new LambdaQueryWrapper<Plan>()
                        .eq(Plan::getId, id)
                        .eq(Plan::getUserId, userId));
        if (plan == null) {
            throw new BusinessException(404, "计划不存在");
        }
        return plan;
    }

    /** 内置模板：按训练目标给一周安排（周一=1 ... 周日=7）。 */
    private static final Map<String, List<PlanItemRequest>> TEMPLATES = Map.of(
            "减脂", List.of(
                    new PlanItemRequest(1, "俯卧撑", 4, 12, null, "热身 5 分钟"),
                    new PlanItemRequest(1, "哑铃推举", 4, 12, 5.0, null),
                    new PlanItemRequest(3, "深蹲", 4, 15, null, "自重为主"),
                    new PlanItemRequest(3, "保加利亚分腿蹲", 3, 12, null, null),
                    new PlanItemRequest(5, "波比跳", 6, 30, null, "间歇 30 秒"),
                    new PlanItemRequest(6, "平板支撑", 4, 60, null, "每组 60 秒")),
            "增肌", List.of(
                    new PlanItemRequest(1, "杠铃卧推", 5, 8, null, "大重量低次数"),
                    new PlanItemRequest(1, "哑铃飞鸟", 4, 12, null, null),
                    new PlanItemRequest(3, "引体向上", 4, 8, null, null),
                    new PlanItemRequest(3, "杠铃划船", 4, 10, null, null),
                    new PlanItemRequest(5, "深蹲", 5, 8, null, "大重量"),
                    new PlanItemRequest(5, "站姿推举", 4, 10, null, null),
                    new PlanItemRequest(6, "弯举", 4, 12, null, "手臂孤立"),
                    new PlanItemRequest(6, "三头下压", 4, 12, null, null)),
            "塑形", List.of(
                    new PlanItemRequest(2, "臀桥", 4, 15, null, "臀部激活"),
                    new PlanItemRequest(2, "箭步蹲", 4, 12, null, null),
                    new PlanItemRequest(4, "哑铃划船", 4, 12, null, null),
                    new PlanItemRequest(4, "面拉", 4, 15, null, "改善圆肩"),
                    new PlanItemRequest(6, "平板支撑", 4, 45, null, null),
                    new PlanItemRequest(6, "卷腹", 4, 20, null, null)),
            "保持", List.of(
                    new PlanItemRequest(1, "全身力量循环", 4, 12, null, null),
                    new PlanItemRequest(4, "慢跑", 1, 1, null, "30 分钟有氧"),
                    new PlanItemRequest(6, "拉伸 + 瑜伽", 1, 1, null, "放松恢复")));
}
