package com.cricket.cricketbackend.repository

import com.cricket.cricketbackend.model.entity.BallByBallEntity
import org.springframework.data.jpa.repository.JpaRepository

interface BallByBallRepository : JpaRepository<BallByBallEntity, Int> {
    fun findAllByMatchIdAndInningsIdOrderBySequenceNoAsc(matchId: Int, inningsId: Int): List<BallByBallEntity>
    fun findFirstByMatchIdAndInningsIdOrderBySequenceNoDesc(matchId: Int, inningsId: Int): BallByBallEntity?
}
