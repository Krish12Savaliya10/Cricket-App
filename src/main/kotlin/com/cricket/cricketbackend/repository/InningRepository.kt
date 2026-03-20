package com.cricket.cricketbackend.repository

import com.cricket.cricketbackend.model.entity.InningEntity
import org.springframework.data.jpa.repository.JpaRepository

interface InningRepository : JpaRepository<InningEntity, Int> {
    fun findByMatchIdAndCompletedFalseOrderByInningsNoAsc(matchId: Int): List<InningEntity>
    fun findByMatchIdAndInningsNo(matchId: Int, inningsNo: Int): InningEntity?
    fun findFirstByMatchIdOrderByInningsNoDesc(matchId: Int): InningEntity?
}
