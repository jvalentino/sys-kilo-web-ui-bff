package com.github.jvalentino.juliet.rest

import com.github.jvalentino.juliet.doc.model.DocDto
import com.github.jvalentino.juliet.doc.model.ViewVersionDto
import com.github.jvalentino.juliet.dto.DashboardDto
import com.github.jvalentino.juliet.dto.HomeDto
import com.github.jvalentino.juliet.dto.LoginDto
import com.github.jvalentino.juliet.dto.ResultDto
import com.github.jvalentino.juliet.service.AuthService
import com.github.jvalentino.juliet.service.BffService
import com.github.jvalentino.juliet.user.model.UserDto
import groovy.transform.CompileDynamic
import groovy.util.logging.Slf4j
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

import javax.servlet.http.HttpServletResponse
import javax.servlet.http.HttpSession

/**
 * Generic services for the BFF
 */
@CompileDynamic
@Slf4j
@RestController
@SuppressWarnings(['UnnecessarySetter', 'UnnecessaryGetter'])
class BffRest {

    @Autowired
    BffService bffService

    @Autowired
    AuthService authService

    @Autowired
    AuthenticationManager authenticationManager

    @GetMapping('/')
    @CircuitBreaker(name = 'Index')
    HomeDto index() {
        bffService.index()
    }

    @GetMapping('/dashboard')
    @CircuitBreaker(name = 'Dashboard')
    DashboardDto dashboard() {
        DashboardDto result = new DashboardDto()
        result.with {
            documents = bffService.allDocs()
        }
        result
    }

    @PostMapping('/custom-login')
    @CircuitBreaker(name = 'CustomLogin')
    LoginDto login(@RequestBody UserDto user, HttpSession session) {
        LoginDto result = authService.login(user, authenticationManager, session)
        result
    }

    @PostMapping('/upload-file')
    @CircuitBreaker(name = 'UploadFile')
    ResultDto upload(@RequestParam('file') MultipartFile file) {
        Long userId = authService.retrieveCurrentlyLoggedInUserIdAsLong()
        bffService.upload(userId, file)
        new ResultDto()
    }

    @GetMapping('/view-versions/{docId}')
    @CircuitBreaker(name = 'ViewVersions')
    ViewVersionDto viewVersions(@PathVariable(value='docId') Long docId) {
        bffService.viewVersions(docId)
    }

    @GetMapping('/version/download/{docVersionId}')
    @CircuitBreaker(name = 'DownloadVersion')
    void downloadVersion(@PathVariable(value='docVersionId') Long docVersionId, HttpServletResponse response) {
        DocDto doc = bffService.retrieveVersion(docVersionId)

        response.setContentType(doc.mimeType)
        response.setHeader('Content-disposition',
                "attachment; filename=${doc.fileName.replaceAll(' ', '')}")

        InputStream is = new ByteArrayInputStream(doc.base64.decodeBase64())
        OutputStream out = response.getOutputStream()

        byte[] buffer = new byte[1048]

        int numBytesRead
        while ((numBytesRead = is.read(buffer)) > 0) {
            out.write(buffer, 0, numBytesRead)
        }
    }

    @PostMapping('/version/new/{docId}')
    @CircuitBreaker(name = 'UploadVersion')
    ResultDto uploadVersion(@RequestParam('file') MultipartFile file, @PathVariable(value='docId') Long docId) {
        Long userId = authService.retrieveCurrentlyLoggedInUserIdAsLong()
        bffService.uploadVersion(userId, docId, file)
        new ResultDto()
    }

}
