package location.integration

import com.example.location.LocationApplication
import com.example.location.dto.AccessDTO
import com.example.location.dto.LoginDTO
import com.example.location.dto.SharedLocation
import com.example.location.dto.UserLocationDTO
import com.example.location.entities.Access
import com.example.location.entities.Location
import com.example.location.entities.User
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import spock.lang.Shared
import spock.lang.Specification

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    classes = [LocationApplication.class])
@EnableAutoConfiguration
class ShareLocationTest extends Specification {

    private TestRestTemplate restTemplate = new TestRestTemplate()
    ObjectMapper objectMapper = new ObjectMapper();
    private HttpHeaders headers = new HttpHeaders()
    @Value('${local.server.port}')
    private int port
    @Shared
    private static long UidOwner
    @Shared
    private static long UidGuest
    @Shared
    private long LID

    private static final String OWNER = "owner1234@example.com"
    private static final String GUEST = "guest1234@example.com"

    private static final String PASSWORD = "password"

    def setup() {

        headers.setContentType(MediaType.APPLICATION_JSON)
    }

    def "setupSpecCustom"() {

        given:
            def url = "http://localhost:" + port
            headers.setContentType(MediaType.APPLICATION_JSON)
            def user1 = new User(null, "Owner", "Test", OWNER, PASSWORD)
            def user2 = new User(null, "Guest", "Test", GUEST, PASSWORD)

            restTemplate.postForEntity(url + "/register", new HttpEntity<>(objectMapper.writeValueAsString(user1), headers), Void)
            restTemplate.postForEntity(url + "/register", new HttpEntity<>(objectMapper.writeValueAsString(user2), headers), Void)
            def response1 = restTemplate.postForEntity(url + "/login",
                new HttpEntity<>(objectMapper.writeValueAsString(new LoginDTO(OWNER, PASSWORD)), headers), Void.class)
            def response2 = restTemplate.postForEntity(url + "/login",
                new HttpEntity<>(objectMapper.writeValueAsString(new LoginDTO(GUEST, PASSWORD)), headers), Void.class)

            UidOwner = response1.getHeaders().get("Authorization").get(0).toLong()
            UidGuest = response2.getHeaders().get("Authorization").get(0).toLong()
            headers.set("Authorization", UidOwner.toString())
            def location = new Location(null, UidOwner, "Test Location", "Test Address")
            def responseLocation = restTemplate.postForEntity("http://localhost:" + port + "/location", new HttpEntity<>(objectMapper.writeValueAsString(location), headers), Location)
            LID = responseLocation.getBody().getLid()

    }

    def "Test Share Location"() {

        given:
            def url = "http://localhost:" + port + "/location/share"
            def Access = new AccessDTO(LID, GUEST, "read-only")
            def request = new HttpEntity<>(objectMapper.writeValueAsString(Access), headers)
        when:
            def response = restTemplate.postForEntity(url, request, com.example.location.entities.Access)
        then:
            response.getStatusCode() == HttpStatus.CREATED
            response.getBody().getAid() != null
            response.getBody().getLid() == LID
            response.getBody().getUid() == UidGuest
            response.getBody().getType() == "read-only"
    }

    def "Test Get All Shared location for guest"() {

        given:
            def url = "http://localhost:" + port + "/location/all"
            headers.set("Authorization", UidGuest.toString())
            def request = new HttpEntity<>(null, headers)
        when:
            def response = restTemplate.exchange(url, HttpMethod.GET, request, List)
            SharedLocation sharedLocations = (SharedLocation) response.getBody().get(0);
        then:
            response.getStatusCode() == HttpStatus.OK
            response.getBody().size() == 1
            sharedLocations.getLid() == LID
            sharedLocations.getAccessType() == "read-only"
    }

    def "Test Change Share mode"() {

        given:
            def url = "http://localhost:" + port + "/location/access"
            def dto = new UserLocationDTO(GUEST, LID)
            headers.set("Authorization", UidOwner.toString())
            def request = new HttpEntity<>(objectMapper.writeValueAsString(dto), headers)
        when:
            def response = restTemplate.exchange(url, HttpMethod.POST, request, String)
        then:
            response.getStatusCode() == HttpStatus.OK
    }

    def "Test Get All Shared location After Change for guest"() {

        given:
            def url = "http://localhost:" + port + "/location/all"
            headers.set("Authorization", UidGuest.toString())
            def request = new HttpEntity<>(null, headers)
        when:
            def response = restTemplate.exchange(url, HttpMethod.GET, request, List)
            SharedLocation sharedLocations = (SharedLocation) response.getBody().get(0);
        then:
            response.getStatusCode() == HttpStatus.OK
            response.getBody().size() == 1
            sharedLocations.getLid() == LID
            sharedLocations.getAccessType() == "admin"
    }

    def "Test Unfriend"() {

        given:
            def baseUrl = "http://localhost:" + port
            def dto = new UserLocationDTO(GUEST, LID)
            headers.set("Authorization", UidOwner.toString())
            def request = new HttpEntity<>(objectMapper.writeValueAsString(dto), headers)
        when:
            def response = restTemplate.exchange(baseUrl + "/location/unfriend", HttpMethod.POST, request, Void)
        then:
            response.getStatusCode() == HttpStatus.OK
    }

    def "Test Get All Shared location After Unfriending for guest"() {

        given:
            def url = "http://localhost:" + port + "/location/all"
            headers.set("Authorization", UidGuest.toString())
            def request = new HttpEntity<>(null, headers)
        when:
            def response = restTemplate.exchange(url, HttpMethod.GET, request, List)
        then:
            response.getStatusCode() == HttpStatus.OK
            response.getBody().size() == 0
    }

    def "cleanupSpecCustom"() {

        given:
            def baseUrl = "http://localhost:" + port;
            def urlOwner = baseUrl + "/user/" + UidOwner
            def urlGuest = baseUrl + "/user/" + UidGuest
            def urlLocation = baseUrl + "/location/" + LID
            def respOwner, respGuest, respLocation
        when:
            respOwner = restTemplate.exchange(urlOwner, HttpMethod.DELETE, new HttpEntity<>(), Void)
            respGuest = restTemplate.exchange(urlGuest, HttpMethod.DELETE, new HttpEntity<>(), Void)
            respLocation = restTemplate.exchange(urlLocation, HttpMethod.DELETE, new HttpEntity<>(), Void)
        then:
            respOwner.statusCode == HttpStatus.OK
            respGuest.statusCode == HttpStatus.OK
            respLocation.statusCode == HttpStatus.OK
    }
}
