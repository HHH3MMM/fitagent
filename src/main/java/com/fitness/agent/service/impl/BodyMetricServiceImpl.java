package com.fitness.agent.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fitness.agent.common.BusinessException;
import com.fitness.agent.dto.BodyMetricRequest;
import com.fitness.agent.dto.MetricTrendPoint;
import com.fitness.agent.entity.BodyMetric;
import com.fitness.agent.mapper.BodyMetricMapper;
import com.fitness.agent.service.BodyMetricService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

/**
 * 三维记录业务。与打卡一样，同一天一条靠唯一索引兜底。
 * trend 只取最近 N 天且按日期升序，前端直接画折线。
 */
@Service
@RequiredArgsConstructor
public class BodyMetricServiceImpl implements BodyMetricService {

    private final BodyMetricMapper bodyMetricMapper;

    @Override
    public BodyMetric record(Long userId, BodyMetricRequest req) {
        Long count = bodyMetricMapper.selectCount(
                new LambdaQueryWrapper<BodyMetric>()
                        .eq(BodyMetric::getUserId, userId)
                        .eq(BodyMetric::getRecordDate, req.recordDate()));
        if (count > 0) {
            throw new BusinessException(409, "该日期已有记录，请用更新");
        }
        BodyMetric metric = toEntity(userId, req, null);
        bodyMetricMapper.insert(metric);
        return metric;
    }

    @Override
    public List<BodyMetric> list(Long userId) {
        return bodyMetricMapper.selectList(
                new LambdaQueryWrapper<BodyMetric>()
                        .eq(BodyMetric::getUserId, userId)
                        .orderByDesc(BodyMetric::getRecordDate));
    }

    @Override
    public BodyMetric update(Long userId, Long id, BodyMetricRequest req) {
        BodyMetric metric = findOwned(userId, id);
        BodyMetric updated = toEntity(userId, req, metric.getId());
        bodyMetricMapper.updateById(updated);
        return updated;
    }

    @Override
    public void delete(Long userId, Long id) {
        BodyMetric metric = findOwned(userId, id);
        bodyMetricMapper.deleteById(metric.getId());
    }

    @Override
    public List<MetricTrendPoint> trend(Long userId, int days) {
        LocalDate from = LocalDate.now().minusDays(days - 1L);
        List<BodyMetric> list = bodyMetricMapper.selectList(
                new LambdaQueryWrapper<BodyMetric>()
                        .eq(BodyMetric::getUserId, userId)
                        .ge(BodyMetric::getRecordDate, from)
                        .orderByAsc(BodyMetric::getRecordDate));
        return list.stream()
                .map(m -> new MetricTrendPoint(m.getRecordDate(), m.getWeight(), m.getChest(),
                        m.getWaist(), m.getHip(), m.getBodyFat()))
                .toList();
    }

    private BodyMetric findOwned(Long userId, Long id) {
        BodyMetric metric = bodyMetricMapper.selectOne(
                new LambdaQueryWrapper<BodyMetric>()
                        .eq(BodyMetric::getId, id)
                        .eq(BodyMetric::getUserId, userId));
        if (metric == null) {
            throw new BusinessException(404, "记录不存在");
        }
        return metric;
    }

    private BodyMetric toEntity(Long userId, BodyMetricRequest req, Long id) {
        return BodyMetric.builder()
                .id(id)
                .userId(userId)
                .recordDate(req.recordDate())
                .weight(req.weight())
                .chest(req.chest())
                .waist(req.waist())
                .hip(req.hip())
                .bodyFat(req.bodyFat())
                .note(req.note())
                .build();
    }
}
