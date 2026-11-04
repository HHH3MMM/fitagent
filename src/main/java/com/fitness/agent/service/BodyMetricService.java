package com.fitness.agent.service;

import com.fitness.agent.dto.BodyMetricRequest;
import com.fitness.agent.dto.MetricTrendPoint;
import com.fitness.agent.entity.BodyMetric;

import java.util.List;

public interface BodyMetricService {

    /** 新增三维记录。同一天只允许一条。 */
    BodyMetric record(Long userId, BodyMetricRequest req);

    List<BodyMetric> list(Long userId);

    BodyMetric update(Long userId, Long id, BodyMetricRequest req);

    void delete(Long userId, Long id);

    /** 最近 N 天的趋势点（按日期升序），供曲线图使用。 */
    List<MetricTrendPoint> trend(Long userId, int days);
}
