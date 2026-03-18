package com.cricket.cricketbackend.repository

import com.cricket.cricketbackend.model.entity.SeriesEntity
import org.springframework.data.jpa.repository.JpaRepository

interface SeriesRepository : JpaRepository<SeriesEntity, Int> {
    fun findAllByHostId(hostId: Int): List<SeriesEntity>
    fun findAllBySeriesStatus(seriesStatus: String): List<SeriesEntity>
}
