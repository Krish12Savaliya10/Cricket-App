package com.cricket.cricketbackend.service

import kotlin.random.Random

object IdGenerator {
    fun nextLegacyStyleId(): Int = Random.nextInt(100000, 999999)
}
