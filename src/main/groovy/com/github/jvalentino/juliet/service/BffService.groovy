package com.github.jvalentino.juliet.service

import com.github.jvalentino.juliet.doc.api.DocRestApi
import com.github.jvalentino.juliet.doc.model.Doc
import com.github.jvalentino.juliet.doc.model.DocDto
import com.github.jvalentino.juliet.doc.model.ViewVersionDto
import com.github.jvalentino.juliet.dto.HomeDto
import com.github.jvalentino.juliet.user.api.UserRestApi
import groovy.transform.CompileDynamic
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile

/**
 * General service class for supporting the REST endpoints
 */
@CompileDynamic
@Slf4j
@Service
class BffService {

    @Autowired
    DocRestApi docRestApi

    @Autowired
    UserRestApi userRestApi

    List<Doc> allDocs() {
        docRestApi.dashboard().documents
    }

    HomeDto index() {
        HomeDto result = new HomeDto()
        result.with {
            documents = docRestApi.countDocs().value
            users = userRestApi.performCount().value
        }
        result
    }

    void upload(Long userId, MultipartFile file) {
        DocDto doc = new DocDto()
        doc.with {
            fileName = file.originalFilename
            base64 = file.bytes.encodeBase64()
        }
        docRestApi.upload1(userId, doc)
    }

    ViewVersionDto viewVersions(Long docId) {
        docRestApi.versions(docId)
    }

    DocDto retrieveVersion(Long docVersionId) {
        docRestApi.downloadVersion(docVersionId)
    }

    void uploadVersion(Long userId, Long docId, MultipartFile file) {
        DocDto doc = new DocDto()
        doc.with {
            fileName = file.originalFilename
            base64 = file.bytes.encodeBase64()
        }
        docRestApi.upload(docId, userId, doc)
    }

}
