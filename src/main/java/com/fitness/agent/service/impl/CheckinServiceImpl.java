package com.fitness.agent.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fitness.agent.common.BusinessException;
import com.fitness.agent.dto.CheckinRequest;
import com.fitness.agent.entity.Checkin;
import com.fitness.agent.mapper.CheckinMapper;
import com.fitness.agent.service.CheckinService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

/**
 * 打卡业务。
 * "一天只能打一次"做了两层防护：
 * 1. 应用层：先 selectCount 查今天是否已打卡（快路径，给用户友好提示）；
 * 2. 数据库层：UNIQUE(user_id, checkin_date) 唯一索引兜底（并发场景两个请求同时进来，
 *    应用层都查不到，但数据库只允许一条插入成功）——这就是"校验放应用、约束放数据库"。
 * streak 连续天数：按日期降序，从最近一天（今天没打则从昨天）往前数连续。
 */
@Service
@RequiredArgsConstructor
public class CheckinServiceImpl implements CheckinService {

    private final CheckinMapper checkinMapper;

    @Override
    public Checkin checkin(Long userId, CheckinRequest req) {
        LocalDate today = LocalDate.now();
        Long count = checkinMapper.selectCount(
                new LambdaQueryWrapper<Checkin>()
                        .eq(Checkin::getUserId, userId)
                        .eq(Checkin::getCheckinDate, today));
        if (count > 0) {
            throw new BusinessException(409, "今天已经打过卡了");
        }
        Checkin checkin = Checkin.builder()
                .userId(userId)
                .checkinDate(today)
                .note(req.note())
                .build();
        checkinMapper.insert(checkin);
        return checkin;
    }

    @Override
    public List<Checkin> list(Long userId) {
        return checkinMapper.selectList(
                new LambdaQueryWrapper<Checkin>()
                        .eq(Checkin::getUserId, userId)
                        .orderByDesc(Checkin::getCheckinDate));
    }

    @Override
    public int streak(Long userId) {
        List<Checkin> list = checkinMapper.selectList(
                new LambdaQueryWrapper<Checkin>()
                        .eq(Checkin::getUserId, userId)
                        .select(Checkin::getCheckinDate)
                        .orderByDesc(Checkin::getCheckinDate));
        if (list.isEmpty()) {
            return 0;
        }
        LocalDate expect = LocalDate.now();
        if (!list.get(0).getCheckinDate().equals(expect)) {
            expect = expect.minusDays(1);   // 今天还没打：从昨天开始数，不打断连续
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
        return streak;
    }

    @Override
    public void delete(Long userId, Long id) {
        Checkin existing = findOwned(userId, id);
        checkinMapper.deleteById(existing.getId());
    }

    /** 查"属于当前用户"的记录：带 userId 条件，防止越权删别人的数据。 */
    private Checkin findOwned(Long userId, Long id) {
        Checkin checkin = checkinMapper.selectOne(
                new LambdaQueryWrapper<Checkin>()
                        .eq(Checkin::getId, id)
                        .eq(Checkin::getUserId, userId));
        if (checkin == null) {
            throw new BusinessException(404, "打卡记录不存在");
        }
        return checkin;
    }
}
