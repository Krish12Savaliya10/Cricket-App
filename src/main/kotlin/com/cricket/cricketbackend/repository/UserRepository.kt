package com.cricket.cricketbackend.repository

import com.cricket.cricketbackend.model.entity.UserEntity
import org.springframework.data.jpa.repository.JpaRepository

interface UserRepository : JpaRepository<UserEntity, Int>
