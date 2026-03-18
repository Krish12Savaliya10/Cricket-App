package com.cricket.cricketbackend.controller

import com.cricket.cricketbackend.dto.request.CreateSeriesRequest
import com.cricket.cricketbackend.dto.response.SeriesResponse
import com.cricket.cricketbackend.service.SeriesService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/series")
class SeriesController(
    private val seriesService: SeriesService,
) {
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createSeries(@Valid @RequestBody request: CreateSeriesRequest): SeriesResponse = seriesService.createSeries(request)

    @GetMapping
    fun listSeries(@RequestParam(required = false) status: String?): List<SeriesResponse> = seriesService.listSeries(status)

    @GetMapping("/{seriesId}")
    fun getSeries(@PathVariable seriesId: Int): SeriesResponse = seriesService.getSeries(seriesId)
}
