package location

import com.example.location.entities.Location
import com.example.location.entities.User
import com.example.location.repositories.AccessRepository
import com.example.location.repositories.LocationRepository
import com.example.location.repositories.UserRepository
import com.example.location.services.LocationServiceImpl
import com.example.location.services.UserServiceImpl
import spock.lang.Specification

class IntegrationTest extends Specification {


    LocationRepository locationRepository = Mock()
    UserRepository userRepository = Mock()
    AccessRepository accessRepository = Mock()
    def userService = new UserServiceImpl(userRepository, locationRepository)
    def locationService = new LocationServiceImpl(locationRepository, accessRepository, userRepository);
    def setup() {

    }
    def "should be able to create a user account, login and add location"() {
        given:

        def email = "test@example.com"
        def name = "Test"
        def lastname = "User"
        def password = "password"
        def user = new User(uid: 1, email: email, firstName: name, lastName: lastname, password: password)
        userRepository.save(user) >> user

        def location = new Location(name: "home", address: "kaban 53", uid: 1)
        locationRepository.save(_ as Location) >> location
        userRepository.findByEmailAndPassword(user.getEmail(), user.password) >> Optional.of(user)
        userRepository.findById(Long.parseLong("1")) >> Optional.of(user)
        when:
        def user1 = userService.insertUser(user).join()
        def authorized = userService.authorize(user1.getEmail(), user1.getPassword()).join()
        def savedlocation = locationService.saveLocation(user1.getUid().toString(), "home", "kaban 53").join()
        then:
        user1 == user
        authorized == Optional.of(user)
        savedlocation == location
    }

}
