package com.github.jvalentino.juliet.util

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.MvcResult
import spock.lang.Specification




@ExtendWith(SpringExtension)
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(
        locations = "classpath:integration.properties")
abstract class BaseIntg extends Specification {

    @Autowired
    MockMvc mvc


    Object toObject(MvcResult response, Class clazz) {
        String json = response.getResponse().getContentAsString()
        new ObjectMapper().readValue(json, clazz)
    }

    void mockAdminLoggedIn() {
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken('1', null)
        SecurityContextHolder.getContext().setAuthentication(authentication)
    }

    String toJson(Object obj) {
        new ObjectMapper().writeValueAsString(obj)
    }

}
