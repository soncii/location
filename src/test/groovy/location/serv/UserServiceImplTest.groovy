package location.serv

import com.example.location.component.HistoryEventPublisher
import com.example.location.entities.Location
import com.example.location.entities.User
import com.example.location.repositories.LocationRepository
import com.example.location.repositories.UserRepository
import com.example.location.services.UserServiceImpl
import com.example.location.util.DbException
import spock.lang.Specification

import java.util.concurrent.CompletableFuture

class UserServiceImplTest extends Specification {

    UserRepository userRepository = Stub(UserRepository)

    LocationRepository locationRepository = Stub(LocationRepository)

    HistoryEventPublisher historyEventPublisher = Mock(HistoryEventPublisher)

    UserServiceImpl userService = new UserServiceImpl(userRepository, locationRepository, historyEventPublisher)

    def "authorize should return user when email and password are valid"() {

        given:
            def email = "test@example.com"
            def password = "password"
            def expectedUser = new User(email: email, password: password)
            userRepository.findByEmailAndPassword(email, password) >> CompletableFuture.completedFuture(Optional.of(expectedUser))

        when:
            def result = userService.authorize(email, password).join()

        then:
            result.isPresent()
            result.get() == expectedUser
    }

    def "authorize should return empty optional when email is null"() {

        when:
            def result = userService.authorize(null, "password").join()

        then:
            !result.isPresent()
    }

    def "authorize should return empty optional when password is null"() {

        when:
            def result = userService.authorize("test@example.com", null).join()

        then:
            !result.isPresent()
    }

    def "insertUser should save the user when it is not empty"() {

        given:
            def user = new User(firstName: "John", lastName: "Doe", email: "test@example.com", password: "password")
            def saved = new User(uid: 1L, firstName: "John", lastName: "Doe", email: "test@example.com", password: "password")
            userRepository.save(user) >> CompletableFuture.completedFuture(saved)

        when:
            def result = userService.insertUser(user).join()

        then:
            result == saved
            1 * historyEventPublisher.publishHistoryCreatedEvent(saved.uid, "USER", saved)
    }

    def "findUserById should return user optional when the user exists"() {

        given:
            def uid = 1L
            def expectedUser = new User(uid: uid)
            userRepository.findById(uid) >> CompletableFuture.completedFuture(Optional.of(expectedUser))

        when:
            def result = userService.findUserById(uid).join()

        then:
            result.isPresent()
            result.get() == expectedUser
    }

    def "findUserById should return empty optional when the user does not exist"() {

        given:
            def uid = 1L
            userRepository.findById(uid) >> CompletableFuture.completedFuture(Optional.empty())

        when:
            def result = userService.findUserById(uid).join()

        then:
            result == Optional.empty()
    }

    def "authorizeOwner should return true when uidString is a valid Long and Location exists"() {

        given:
            def uidString = "1"
            def lid = 1L
            locationRepository.findByUidAndLid(1L, lid) >> CompletableFuture.completedFuture(Optional.of(new Location()))

        when:
            def result = userService.authorizeOwner(uidString, lid).join()

        then:
            result
    }

    def "authorizeOwner should return false when Location does not exist"() {

        given:
            def uidString = "1"
            def lid = 1L
            locationRepository.findByUidAndLid(_, _) >> CompletableFuture.completedFuture(Optional.empty())

        when:
            def result = userService.authorizeOwner(uidString, lid).join()

        then:
            !result
    }

    def "deleteUser should return true and publish event when user exists and is deleted successfully"() {

        given:
            def uid = 1L
            userRepository.deleteById(uid) >> CompletableFuture.completedFuture(true)

        when:
            def result = userService.deleteUser(uid).join()

        then:
            result == true
            1 * historyEventPublisher.publishHistoryDeletedEvent(uid, "USER", uid)
    }

    def "deleteUser should return throw exception and don't publish event when user does not exist"() {

        given:
            def uid = 2L
            userRepository.deleteById(uid) >> CompletableFuture.completedFuture(false)

        when:
            def result = userService.deleteUser(uid).exceptionally({ thrown ->

                if (thrown.cause instanceof DbException) {
                    return false
                } else {
                    throw thrown
                }
            }).join()

        then:
            result == false
            0 * historyEventPublisher.publishHistoryDeletedEvent(_)
    }

    def "isValidEmail should return true for valid email addresses"() {

        expect:
            userService.isValidEmail("test@example.com")
            userService.isValidEmail("john.doe@example.co.uk")
            userService.isValidEmail("info+test@example.com")
    }

    def "isValidEmail should return false for invalid email addresses"() {

        expect:
            !userService.isValidEmail("test@example")
            !userService.isValidEmail("john.doe@example..co.uk")
            !userService.isValidEmail("test@")
            !userService.isValidEmail("@example.com")
    }
}
