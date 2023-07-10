package location.integration

import com.example.location.LocationApplication
import com.example.location.dto.LoginDTO
import com.example.location.entities.Location
import com.example.location.entities.User
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import spock.lang.Shared
import spock.lang.Specification

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest(classes = LocationApplication.class)
@AutoConfigureMockMvc
class MainControllerTest extends Specification {

    @Autowired
    private MockMvc mockMvc

    @Autowired
    private ObjectMapper objectMapper

    @Autowired
    private JdbcTemplate jdbcTemplate

    @Shared
    private static final String EMAIL = "integrationtest14253455@gmail.com"
    @Shared
    private static final String PASSWORD = "password"
    @Shared
    private long UID
    @Shared
    private long LID

    def "Test registering a user"() {

        given:
            def user = new User(null, "User", "Test", EMAIL, PASSWORD)

            def request = mockMvc.perform(MockMvcRequestBuilders
                .post("/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user)))
                .andExpect(request().asyncStarted())
                .andReturn()

        when:
            def response = mockMvc.perform(MockMvcRequestBuilders.asyncDispatch(request)).andExpect(status().isOk()).andReturn().response
            def registeredUser = objectMapper.readValue(response.contentAsString, User.class)

        then:
            registeredUser != null
            registeredUser.uid != null
            registeredUser.email == EMAIL
            registeredUser.firstName == user.firstName
            registeredUser.lastName == user.lastName
    }

    def "Testing user login"() {

        given:
            def login = new LoginDTO(EMAIL, PASSWORD)
            def request = mockMvc.perform(MockMvcRequestBuilders.post("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(login)))
                .andExpect(request().asyncStarted())
                .andReturn()

        when:
            def response = mockMvc.perform(MockMvcRequestBuilders.asyncDispatch(request)).andExpect(status().isOk()).andReturn().response
            def authorizationHeader = response.getHeader("Authorization")
            UID = authorizationHeader.toLong()

        then:
            authorizationHeader != null
    }

    def "Test user login with incorrect password"() {

        given:
            def login = new LoginDTO(EMAIL, "wrong" + PASSWORD)
            def request = mockMvc.perform(MockMvcRequestBuilders.post("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(login)))
                .andExpect(request().asyncStarted())
                .andReturn()

        expect:
            def response = mockMvc.perform(MockMvcRequestBuilders.asyncDispatch(request)).andExpect(status().isUnauthorized()).andReturn().response
    }

    def "Test retrieving test user locations, should return empty list"() {

        given:
            def request = mockMvc.perform(MockMvcRequestBuilders.get("/user/locations")
                .header("Authorization", UID.toString()))
                .andExpect(request().asyncStarted())
                .andReturn()

        when:
            def response = mockMvc.perform(MockMvcRequestBuilders.asyncDispatch(request)).andExpect(status().isOk()).andReturn().response

        then:
            response.contentAsString != null
            response.contentAsString == '[]'
    }

    def "Test adding location"() {

        given:
            def location = new Location(UID, "testLocation", "testAddress")
            def request = mockMvc.perform(MockMvcRequestBuilders.post("/location")
                .header("Authorization", UID.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(location)))
                .andExpect(request().asyncStarted())
                .andReturn()

        when:
            def response = mockMvc.perform(MockMvcRequestBuilders.asyncDispatch(request)).andExpect(status().isCreated()).andReturn().response
            LID = objectMapper.readValue(response.contentAsString, Location).lid

        then:
            response.contentAsString != null
            response.contentAsString.contains(LID.toString())
    }

    def "Test retrieving added location"() {

        given:
            def request = mockMvc.perform(MockMvcRequestBuilders.get("/location/$LID")
                .header("Authorization", UID.toString()))
                .andExpect(request().asyncStarted())
                .andReturn()

        when:
            def response = mockMvc.perform(MockMvcRequestBuilders.asyncDispatch(request)).andExpect(status().isOk()).andReturn().response

        then:
            response.contentAsString != null
            response.contentAsString.contains(LID.toString())
    }

    def "Test retrieving test user locations, should return one location"() {

        given:
            def request = mockMvc.perform(MockMvcRequestBuilders.get("/user/locations")
                .header("Authorization", UID.toString()))
                .andExpect(request().asyncStarted())
                .andReturn()

        when:
            def response = mockMvc.perform(MockMvcRequestBuilders.asyncDispatch(request)).andExpect(status().isOk()).andReturn().response

        then:
            response.contentAsString != null
            response.contentAsString.contains(LID.toString())
    }

    def "Test retrieving user locations with empty UID"() {

        given:
            def request = mockMvc.perform(MockMvcRequestBuilders.get("/user/locations"))

        when:
            def response = request.andExpect(status().isBadRequest()).andReturn().response

        then:
            response.contentAsString != null
            response.contentAsString == "Authorization header is missing"
    }

    def "cleanupSpecCustom"() {

        def update = jdbcTemplate.update("DELETE FROM users WHERE uid = ?", UID)
        print("Deleted " + update + " users")
        expect:
            update == 1
    }
}
