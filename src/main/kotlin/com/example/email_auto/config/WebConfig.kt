package com.example.email_auto.config

import com.example.email_auto.service.OctetStreamReadMsgConverter
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class WebConfig @Autowired constructor(
    private val octetStreamReadMsgConverter: OctetStreamReadMsgConverter
) : WebMvcConfigurer{
    override fun configureMessageConverters(converters: MutableList<HttpMessageConverter<*>>) {
        converters.add(octetStreamReadMsgConverter)
    }
}