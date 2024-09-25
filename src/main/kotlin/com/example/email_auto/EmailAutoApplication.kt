package com.example.email_auto

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class EmailAutoApplication

fun main(args: Array<String>) {
    runApplication<EmailAutoApplication>(*args)
    args
}
