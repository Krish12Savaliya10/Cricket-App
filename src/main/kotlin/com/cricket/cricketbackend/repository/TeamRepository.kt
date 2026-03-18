package com.cricket.cricketbackend.repository

import com.cricket.cricketbackend.model.entity.TeamEntity
import org.springframework.data.jpa.repository.JpaRepository

interface TeamRepository : JpaRepository<TeamEntity, Int>
