package location.serv

import com.example.location.dto.LocationDTO
import com.example.location.dto.SharedLocation
import com.example.location.entities.Location
import com.example.location.entities.User
import com.example.location.repositories.AccessRepository
import com.example.location.repositories.LocationRepository
import com.example.location.repositories.UserRepository
import com.example.location.services.LocationServiceImpl
import spock.lang.Shared
import spock.lang.Specification

import java.util.ArrayList
import java.util.List
import java.util.Optional
import java.util.concurrent.CompletableFuture

class LocationServiceImplTest extends Specification {

    LocationRepository locationRepository = Stub(LocationRepository)

    AccessRepository accessRepository = Stub(AccessRepository)

    UserRepository userRepository = Stub(UserRepository)

    LocationServiceImpl locationService = new LocationServiceImpl(locationRepository, accessRepository, userRepository)

    // Other test cases...

    def "findUserLocations should return a list of location DTOs for a given user"() {
        given:
        def uidString = "1"
        def uid = 1L
        def location1 = new Location(uid: uid, name: "Location 1", address: "Address 1")
        def location2 = new Location(uid: uid, name: "Location 2", address: "Address 2")
        def locations = [location1, location2]
        locationRepository.findAllByUid(uid) >> locations
        accessRepository.findAllByLid(location1.lid) >> []
        accessRepository.findAllByLid(location2.lid) >> []

        when:
        def result = locationService.findUserLocations(uidString)

        then:
        result.get().equals([
                new LocationDTO(location1, []),
                new LocationDTO(location2, [])
        ])

    }

    def "findUserLocations should return an empty list when no locations are found"() {
        given:
        def uidString = "1"
        def uid = 1L
        locationRepository.findAllByUid(uid) >> null

        when:
        def result = locationService.findUserLocations(uidString)

        then:

        result.get() == []

    }

    def "saveLocation should return the saved location when valid parameters are provided"() {
        given:
        def uid = "1"
        def name = "Location 1"
        def address = "Address 1"
        def user = new User(uid: 1L)
        userRepository.findById(1L) >> Optional.of(user)
        locationRepository.save(_ as Location) >> { Location location -> location }

        when:
        def result = locationService.saveLocation(uid, name, address).join()

        then:

        result == new Location(uid: 1L, name: name, address: address)

    }

    def "saveLocation should return null when the UID is not a valid number"() {
        given:
        def uid = "invalid"
        def name = "Location 1"
        def address = "Address 1"

        when:
        def result = locationService.saveLocation(uid, name, address)

        then:

            result.get() == null

    }

    def "saveLocation should return null when the user with the given UID does not exist"() {
        given:
        def uid = "1"
        def name = "Location 1"
        def address = "Address 1"
        userRepository.findById(uid as Long) >> Optional.empty()

        when:
        def result = locationService.saveLocation(uid, name, address)

        then:

            result.get() == null

    }

    def "findById should return the location with the given ID"() {
        given:
        def lid = 1L
        def location = new Location(lid: lid, name: "Location 1", address: "Address 1")
        locationRepository.findById(lid) >> Optional.of(location)

        when:
        def result = locationService.findById(lid)

        then:

            result.get() == Optional.of(location)

    }

    def "findAllLocations should return a list of shared locations for a given user"() {
        given:
        def uid = "1"
        def uidL = 1L
        def user = new User(uid: 1L, email: "user@example.com")
        def location1 = new Location(uid: uid as Long, name: "Location 1", address: "Address 1")
        def location2 = new Location(uid: uid as Long, name: "Location 2", address: "Address 2")
        def allSharedLocations = [
                new SharedLocation(location1, user.email),
                new SharedLocation(location2, user.email)
        ]
        userRepository.findById(uidL) >> Optional.of(user)
        locationRepository.findAllSharedLocation(uidL as Long) >> allSharedLocations
        locationRepository.findAllByUid(uidL as Long) >> [location1, location2]

        when:
        def result = locationService.findAllLocations(uid).join()

        then:
        result == allSharedLocations

    }

    def "findAllLocations should return null when the UID is 'empty'"() {
        given:
        def uid = "empty"

        when:
        def result = locationService.findAllLocations(uid)

        then:

            result.get() == null

    }
}
