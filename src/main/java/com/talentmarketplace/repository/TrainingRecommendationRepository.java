package com.talentmarketplace.repository;

import com.talentmarketplace.entity.TrainingRecommendation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TrainingRecommendationRepository extends JpaRepository<TrainingRecommendation, Long> {
    List<TrainingRecommendation> findByEmployeeId(Long employeeId);
    List<TrainingRecommendation> findByStatus(TrainingRecommendation.RecommendationStatus status);

    @Query("SELECT tr FROM TrainingRecommendation tr JOIN FETCH tr.employee JOIN FETCH tr.skill WHERE tr.employee.id = :employeeId")
    List<TrainingRecommendation> findByEmployeeIdWithDetails(@Param("employeeId") Long employeeId);

    @Query("SELECT tr FROM TrainingRecommendation tr JOIN FETCH tr.employee JOIN FETCH tr.skill ORDER BY tr.createdAt DESC")
    List<TrainingRecommendation> findAllWithDetails();
}
