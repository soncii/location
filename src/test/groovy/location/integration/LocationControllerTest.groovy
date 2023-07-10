package location.integration

import com.example.location.LocationApplication
import com.example.location.dto.AccessDTO
import com.example.location.dto.SharedLocation
import com.example.location.dto.UserLocationDTO
import com.example.location.entities.Access
import com.example.location.entities.Location
import com.example.location.entities.User
import com.example.location.services.LocationService
import com.example.location.services.UserService
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.MvcResult
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import spock.lang.Shared
import spock.lang.Specification

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    classes = [LocationApplication.class])
@AutoConfigureMockMvc
@EnableAutoConfiguration
class LocationControllerTest extends Specification {

    @Autowired
    private MockMvc mockMvc
    @Autowired
    private UserService userService

    @Autowired
    JdbcTemplate jdbcTemplate
    @Autowired
    private LocationService locationService

    @Autowired
    private ObjectMapper objectMapper

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

    def "setupSpecCustom"() {

        given:
            def user1 = new User(null, "Owner", "Test", OWNER, PASSWORD)
            def user2 = new User(null, "Guest", "Test", GUEST, PASSWORD)

        when:
            user1 = userService.insertUser(user1).join()
            user2 = userService.insertUser(user2).join()

            UidOwner = user1.uid
            UidGuest = user2.uid

            def location = new Location(null, UidOwner, "Test Location", "Test Address")

            location = locationService.saveLocation(location).join()

            LID = location.lid
        then:
            UidOwner != null
            UidGuest != null
            LID != null
    }

    def "Test Share Location"() {

        given:
            def access = new AccessDTO(LID, GUEST, "read-only")
            MvcResult request = mockMvc.perform(MockMvcRequestBuilders.post("/location/share")
                .header("Authorization", UidOwner.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(access)))
                .andExpect(request().asyncStarted())
                .andReturn()

        when:
            def result = mockMvc.perform(MockMvcRequestBuilders.asyncDispatch(request))
                .andExpect(status().isCreated())
                .andReturn()
                .response

            def accessSaved = objectMapper.readValue(result.contentAsString, Access.class)

        then:
            accessSaved != null
            accessSaved.aid != null
            accessSaved.lid == LID
            accessSaved.uid == UidGuest
            accessSaved.type == "read-only"
    }

    def "Test Get All Shared location for guest"() {

        given:
            MvcResult request = mockMvc.perform(MockMvcRequestBuilders.get("/location/all")
                .header("Authorization", UidGuest.toString()))
                .andExpect(request().asyncStarted())
                .andReturn()

        when:
            def response = mockMvc.perform(MockMvcRequestBuilders.asyncDispatch(request)).andExpect(status().isOk()).andReturn().response
            def sharedLocations = objectMapper.readValue(response.contentAsString, List)

        then:
            sharedLocations.size() == 1
            SharedLocation sharedLocation = sharedLocations[0]
            sharedLocation.lid == LID
            sharedLocation.accessType == "read-only"
    }

    def "Test Change Share mode"() {

        given:
            def dto = new UserLocationDTO(GUEST, LID)
            MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/location/access")
                .header("Authorization", UidOwner.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(request().asyncStarted())
                .andReturn()
        expect:
            mockMvc.perform(MockMvcRequestBuilders.asyncDispatch(result))
                .andExpect(status().isOk())
    }

    def "Test Get All Shared location After Change for guest"() {

        given:
            MvcResult request = mockMvc.perform(MockMvcRequestBuilders.get("/location/all")
                .header("Authorization", UidGuest.toString()))
                .andExpect(request().asyncStarted())
                .andReturn()

        when:
            def response = mockMvc.perform(MockMvcRequestBuilders.asyncDispatch(request)).andExpect(status().isOk()).andReturn().response
            def sharedLocations = objectMapper.readValue(response.contentAsString, List)

        then:
            sharedLocations.size() == 1
            SharedLocation sharedLocation = sharedLocations[0]
            sharedLocation.lid == LID
            sharedLocation.accessType == "admin"
    }

    def "Test Unfriend"() {

        given:
            def dto = new UserLocationDTO(GUEST, LID)
            MvcResult request = mockMvc.perform(MockMvcRequestBuilders.post("/location/unfriend")
                .header("Authorization", UidOwner.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(request().asyncStarted())
                .andReturn()
        expect:
            mockMvc.perform(MockMvcRequestBuilders.asyncDispatch(request)).andExpect(status().isOk())
    }

    def "Test Get All Shared location After Unfriending for guest"() {

        given:
            MvcResult request = mockMvc.perform(MockMvcRequestBuilders.get("/location/all")
                .header("Authorization", UidGuest.toString()))
                .andExpect(request().asyncStarted())
                .andReturn()

        when:
            def response = mockMvc.perform(MockMvcRequestBuilders.asyncDispatch(request)).andExpect(status().isOk()).andReturn().response
            def sharedLocations = objectMapper.readValue(response.contentAsString, List)

        then:
            sharedLocations.size() == 0
    }

    def "cleanupSpecCustom"() {

        given:
            def owner = jdbcTemplate.update("DELETE FROM users WHERE uid = ?", UidOwner)
            def guest = jdbcTemplate.update("DELETE FROM users WHERE uid = ?", UidGuest)
        expect:
            owner == 1
            guest == 1
    }
}
