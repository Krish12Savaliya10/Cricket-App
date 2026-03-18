package com.cricket.cricketbackend.controller

import com.cricket.cricketbackend.dto.response.AudienceResponse
import com.cricket.cricketbackend.service.AudienceService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/audience")
class AudienceController(
    private val audienceService: AudienceService,
) {
    @GetMapping("/dashboard")
    fun getDashboard(): AudienceResponse = audienceService.getDashboard()
}
