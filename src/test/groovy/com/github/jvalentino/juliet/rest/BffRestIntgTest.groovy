package com.github.jvalentino.juliet.rest

import com.github.jvalentino.juliet.doc.api.DocRestApi
import com.github.jvalentino.juliet.doc.model.Doc
import com.github.jvalentino.juliet.doc.model.DocDto
import com.github.jvalentino.juliet.doc.model.DocListDto
import com.github.jvalentino.juliet.doc.model.ViewVersionDto
import com.github.jvalentino.juliet.dto.DashboardDto
import com.github.jvalentino.juliet.dto.HomeDto
import com.github.jvalentino.juliet.dto.LoginDto
import com.github.jvalentino.juliet.dto.ResultDto
import com.github.jvalentino.juliet.user.api.UserRestApi
import com.github.jvalentino.juliet.user.model.AuthUser
import com.github.jvalentino.juliet.user.model.UserDto
import com.github.jvalentino.juliet.util.BaseIntg
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MvcResult
import org.springframework.web.multipart.MultipartFile

import javax.servlet.http.HttpSession

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class BffRestIntgTest extends BaseIntg {

    @MockBean
    DocRestApi docRestApi

    @MockBean
    UserRestApi userRestApi

    @Autowired
    BffRest controller

    void "test index"() {
        given:
        org.mockito.Mockito.when(docRestApi.countDocs()).thenReturn(
                new com.github.jvalentino.juliet.doc.model.CountDto(value:1))
        org.mockito.Mockito.when(userRestApi.performCount()).thenReturn(
                new com.github.jvalentino.juliet.user.model.CountDto(value:2))

        when:
        MvcResult response = mvc.perform(
                get("/"))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn()

        then:
        HomeDto result = this.toObject(response, HomeDto)
        result.users == 2
        result.documents == 1
    }

    void "Test dashboard when not authorized"() {
        when:
        MvcResult response = mvc.perform(
                get("/dashboard"))
                .andDo(print())
                .andExpect(status().is(403))
                .andReturn()

        then:
        true
    }

    void "test dashboard"() {
        given:
        this.mockAdminLoggedIn()

        and:
        List<Doc> docs = [new Doc(name:'alpha.pdf')]
        DocListDto dto = new DocListDto(documents:docs)
        org.mockito.Mockito.when(docRestApi.dashboard()).thenReturn(dto)

        when:
        MvcResult response = mvc.perform(
                get("/dashboard"))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn()

        then:
        DashboardDto result = this.toObject(response, DashboardDto)
        result.documents.size() == 1
        result.documents.first().name == 'alpha.pdf'
    }

    void "test login"() {
        given:
        UserDto input = new UserDto(email:'foo', password:'bar')

        and:
        AuthUser user = new AuthUser(authUserId:2L)
        org.mockito.Mockito.when(userRestApi.login(new UserDto(email:'foo', password:'bar'))).thenReturn(user)

        when:
        MvcResult response = mvc.perform(
                post("/custom-login")
                        .content(toJson(input))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn()

        then:
        LoginDto result = this.toObject(response, LoginDto)
        result.success == true
        result.sessionId != null
    }

    void "Test upload"() {
        given:
        this.mockAdminLoggedIn()

        and:
        MultipartFile file = GroovyMock()

        and:
        DocDto doc = new DocDto()
        doc.with {
            fileName = 'alpha.pdf'
            base64 = 'hello'.bytes.encodeBase64()
        }
        org.mockito.Mockito.when(docRestApi.upload1(1L, doc)).thenReturn(null)

        when:
        ResultDto result = controller.upload(file)

        then:
        1 * file.originalFilename >> 'alpha.pdf'
        1 * file.bytes >> 'hello'.bytes

        and:
        result.success == true
    }

    void "Test view versions"() {
        given:
        this.mockAdminLoggedIn()

        and:
        org.mockito.Mockito.when(docRestApi.versions(1L)).thenReturn(new ViewVersionDto())

        when:
        MvcResult response = mvc.perform(
                get("/view-versions/1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn()

        then:
        ViewVersionDto result = this.toObject(response, ViewVersionDto)
        result
    }

    void "test download version"() {
        given:
        this.mockAdminLoggedIn()

        and:
        DocDto doc = new DocDto()
        doc.with {
            fileName = 'alpha.txt'
            base64 = 'this is a test'.bytes.encodeBase64()
            mimeType = 'text/plain'
        }
        org.mockito.Mockito.when(docRestApi.downloadVersion(1L)).thenReturn(doc)

        when:
        MvcResult response = mvc.perform(
                get("/version/download/1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn()

        then:
        response
        response.response.contentType == 'text/plain'
        new String(response.response.getContentAsByteArray()) == 'this is a test'
    }

    void "Test upload version"() {
        given:
        this.mockAdminLoggedIn()

        and:
        MultipartFile file = GroovyMock()

        and:
        DocDto doc = new DocDto()
        doc.with {
            fileName = 'alpha.pdf'
            base64 = 'hello'.bytes.encodeBase64()
        }
        org.mockito.Mockito.when(docRestApi.upload(1L, 1L, doc)).thenReturn(null)

        when:
        ResultDto result = controller.uploadVersion(file, 1L)

        then:
        1 * file.originalFilename >> 'alpha.pdf'
        1 * file.bytes >> 'hello'.bytes

        and:
        result.success == true
    }
}
