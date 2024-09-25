package com.example.email_auto.entity

import jakarta.persistence.*

@Entity
@Table(name = "subscriber")
class Subscriber (
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id:Long? = null,

    @Column(nullable = false, unique = true)
    val email: String = "",

    @Column(name = "unsubscribe_token", nullable = false)
    val unsubscribeToken: String = ""
)