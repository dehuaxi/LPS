package com.defei.lps.dao;

import com.defei.lps.entity.PlanTakeRecord;
import org.springframework.stereotype.Repository;

@Repository
public interface PlanTakeRecordMapper {
    int deleteByPrimaryKey(Integer id);

    int insertSelective(PlanTakeRecord record);

    PlanTakeRecord selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(PlanTakeRecord record);
}