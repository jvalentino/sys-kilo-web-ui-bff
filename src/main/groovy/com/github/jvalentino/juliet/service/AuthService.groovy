package com.github.jvalentino.juliet.service

import com.github.jvalentino.juliet.dto.LoginDto
import com.github.jvalentino.juliet.dto.ResultDto
import com.github.jvalentino.juliet.user.api.UserRestApi
import com.github.jvalentino.juliet.user.model.AuthUser
import com.github.jvalentino.juliet.user.model.UserDto
import groovy.transform.CompileDynamic
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service

import javax.servlet.http.HttpSession

/**
 * Service for dealing with the magic of authorization and session
 * @author john.valentino
 */
@Slf4j
@CompileDynamic
@Service
@SuppressWarnings(['UnnecessaryGetter', 'UnnecessarySetter'])
class AuthService {

    AuthService instance = this

    @Autowired
    UserRestApi userRestApi

    ResultDto login(UserDto user, AuthenticationManager authenticationManager, HttpSession session) {
        log.info('Attempting to login the user user by email of ' + user.email)

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(user.email, user.password))
            SecurityContextHolder.getContext().setAuthentication(authentication)
            String sessionId = session.getId()
            log.info("${user.email} is logged in with session ID ${sessionId}")
            return new LoginDto(sessionId:sessionId, sessionIdBase64:sessionId.bytes.encodeBase64())
        } catch (e) {
            log.error("${user.email} gave invalid credentials", e)
        }

        new LoginDto(success:false, message:'Invalid Credentials')
    }

    @SuppressWarnings(['ThrowException'])
    Authentication authenticate(Authentication authentication) {
        UsernamePasswordAuthenticationToken auth = authentication

        String authUserId = instance.retrieveCurrentlyLoggedInUserId()
        log.info("Authenticating ${authUserId}...")

        // if they have not logged in, do so
        if (authUserId == 'anonymousUser' || authUserId == null) {
            log.info('Not logged in to we have to first login...')
            AuthUser user = instance.isValidUser(auth.getPrincipal(), auth.getCredentials())
            if (user != null) {
                return new UsernamePasswordAuthenticationToken(user.authUserId, auth.getCredentials())
            }

            throw new Exception('Invalid username and/or password')
        }

        // they are already logged in
        log.info("${authUserId} is already logged in")
        authentication
    }

    AuthUser isValidUser(String email, String password) {
        try {
            AuthUser user = userRestApi.login(new UserDto(email:email, password:password))
            return user
        } catch (e) {
            log.error('Unable to login', e)
        }

        null
    }

    String retrieveCurrentlyLoggedInUserId() {
        SecurityContextHolder.getContext().getAuthentication()?.getPrincipal()
    }

    Long retrieveCurrentlyLoggedInUserIdAsLong() {
        this.retrieveCurrentlyLoggedInUserId().toLong()
    }

}
