package com.talentmarketplace.repository;

import com.talentmarketplace.entity.DemandSkill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DemandSkillRepository extends JpaRepository<DemandSkill, Long> {
    List<DemandSkill> findByDemandId(Long demandId);
}
