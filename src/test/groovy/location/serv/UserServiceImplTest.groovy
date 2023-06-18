package location.serv

import com.example.location.entities.Location
import com.example.location.entities.User
import com.example.location.repositories.LocationRepository
import com.example.location.repositories.UserRepository
import com.example.location.services.UserServiceImpl
import spock.lang.AutoCleanup
import spock.lang.Shared
import spock.lang.Specification
import spock.util.concurrent.BlockingVariable
import spock.util.concurrent.PollingConditions

import java.util.Optional
import java.util.concurrent.CompletableFuture

class UserServiceImplTest extends Specification {

    UserRepository userRepository = Stub(UserRepository)

    LocationRepository locationRepository = Stub(LocationRepository)

    UserServiceImpl userService = new UserServiceImpl(userRepository, locationRepository)

    def "authorize should return user when email and password are valid"() {
        given:
        def email = "test@example.com"
        def password = "password"
        def expectedUser = new User(email: email, password: password)
        userRepository.findByEmailAndPassword(email, password) >> Optional.of(expectedUser)

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
        userRepository.save(user) >> user

        when:
        def result = userService.insertUser(user).join()

        then:
        result == user
    }

    def "insertUser should return the user without saving when it is empty"() {
        given:
        def user = new User()

        when:
        def result = userService.insertUser(user).join()

        then:
        result == user
        0 * userRepository.save(_)
    }

    def "findUserById should return user optional when the user exists"() {
        given:
        def uid = 1L
        def expectedUser = new User(uid: uid)
        userRepository.findById(uid) >> Optional.of(expectedUser)

        when:
        def result = userService.findUserById(uid).join()

        then:
        result.isPresent()
        result.get() == expectedUser
    }



    def "findUserById should return empty optional when the user does not exist"() {
        given:
        def uid = 1L
        userRepository.findById(uid) >> Optional.empty()

        def result = new BlockingVariable<Optional<User>>()

        when:
        userService.findUserById(uid).thenAccept({ x -> result.set(x) })

        then:
        result.get() == Optional.empty()

    }



    def "authorizeOwner should return true when uidString is a valid Long and Location exists"() {
        given:
        def uidString = "1"
        def lid = 1L
        locationRepository.findByUidAndLid(_, _) >> Optional.of(new Location())

        when:
        def result = userService.authorizeOwner(uidString, lid).join()

        then:
        result
    }

    def "authorizeOwner should return false when uidString is not a valid Long"() {
        when:
        def result = userService.authorizeOwner("invalid", 1L).join()

        then:
        !result
    }

    def "authorizeOwner should return false when Location does not exist"() {
        given:
        def uidString = "1"
        def lid = 1L
        locationRepository.findByUidAndLid(_, _) >> Optional.empty()

        when:
        def result = userService.authorizeOwner(uidString, lid).join()

        then:
        !result
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
