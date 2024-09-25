package com.example.email_auto.controller

import com.example.email_auto.dto.SubscribeRequest
import com.example.email_auto.service.SubscriptionService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = ["http://10.125.208.187:3000"])
class SubscriptionController(private val subscriptionService: SubscriptionService) {
    @PostMapping("/subscribe")
    fun subscribe(@RequestBody request:SubscribeRequest): ResponseEntity<String> {
        val email = request.email
        val success:Boolean = subscriptionService.subscribe(email)

        return if (success) {
            ResponseEntity.ok("Subscription successful")
        } else {
            ResponseEntity.badRequest().body("Email is already subscribed")
        }
    }

    @DeleteMapping("/unsubscribe")
    fun unsubscribe(@RequestBody payload: Map<String, String>):ResponseEntity<String>{
        val email = payload["email"] ?: return ResponseEntity.badRequest().body("Email is required")
        val success:Boolean = subscriptionService.unsubscribeByEmail(email)

        return if (success) {
            ResponseEntity.ok("Unsubscription successful")
        } else {
            ResponseEntity.status(404).body("Email not found")
        }
    }

    @DeleteMapping("/unsubscribe/token")
    fun unsubscribeByToken(@RequestParam token: String): ResponseEntity<String> {
        val success = subscriptionService.unsubscribeByToken(token)
        return if (success) {
            ResponseEntity.ok("Unsubscription successful")
        } else {
            ResponseEntity.status(404).body("Invalid token")
        }
    }
}