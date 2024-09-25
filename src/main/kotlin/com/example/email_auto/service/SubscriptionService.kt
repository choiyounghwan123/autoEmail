package com.example.email_auto.service

import com.example.email_auto.entity.Subscriber
import com.example.email_auto.repository.SubscriptionRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class SubscriptionService @Autowired constructor(
    private val subscriptionRepository: SubscriptionRepository,
    private val emailService: EmailService
) {
    fun subscribe(email: String): Boolean {
        if (subscriptionRepository.findByEmail(email) != null) {
            return false // 이미 구독중임
        }

        val unsubscribeToken: String = UUID.randomUUID().toString() // ?

        val subscriber: Subscriber = Subscriber(email = email, unsubscribeToken = unsubscribeToken)
        subscriptionRepository.save(subscriber)

        emailService.sendSubscriptionEmail(subscriber)

        return true
    }

    fun unsubscribeByEmail(email: String): Boolean {
        val subscriber: Subscriber? = subscriptionRepository.findByEmail(email)

        return if (subscriber != null) {
            subscriptionRepository.delete(subscriber)
            return true
        } else {
            false
        }

    }

    fun unsubscribeByToken(token: String): Boolean {
        val subscriber: Subscriber? = subscriptionRepository.findByUnsubscribeToken(token)

        return if (subscriber != null) {
            subscriptionRepository.delete(subscriber)
            return true
        } else {
            false
        }
    }
}