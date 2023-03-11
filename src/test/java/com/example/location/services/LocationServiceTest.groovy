package com.example.location.services

import com.example.location.dto.LocationDTO
import com.example.location.dto.SharedLocation
import com.example.location.entities.Access
import com.example.location.entities.Location
import com.example.location.entities.User
import com.example.location.repositories.AccessRepository
import com.example.location.repositories.LocationRepository
import com.example.location.repositories.UserRepository
import com.example.location.services.LocationService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import spock.lang.Specification

import java.util.Arrays
import java.util.List
import java.util.Optional

class LocationServiceTest extends Specification {

    LocationRepository locationRepository = Mock()
    AccessRepository accessRepository = Mock()
    UserRepository userRepository = Mock()
    LocationService locationService = new LocationService(locationRepository, accessRepository, userRepository)

    def setup() {

    }

    def "test findUserLocations method"() {
        given:
        def uid = 1
        def location1 = new Location(uid: uid, name: "Location 1", address: "Address 1")
        def location2 = new Location(uid: uid, name: "Location 2", address: "Address 2")
        def access1 =  new Access(uid: uid, lid: location1.lid, type: "read-only")
        def access2 = new Access(uid: uid, lid: location1.lid, type: "admin")
        def access3 = new Access(uid: uid, lid: location2.lid, type: "read-only")
        locationRepository.findAllByUid(uid) >> [location1, location2]
        accessRepository.findAllByLid(location1.lid) >> [access1, access2]
        accessRepository.findAllByLid(location2.lid) >> [access3]

        when:
        def result = locationService.findUserLocations("1")

        then:
        result.size() == 2
        result[0].name == location1.name
        result[0].address == location1.address
        result[0].permissions.size() == 2
        result[1].name == location2.name
        result[1].address == location2.address
    }


    def "test saveLocation method with invalid uid"() {
        given:
        def uid = "invalid"
        def name = "Location 1"
        def address = "Address 1"


        when:
        def result = locationService.saveLocation(uid, name, address)

        then:
        result == null
    }

    def "test findById method"() {
        given:
        def lid = 1L
        def location = new Location(lid: lid, name: "Location 1", address: "Address 1")
        locationRepository.findById(lid) >> Optional.of(location)

        when:
        def result = locationService.findById(lid)

        then:
        result.isPresent() == true
        result.get().lid == lid
        result.get().name == location.name
        result.get().address == location.address
    }
}
