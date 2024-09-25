package com.example.email_auto.repository

import com.example.email_auto.entity.Subscriber
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface SubscriptionRepository : JpaRepository<Subscriber,Long>{
    fun findByEmail(email:String): Subscriber?

    fun findByUnsubscribeToken(unsubscribeToken: String): Subscriber?
}