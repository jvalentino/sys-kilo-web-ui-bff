package com.github.jvalentino.juliet


import org.springframework.boot.SpringApplication
import spock.lang.Specification

class KiloUiBffAppTest extends Specification {

    def setup() {
        GroovyMock(SpringApplication, global:true)
    }

    def "test main"() {
        when:
        KiloUiBffApp.main(null)

        then:
        1 * SpringApplication.run(KiloUiBffApp, null)
    }

}
