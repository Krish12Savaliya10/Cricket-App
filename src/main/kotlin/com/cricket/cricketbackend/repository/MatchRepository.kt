package com.cricket.cricketbackend.repository

import com.cricket.cricketbackend.model.entity.MatchEntity
import com.cricket.cricketbackend.model.entity.MatchStatus
import com.cricket.cricketbackend.model.entity.MatchType
import org.springframework.data.jpa.repository.JpaRepository

interface MatchRepository : JpaRepository<MatchEntity, Int> {
    fun findAllByHostId(hostId: Int): List<MatchEntity>
    fun findAllByMatchStatus(matchStatus: MatchStatus): List<MatchEntity>
    fun findAllByMatchType(matchType: MatchType): List<MatchEntity>
    fun findAllByMatchTypeAndMatchTypeId(matchType: MatchType, matchTypeId: Int): List<MatchEntity>
    fun existsByMatchStatusAndIdNot(matchStatus: MatchStatus, id: Int): Boolean
}
