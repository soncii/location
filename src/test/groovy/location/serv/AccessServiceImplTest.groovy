package location.serv

import com.example.location.dto.UserAccessDto
import com.example.location.entities.Access
import com.example.location.entities.User
import com.example.location.repositories.AccessRepository
import com.example.location.repositories.UserRepository
import com.example.location.services.AccessServiceImpl
import spock.lang.Specification

import java.util.concurrent.CompletableFuture

class AccessServiceImplTest extends Specification {

    AccessRepository accessRepository = Stub(AccessRepository)
    UserRepository userRepository = Stub(UserRepository)

    AccessServiceImpl accessService = new AccessServiceImpl(accessRepository, userRepository)

    def "saveAccess returns optional with aid #aid"() {
        def shareMode="admin"
        Long lid = 1L, aid=1L
        Access access = new Access(aid: lid, uid: 1L, lid: aid, type: "admin")

        String email = "test@example.com"
        User user = new User(uid:1L)
        given:
        accessRepository.save(_ as Access) >> access
        userRepository.findByEmail(email) >> Optional.of(user)
        expect:
        accessService.saveAccess(email, shareMode, lid)==(Optional.of(new Access(aid: aid, uid: user.uid, lid: lid, type: shareMode)))

    }

    def "getUsersOnLocation should return list of UserAccessDto"() {
        given:
        Long lid = 1L
        List<UserAccessDto> userAccessList = [
                new UserAccessDto(email: "user1@example.com", accessType: "admin"),
                new UserAccessDto(email: "user2@example.com", accessType: "read-only")
        ]

        accessRepository.getUserAccessByLocationId(lid) >> userAccessList

        when:
        List<UserAccessDto> result = accessService.getUsersOnLocation(lid)

        then:
        result == userAccessList
    }

    def "delete should return true when access is deleted"() {
        given:
        Long uid = 1L
        Long lid = 1L
        String email = "test@example.com"

        userRepository.findByEmail(email) >> Optional.of(new User(uid: uid))
        accessRepository.deleteByUidAndLid(uid, lid) >> 1

        when:
        def result = accessService.delete(uid, lid, email).join()

        then:
        result == true
    }

    def "delete should return false when user doesn't exist"() {
        given:
        Long uid = 1L
        Long lid = 1L
        String email = "test@example.com"

        userRepository.findByEmail(email) >> Optional.empty()

        when:
        def result = accessService.delete(uid, lid, email)

        then:
        result.get() == false
    }

    def "change should return true when access is changed"() {
        given:
        Long lid = 1L
        String email = "test@example.com"
        Access existingAccess = new Access(uid: 1L, lid: lid, type: "admin")
        Access changedAccess = new Access(uid: 1L, lid: lid, type: "read-only")

        userRepository.findByEmail(email) >> Optional.of(new User(uid: 1L))
        accessRepository.findByUidAndLid(1L, lid) >> Optional.of(existingAccess)
        accessRepository.update(changedAccess) >> true

        when:
        def result = accessService.change(lid, email)

        then:
        result.get() == true
    }

    def "change should return false when user doesn't exist"() {
        given:
        Long lid = 1L
        String email = "test@example.com"

        userRepository.findByEmail(email) >> Optional.empty()

        when:
        def result = accessService.change(lid, email)

        then:
        result.get() == false
    }

    def "changeAccess should change access type from admin to read-only"() {
        given:
        Access access = new Access(uid: 1L, lid: 1L, type: "admin")

        when:
        Access result = accessService.changeAccess(access)

        then:
        result.type == "read-only"
    }

    def "changeAccess should change access type from read-only to admin"() {
        given:
        Access access = new Access(uid: 1L, lid: 1L, type: "read-only")

        when:
        Access result = accessService.changeAccess(access)

        then:
        result.type == "admin"
    }

}
