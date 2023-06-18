package location

import com.example.location.controllers.MainController
import com.example.location.entities.Location
import com.example.location.entities.User
import com.example.location.services.LocationServiceImpl
import com.example.location.services.UserServiceImpl
import org.springframework.ui.Model
import spock.lang.Specification
import spock.lang.Subject
import spock.lang.Unroll
import javax.servlet.http.Cookie
import javax.servlet.http.HttpServletResponse
import java.util.concurrent.CompletableFuture

class MainControllerTest extends Specification {



    UserServiceImpl userService = Stub()
    LocationServiceImpl locationService = Stub()

    @Subject
    MainController mainController = new MainController(userService,locationService)

    @Unroll
    def "should return the #expectedView view"() {
        given:
        Model model = Mock(Model)
        String expectedView = exview

        when:
        def view = mainController."${method}"(model)

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
        String view = mainController.index("empty", model).join()

        then:
        view == "indexNotLogged"
    }

    def "should redirect to /login with error when user is not authorized"() {
        given:
        HttpServletResponse response = Mock(HttpServletResponse)
        userService.authorize("invalid-email", "invalid-password") >> CompletableFuture.completedFuture(Optional.empty())
        when:
        String view = mainController.loginUser("invalid-email", "invalid-password", response).join()

        then:
        view == "redirect:/login?error=true"
    }

    @Unroll
    def "should redirect to / when user is authorized"() {
        given:
        HttpServletResponse response = Mock(HttpServletResponse)
        User user = new User(uid: uid)



        when:
        userService.authorize(email, password) >> CompletableFuture.completedFuture( Optional.of(user))
        String view = mainController.loginUser(email, password, response).join()

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
        userService.insertUser(user) >> CompletableFuture.completedFuture(new User(uid: 1L))

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

        locationService.findUserLocations(uid) >> CompletableFuture.completedFuture(locations)

        when:
        String view = mainController.index(uid, model).join()

        then:
        view == expectedView

        where:
        uid         | expectedView
        "1"         | "locations"
        "empty"     | "indexNotLogged"
    }
}


