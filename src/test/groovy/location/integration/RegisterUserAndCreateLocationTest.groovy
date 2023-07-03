package location.integration

import com.example.location.LocationApplication
import com.example.location.configuration.AppConfig
import com.example.location.controllers.MainController
import com.example.location.dto.LocationDTO
import com.example.location.dto.LoginDTO
import com.example.location.entities.Location
import com.example.location.entities.User
import com.example.location.services.LocationServiceImpl
import com.example.location.services.UserServiceImpl
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Primary
import org.springframework.jdbc.datasource.DriverManagerDataSource
import org.springframework.test.annotation.Rollback
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.web.client.RestTemplate
import spock.lang.Shared
import spock.lang.Specification
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import spock.lang.Stepwise

import javax.sql.DataSource

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    classes = [LocationApplication.class])
@EnableAutoConfiguration
class RegisterUserAndCreateLocationTest extends Specification {

    private TestRestTemplate restTemplate = new TestRestTemplate()
    ObjectMapper objectMapper = new ObjectMapper();
    private HttpHeaders headers = new HttpHeaders()
    @Value('${local.server.port}')
    private int port
    @Shared
    private long UID
    @Shared
    private long LID

    private final static String EMAIL = "integrationtest123@gmail.com"

    private final static String PASSWORD = "password"

    def setup() {

        headers.setContentType(MediaType.APPLICATION_JSON)
    }

    def "Test registering a user"() {

        given:
            def url = "http://localhost:" + port + "/register"
            def user = new User(null, "User", "Test", EMAIL, PASSWORD)
            def request = new HttpEntity<>(objectMapper.writeValueAsString(user), headers)

        when:
            def response = restTemplate.postForEntity(url, request, User)
        then:
            response.statusCode == HttpStatus.OK
            response.getBody().getUid() != null
            response.getBody().getEmail() == user.getEmail()
            response.getBody().getFirstName() == user.getFirstName()
            response.getBody().getLastName() == user.getLastName()
            response.getBody().getPassword() == user.getPassword()
    }

    def "Testing user login"() {

        given:
            def url = "http://localhost:" + port + "/login"
            def login = new LoginDTO(EMAIL, PASSWORD)
        when:
            def response = restTemplate.postForEntity(url, login, Void)
            UID = response.getHeaders().get("Authorization").get(0).toLong()
        then:
            response.statusCode == HttpStatus.OK
            response.getHeaders().get("Authorization").get(0) != null
    }

    def "Test user login with incorrect password"() {

        given:
            def url = "http://localhost:" + port + "/login"
            def login = new LoginDTO(EMAIL, "wrong" + PASSWORD)
        when:
            def response = restTemplate.postForEntity(url, login, Void.class)
        then:
            response.statusCode == HttpStatus.UNAUTHORIZED
    }

    def "Test retrieving test user locations, should return empty list"() {

        given:
            def url = "http://localhost:" + port + "/user/locations"
            headers.set(HttpHeaders.AUTHORIZATION, UID.toString())

        when:
            def response = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), List)

        then:
            response.statusCode == HttpStatus.OK
            response.getBody().size() == 0
    }

    def "Test adding location"() {

        given:
            def url = "http://localhost:" + port + "/location"
            def location = new Location(UID, "testLocation", "testAddress")
            def request = new HttpEntity<>(objectMapper.writeValueAsString(location), headers)
            headers.set(HttpHeaders.AUTHORIZATION, UID.toString())
        when:
            def response = restTemplate.exchange(url, HttpMethod.POST, request, Location)
            if (response.getBody() != null) {
                LID = response.getBody().getLid()
            }
        then:
            response.statusCode == HttpStatus.CREATED
            response.getBody().getLid() != null
    }

    def "Test retrieving added location"() {

        given:
            def url = "http://localhost:" + port + "/location/" + LID
            headers.add(HttpHeaders.AUTHORIZATION, UID.toString())
            def location = new Location(LID, UID, "testLocation", "testAddress")
        when:
            def response = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), Location)

        then:
            response.statusCode == HttpStatus.OK
            response.getBody() == location
    }

    def "Test retrieving test user locations, should return one location"() {

        given:
            def url = "http://localhost:" + port + "/user/locations"
            headers.set(HttpHeaders.AUTHORIZATION, UID.toString())

        when:
            def response = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), List)

        then:
            response.statusCode == HttpStatus.OK
            response.getBody().size() == 1
            response.getBody().get(0).id == LID
            response.getBody().get(0).uid == UID
            response.getBody().get(0).name == "testLocation"
            response.getBody().get(0).address == "testAddress"
            response.getBody().get(0).permissions == []
    }

    def "Test retrieving user locations with empty UID"() {

        given:
            def url = "http://localhost:" + port + "/user/locations"
            headers.remove(HttpHeaders.AUTHORIZATION)
        when:
            def response = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), String)

        then:
            response.statusCode == HttpStatus.BAD_REQUEST
            response.getBody() == "authorization token is needed"
    }

    def "cleanupSpecCustom"() {

        given:
            def urlUser = "http://localhost:" + port + "/user/" + UID
            def urlLocation = "http://localhost:" + port + "/location/" + LID
            def respUser, respLocation
        when:
            respUser = restTemplate.exchange(urlUser, HttpMethod.DELETE, new HttpEntity<>(), String)
            respLocation = restTemplate.exchange(urlLocation, HttpMethod.DELETE, new HttpEntity<>(), String)
        then:
            respUser.statusCode == HttpStatus.OK
            respLocation.statusCode == HttpStatus.OK
    }
}
