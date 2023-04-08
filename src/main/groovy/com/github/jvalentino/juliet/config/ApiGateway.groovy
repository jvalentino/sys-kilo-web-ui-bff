package com.github.jvalentino.juliet.config

import com.github.jvalentino.juliet.doc.api.DocRestApi
import com.github.jvalentino.juliet.user.api.UserRestApi
import groovy.transform.CompileDynamic
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * General API access
 * @author john.valentino
 */
@CompileDynamic
@Configuration
class ApiGateway {

    @Value('${management.apiKeyDoc}')
    String apiKeyDoc

    @Value('${management.apiKeyUser}')
    String apiKeyUser

    @Value('${management.apiDocUrl}')
    String apiDocUrl

    @Value('${management.apiUserUrl}')
    String apiUserUrl

    @Bean
    DocRestApi docRestApi() {
        DocRestApi api = new DocRestApi()
        api.apiClient.apiKey = apiKeyDoc
        api.apiClient.basePath = apiDocUrl
        api
    }

    @Bean
    UserRestApi userRestApi() {
        UserRestApi api = new UserRestApi()
        api.apiClient.apiKey = apiKeyUser
        api.apiClient.basePath = apiUserUrl
        api
    }

}
