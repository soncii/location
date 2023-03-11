package com.example.location.controllers

import com.example.location.entities.Location
import com.example.location.entities.User
import com.example.location.services.LocationService
import com.example.location.services.UserService
import org.springframework.core.annotation.AnnotationUtils
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.CookieValue
import org.springframework.web.bind.annotation.RequestParam
import spock.lang.Specification
import spock.lang.Subject
import spock.lang.Unroll
import javax.servlet.http.Cookie
import javax.servlet.http.HttpServletResponse

class MainControllerTest extends Specification {

    @Subject
    MainController mainController = new MainController()

    UserService userService = Mock(UserService)
    LocationService locationService = Mock(LocationService)

    def setup() {
        mainController.userService = userService
        mainController.locationService = locationService
    }

    @Unroll
    def "should return the #expectedView view"() {
        given:
        Model model = Mock(Model)
        String expectedView = exview

        when:
        String view = mainController."${method}"(model)

        then:
        view == expectedView

        where:
        method        | exview
        "registerGet" | "register"
        "login"       | "login"
    }

    def "should redirect to /login when user is not logged in"() {
        given:
        Model model = Mock(Model)
        when:
        String view = mainController.index("empty", model)

        then:
        view == "indexNotLogged"
    }

    def "should redirect to /login with error when user is not authorized"() {
        given:
        HttpServletResponse response = Mock(HttpServletResponse)
        userService.authorize("invalid-email", "invalid-password") >> Optional.empty()
        when:
        String view = mainController.loginUser("invalid-email", "invalid-password", response)

        then:
        view == "redirect:/login?error=true"
    }

    @Unroll
    def "should redirect to / when user is authorized"() {
        given:
        HttpServletResponse response = Mock(HttpServletResponse)
        User user = new User(uid: uid)



        when:
        userService.authorize(email, password) >> Optional.of(user)
        String view = mainController.loginUser(email, password, response)

        then:
        view == "redirect:/"
        response.addCookie(_) >> { Cookie cookie ->
            cookie.getName() == "user" && cookie.getValue() == uid.toString()
        }

        where:
        email          | password          | uid
        "test-email"   | "test-password"   | 1L
        "test-email-2" | "test-password-2" | 2L
    }

    def "should insert the user and redirect to /login"() {
        given:
        User user = new User(firstName:"John", lastName: "Example", email: "john@example.com", password: "password")
        userService.insertUser(user) >> new User(uid: 1L)

        when:
        String view = mainController.registerUser(user)

        then:
        view == "redirect:/login"
    }

    @Unroll
    def "should return the expected view and set the user locations model attribute"() {
        given:
        Model model = Mock(Model)
        List<Location> locations = [new Location(name: "Location 1"), new Location(name: "Location 2")]

        locationService.findUserLocations(uid) >> locations

        when:
        String view = mainController.index(uid, model)

        then:
        view == expectedView

        where:
        uid         | expectedView
        "1"         | "locations"
        "empty"     | "indexNotLogged"
    }
}


