package com.fitness.agent.service;

import com.fitness.agent.dto.CheckinRequest;
import com.fitness.agent.entity.Checkin;

import java.util.List;

public interface CheckinService {

    /** 打卡。同一用户同一天只能一次，重复打卡抛 409。 */
    Checkin checkin(Long userId, CheckinRequest req);

    List<Checkin> list(Long userId);

    /** 连续打卡天数。今天还没打不打断连续（从昨天往前数）。 */
    int streak(Long userId);

    void delete(Long userId, Long id);
}
