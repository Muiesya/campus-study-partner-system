package com.campus.partner.repository;

import com.campus.partner.entity.Collaboration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CollaborationRepository extends JpaRepository<Collaboration, Long> {
    @Query("SELECT c FROM Collaboration c WHERE c.studentA.id = :id1 AND c.studentB.id = :id2")
    Optional<Collaboration> findPair(@Param("id1") Long id1, @Param("id2") Long id2);
}
