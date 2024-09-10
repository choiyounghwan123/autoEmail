package com.example.email_auto.controller

import com.example.email_auto.dto.ContentDto
import com.example.email_auto.service.MailService
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/api/emails")
class MailController(private val mailService: MailService) {

    private val logger = LoggerFactory.getLogger(MailController::class.java)

    @PostMapping("/send")
    fun mailSend(@RequestPart("content") contentDto: ContentDto,
                 @RequestPart("images", required = false) images: List<MultipartFile>?)
    :ResponseEntity<Any>{

        if(images != null && !images.isNotEmpty()){
            for (image in images) {
                if (!image.isEmpty) {
                    val fileName = image.originalFilename
                    // Handle the image (e.g., save it, validate it)
                }
            }
        }

        val email:String = "fdgdfgdgf123@icloud.com"
        return try {
            val success:Boolean = mailService.sendMail(contentDto,email,images)
            if(success){
                ResponseEntity.status(HttpStatus.CREATED).body(mapOf("message" to "Email sent successfully"))
            }else{
                ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(mapOf("error" to "Faild to send email"))
            }
        }catch (e:Exception){
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body(mapOf("error" to "Invalid request", "details" to e.message))
        }
    }
}