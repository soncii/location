package com.example.location

import com.example.location.dto.UserAccessDto
import com.example.location.entities.Access
import com.example.location.entities.User
import com.example.location.repositories.AccessRepository
import com.example.location.repositories.UserRepository
import com.example.location.services.AccessService
import spock.lang.Specification
import spock.lang.Unroll

import java.util.List
import java.util.Optional

class AccessServiceTest extends Specification {
    UserRepository userRepository = Mock(UserRepository)
    AccessRepository accessRepository = Mock(AccessRepository)
    AccessService accessService = new AccessService(userRepository, accessRepository)
    Long lid = 1L
    String email = "test@example.com"
    User user = new User(email: email, uid: 1L)
    Access access = new Access(aid: 1L, uid: 1L, lid: 1L, type: "admin")

    def setup() {
        userRepository.findByEmail(email) >> Optional.of(user)
        accessRepository.findByUidAndLid(user.uid, lid) >> Optional.of(access)
    }

    @Unroll
    def "saveAccess returns optional with aid #aid"() {
        given:
        accessRepository.save(_ as Access) >> access

        expect:
        accessService.saveAccess(email, shareMode, lid) == Optional.of(new Access(aid: aid, uid: user.uid, lid: lid, type: shareMode))

        where:
        shareMode | aid
        "admin"   | 1L
        "read-only" | 2L
    }

    def "getUsersOnLocation returns user access dto list"() {
        given:
        accessRepository.getUserAccessByLocationId(lid) >> [new UserAccessDto(email: email, accessType: "admin")]

        expect:
        accessService.getUsersOnLocation(lid) == [new UserAccessDto(email: email, accessType: "admin")]
    }

    def "delete returns true when deleteByUidAndLid returns 1"() {
        given:
        accessRepository.deleteByUidAndLid(user.uid, lid) >> 1

        expect:
        accessService.delete(user.uid, lid, email) == true
    }

    def "delete returns false when user is not found"() {
        given:
        userRepository.findByEmail(email) >> Optional.empty()

        expect:
        accessService.delete(user.uid, lid, email) == false
    }

    def "change updates access type and returns true"() {
        expect:
        accessService.change(lid, email) == true

        where:
        accessType | expectedType
        "admin" | "read-only"
        "read-only" | "admin"
    }
}
