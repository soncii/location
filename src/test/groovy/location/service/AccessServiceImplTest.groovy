package location.service

import com.example.location.component.HistoryEventPublisher
import com.example.location.dto.AccessDTO
import com.example.location.dto.UserAccessDto
import com.example.location.entities.Access
import com.example.location.entities.User
import com.example.location.repositories.AccessRepository
import com.example.location.repositories.UserRepository
import com.example.location.services.AccessServiceImpl
import com.example.location.util.NotFoundException
import com.example.location.util.Util
import spock.lang.Specification

import java.util.concurrent.CompletableFuture

class AccessServiceImplTest extends Specification {

    AccessRepository accessRepository = Mock(AccessRepository)
    UserRepository userRepository = Mock(UserRepository)
    HistoryEventPublisher historyEventPublisher = Mock(HistoryEventPublisher)

    AccessServiceImpl accessService = new AccessServiceImpl(accessRepository, userRepository, historyEventPublisher)

    def "saveAccess returns optional with aid 1"() {

        def shareMode = "admin"
        def email = "test@example.com"
        User user = new User(uid: 1L, email: email)
        def lid = 1L, aid = 1L
        def dto = new AccessDTO(lid, email, shareMode)
        def access = new Access(aid: null, uid: user.uid, lid: lid, type: shareMode)

        given:
            accessRepository.save(_ as Access) >> CompletableFuture.completedFuture(access)
            userRepository.findByEmail(email) >> CompletableFuture.completedFuture(Optional.of(user))
            accessRepository.findByUidAndLid(user.uid, lid) >> CompletableFuture.completedFuture(Optional.empty())
        when:
            def result = accessService.saveAccess(dto).join()
        then:
            result == access
            1 * historyEventPublisher.publishHistoryCreatedEvent(user.uid, Util.ObjectType.ACCESS, access)
    }

    def "getUsersOnLocation should return list of UserAccessDto"() {

        given:
            Long lid = 1L
            List<UserAccessDto> userAccessList = [new UserAccessDto(email: "user1@example.com", accessType: "admin"),
                                                  new UserAccessDto(email: "user2@example.com", accessType: "read-only")]

            accessRepository.getUserAccessByLocationId(lid) >> CompletableFuture.completedFuture(userAccessList)

        when:
            List<UserAccessDto> result = accessService.getUsersOnLocation(lid).join()

        then:
            result == userAccessList
    }

    def "delete should return true when access is deleted"() {

        given:
            Long uid = 1L
            Long lid = 1L
            String email = "test@example.com"

            userRepository.findByEmail(email) >> CompletableFuture.completedFuture(Optional.of(new User(uid: uid)))
            accessRepository.deleteByUidAndLid(uid, lid) >> CompletableFuture.completedFuture(1)

        when:
            def result = accessService.delete(uid, lid, email).join()

        then:
            result == true
            1 * historyEventPublisher.publishHistoryDeletedEvent(uid, Util.ObjectType.ACCESS, new Access(null, uid, lid, null))
    }

    def "delete should return false when user doesn't exist"() {

        given:
            Long uid = 1L
            Long lid = 1L
            String email = "test@example.com"

            userRepository.findByEmail(email) >> CompletableFuture.completedFuture(Optional.empty())

        when:
            def result = accessService.delete(uid, lid, email).join()

        then:
            result == false
            0 * historyEventPublisher.publishHistoryDeletedEvent(_)
    }

    def "change should return true when access is changed"() {

        given:
            Long uid = 1L
            Long lid = 1L
            String email = "test@example.com"
            Access existingAccess = new Access(uid: 1L, lid: lid, type: "admin")
            Access changedAccess = new Access(uid: 1L, lid: lid, type: "read-only")

            userRepository.findByEmail(email) >> CompletableFuture.completedFuture(Optional.of(new User(uid: 1L)))
            accessRepository.findByUidAndLid(1L, lid) >> CompletableFuture.completedFuture(Optional.of(existingAccess))
            accessService.changeAccess(_ as Access) >> changedAccess
            accessRepository.update(changedAccess) >> CompletableFuture.completedFuture(true)

        when:
            def result = accessService.change(uid, lid, email).join()

        then:
            result == true
            1 * historyEventPublisher.publishHistoryUpdatedEvent(uid, Util.ObjectType.ACCESS, existingAccess, changedAccess)
    }

    def "change should return false when user doesn't exist"() {

        given:
            Long uid = 1L
            Long lid = 1L
            String email = "test@example.com"

            userRepository.findByEmail(email) >> CompletableFuture.completedFuture(Optional.empty())

        when:
            def result = accessService.change(uid, lid, email).exceptionally { throwable ->
                if (throwable.cause instanceof NotFoundException) {
                    return false
                } else {
                    throw throwable
                }
            }.join()

        then:
            result == false
            0 * historyEventPublisher.publishHistoryUpdatedEvent(_)
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
