package com.example.email_auto.service

import com.example.email_auto.dto.ContentDto
import jakarta.mail.internet.MimeMessage
import org.springframework.core.io.ByteArrayResource
import org.springframework.core.io.InputStreamSource
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile

@Service
class MailService(private val javaMailSender: JavaMailSender) {
    companion object {
        private const val senderEmail = "fdgdfgdgf123@gmail.com"
    }

    fun createMail(contentDto: ContentDto,email:String, images: List<MultipartFile>?):MimeMessage {
        val message: MimeMessage = javaMailSender.createMimeMessage()
        val helper = MimeMessageHelper(message,true)

        helper.setFrom(senderEmail)
        helper.setTo(email)
        helper.setSubject(contentDto.title)

        val body = StringBuilder("""
            URL: ${contentDto.URL}
            <br><br>
            ${contentDto.content.replace("\n", "<br>")}
            <br><br>
        """.trimIndent())

        images?.forEachIndexed{ index, _ ->
            body.append("<img src='cid:image${index}'/>")
        }

        helper.setText(body.toString(),true)

        images?.forEachIndexed { index, image ->
            if (!image.isEmpty) {
                val resource: InputStreamSource = ByteArrayResource(image.bytes)
                helper.addInline("image$index", resource, image.contentType ?: "image/jpeg")
            }
        }
        return message
    }

    fun sendMail(contentDto: ContentDto,email:String, images: List<MultipartFile>?):Boolean{
        try{
            val message = createMail(contentDto,email, images)
            javaMailSender.send(message)
            return true
        }catch (e:Exception){
            e.printStackTrace()
            return false
        }

    }
}