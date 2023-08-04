package location.integration

import com.example.location.LocationApplication
import com.example.location.component.HistoryEventPublisher
import com.example.location.dto.AccessDTO
import com.example.location.dto.SharedLocation
import com.example.location.dto.UserLocationDTO
import com.example.location.entities.Access
import com.example.location.entities.Location
import com.example.location.entities.User
import com.example.location.services.HistoryService
import com.example.location.services.LocationService
import com.example.location.services.UserService
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.support.GeneratedKeyHolder
import org.springframework.jdbc.support.KeyHolder
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.MvcResult
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import spock.lang.Shared
import spock.lang.Specification

import java.sql.PreparedStatement
import java.sql.Statement

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
    private HistoryService historyService

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

    def addUser(User user) {
        String sql = "INSERT INTO users (firstname, lastname, email, password) VALUES (?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update({ con ->
            PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)
            ps.setString(1, user.getFirstName())
            ps.setString(2, user.getLastName())
            ps.setString(3, user.getEmail())
            ps.setString(4, user.getPassword())
            return ps
        }, keyHolder)
        return keyHolder.getKey().longValue()
    }

    def addLocation(Location location) {
        String sql = "INSERT INTO location (uid, name, address) VALUES (?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update({ con ->
            PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)
            ps.setLong(1, location.getUid())
            ps.setString(2, location.getName())
            ps.setString(3, location.getAddress())
            return ps
        }, keyHolder)
        return keyHolder.getKey().longValue()
    }

    def addShare(Access access) {
        String sql = "INSERT INTO access (uid, lid, type) VALUES (?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update({ con ->
            PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)
            ps.setLong(1, access.getUid())
            ps.setLong(2, access.getLid())
            ps.setString(3, access.getType())
            return ps
        }, keyHolder)
        return keyHolder.getKey().longValue()
    }

    def setup() {

        def owner = new User(null, "Owner", "Test", OWNER, PASSWORD)
        def guest = new User(null, "Guest", "Test", GUEST, PASSWORD)

        UidOwner = addUser(owner)
        UidGuest = addUser(guest)

        def location = new Location(null, UidOwner, "Test Location", "Test Address")

        LID = addLocation(location)
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

    def "Test Share Location with invalid type"() {
        given:
            def access = new AccessDTO(LID, GUEST, "invalid")

        expect:
            mockMvc.perform(MockMvcRequestBuilders.post("/location/share")
                .header("Authorization", UidOwner.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(access)))
                .andExpect(status().isBadRequest())


    }

    def "Test Get All Shared location for guest should return 0"() {

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

    def "Test Get All Shared location for guest should return 1"() {

        given:
            def access = new Access(null,  UidGuest,LID, "read-only")
            addShare(access)
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
            def access = new Access(null,  UidGuest, LID,"read-only")
            addShare(access)
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


    def "Test Unfriend when owner"() {

        given:
            def access = new Access(null,  UidGuest, LID,"read-only")
            addShare(access)
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

    def "Test Unfriend when admin right"() {

        given:
            def access = new Access(null,  UidGuest, LID,"admin")
            addShare(access)
            def UIDGuest2 = addUser(new User(null, "Guest2", "Test", "guest3@mail.com", PASSWORD))
            addShare(new Access(null,  UIDGuest2, LID,"read-only"))
            def dto = new UserLocationDTO("guest3@mail.com", LID)

            MvcResult request = mockMvc.perform(MockMvcRequestBuilders.post("/location/unfriend")
                .header("Authorization", UidGuest.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(request().asyncStarted())
                .andReturn()
        expect:
            mockMvc.perform(MockMvcRequestBuilders.asyncDispatch(request)).andExpect(status().isOk())
        cleanup:
            jdbcTemplate.update("DELETE FROM users WHERE uid = ?", UIDGuest2)
    }


    def "Test Unfriend when no right"() {

            given:
                def access = new Access(null,  UidGuest, LID,"read-only")
                addShare(access)
                def UIDGuest2 = addUser(new User(null, "Guest2", "Test", "guest3@mail.com", PASSWORD))
                addShare(new Access(null,  UIDGuest2, LID,"read-only"))
                def dto = new UserLocationDTO("guest3@mail.com", LID)
                MvcResult request = mockMvc.perform(MockMvcRequestBuilders.post("/location/unfriend")
                    .header("Authorization", UidGuest.toString())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(dto)))
                        .andExpect(request().asyncStarted())
                        .andReturn()

            expect:
            mockMvc.perform(MockMvcRequestBuilders.asyncDispatch(request)).andExpect(status().isForbidden())
            cleanup:
                jdbcTemplate.update("DELETE FROM users WHERE uid = ?", UIDGuest2)
    }

    def cleanup() {

        jdbcTemplate.update("DELETE FROM users WHERE uid = ?", UidOwner)
        jdbcTemplate.update("DELETE FROM users WHERE uid = ?", UidGuest)
    }
}
