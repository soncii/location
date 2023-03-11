package com.example.location.services

import com.example.location.entities.Location
import com.example.location.entities.User
import com.example.location.repositories.LocationRepository
import com.example.location.repositories.UserRepository
import spock.lang.Specification
import spock.lang.Subject
import spock.lang.Unroll

import java.util.Optional

class UserServiceTest extends Specification {


    UserRepository userRepository = Mock()
    LocationRepository locationRepository = Mock()
    UserService userService = new UserService(userRepository, locationRepository)
    def "insertUser should return the saved user if not empty"() {
        given:
        User user = new User(firstName: "John", lastName: "Doe", email: "john.doe@example.com", password: "password")

        when:
        User savedUser = userService.insertUser(user)

        then:
        savedUser == user
        1 * userRepository.save(user) >> user
    }

    def "insertUser should return the same user if empty"() {
        given:
        User user = new User()

        when:
        User savedUser = userService.insertUser(user)

        then:
        savedUser == user
        0 * userRepository.save(_)
    }

    def "findUserById should return the user with the given id"() {
        given:
        Long uid = 1L
        User expectedUser = new User(uid: uid, firstName: "John", lastName: "Doe", email: "john.doe@example.com", password: "password")
        1 * userRepository.findById(uid) >> Optional.of(expectedUser)

        when:
        Optional<User> user = userService.findUserById(uid)

        then:
        user.isPresent() == true
        user.get() == expectedUser
    }

    def "authorize should return the user if email and password match"() {
        given:
        String email = "john.doe@example.com"
        String password = "password"
        User expectedUser = new User(firstName: "John", lastName: "Doe", email: email, password: password)
        1 * userRepository.findByEmailAndPassword(email, password) >> Optional.of(expectedUser)

        when:
        Optional<User> user = userService.authorize(email, password)

        then:
        user.isPresent() == true
        user.get() == expectedUser
    }

    def "authorize should return empty if email or password is null"() {
        given:
        String email = "john.doe@example.com"
        String password = "password"

        when:
        Optional<User> user1 = userService.authorize(email, null)
        Optional<User> user2 = userService.authorize(null, password)

        then:
        user1.isPresent() == false
        user2.isPresent() == false
    }


}
