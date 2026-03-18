package com.cricket.cricketbackend.model.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "Users")
class UserEntity(
    @Id
    @Column(name = "user_id")
    var id: Int = 0,
    @Column(name = "full_name", nullable = false)
    var fullName: String = "",
    @Column(name = "email", nullable = false, unique = true)
    var email: String = "",
    @Column(name = "password", nullable = false)
    var password: String = "",
    @Column(name = "role", nullable = false)
    var role: String = "",
)
