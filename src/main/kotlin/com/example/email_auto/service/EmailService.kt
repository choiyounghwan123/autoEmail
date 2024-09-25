package com.example.email_auto.service

import com.example.email_auto.dto.ContentDto
import com.example.email_auto.entity.Subscriber
import com.example.email_auto.repository.SubscriptionRepository
import jakarta.mail.MessagingException
import jakarta.mail.internet.MimeMessage
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.ByteArrayResource
import org.springframework.core.io.InputStreamSource
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile

@Service
class EmailService(
    private val javaMailSender: JavaMailSender,
    private val subscriptionRepository: SubscriptionRepository,
    @Value("\${email.sender}") private val senderEmail:String
) {


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

    fun sendMailToAll(contentDto: ContentDto,  images: List<MultipartFile>?):Boolean{
        return try{
            val subscribers:List<Subscriber> = subscriptionRepository.findAll()
            subscribers.forEach { subscriber ->
                val message = createMail(
                    contentDto = contentDto,
                    email = subscriber.email,
                    images = images
                )
                javaMailSender.send(message)
            }
            true
        }catch (e: MessagingException){
            e.printStackTrace()
            false
        }
        catch (e:Exception){
            e.printStackTrace()
            false
        }
    }

    fun sendMail(contentDto: ContentDto,email:String, images: List<MultipartFile>?):Boolean{
        try{
            val message = createMail(contentDto,email, images)
            javaMailSender.send(message)
            return true
        }catch (e: MessagingException){
            e.printStackTrace()
            return false
        }
        catch (e:Exception){
            e.printStackTrace()
            return false
        }

    }


    fun sendSubscriptionEmail(subscriber: Subscriber){
        val unsubscribeUrl = "http://localhost:3000/unsubscribe?token=${subscriber.unsubscribeToken}"

        val message = """
            구독해주셔서 감사합니다!
            
            이제부터 공지사항을 받아보실수있습니다! 
                       
            구독을 취소하시려면 다음 링크를 클릭하세요:
            $unsubscribeUrl
        """.trimIndent()
        val email = SimpleMailMessage()
        email.setTo(subscriber.email)
        email.subject = "구독완료 메일!"
        email.text = message

        javaMailSender.send(email)
    }
}