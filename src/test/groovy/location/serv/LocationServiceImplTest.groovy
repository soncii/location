package location.serv

import com.example.location.component.HistoryEventPublisher
import com.example.location.dto.LocationDTO
import com.example.location.dto.SharedLocation
import com.example.location.entities.Location
import com.example.location.entities.User
import com.example.location.repositories.AccessRepository
import com.example.location.repositories.LocationRepository
import com.example.location.repositories.UserRepository
import com.example.location.services.LocationServiceImpl
import com.example.location.util.NotFoundException
import spock.lang.Specification

import java.util.concurrent.CompletableFuture

class LocationServiceImplTest extends Specification {

    LocationRepository locationRepository = Stub(LocationRepository)

    AccessRepository accessRepository = Stub(AccessRepository)

    UserRepository userRepository = Stub(UserRepository)

    HistoryEventPublisher historyEventPublisher = Mock(HistoryEventPublisher)

    LocationServiceImpl locationService = new LocationServiceImpl(locationRepository, accessRepository, userRepository, historyEventPublisher)

    def "findUserLocations should return a list of location DTOs for a given user"() {

        given:
            def uidString = "1"
            def uid = 1L
            def location1 = new Location(uid: uid, name: "Location 1", address: "Address 1")
            def location2 = new Location(uid: uid, name: "Location 2", address: "Address 2")
            def locations = [location1, location2]
            locationRepository.findAllByUid(uid) >> CompletableFuture.completedFuture(locations)
            accessRepository.findAllByLid(location1.lid) >> CompletableFuture.completedFuture([])
            accessRepository.findAllByLid(location2.lid) >> CompletableFuture.completedFuture([])

        when:
            def result = locationService.findUserLocations(uidString).join()

        then:
            result == [new LocationDTO(location1, []),
                       new LocationDTO(location2, [])]
    }

    def "findUserLocations should return an empty list when no locations are found"() {

        given:
            def uidString = "1"
            def uid = 1L
            locationRepository.findAllByUid(uid) >> CompletableFuture.completedFuture(new ArrayList<LocationDTO>())

        when:
            def result = locationService.findUserLocations(uidString).join()

        then:

            result == []
    }

    def "saveLocation should return the saved location when valid parameters are provided"() {

        given:
            def uid = 1L
            def name = "Location 1"
            def address = "Address 1"
            def location = new Location(uid, name, address)
            def user = new User(uid: uid)
            userRepository.findById(1L) >> CompletableFuture.completedFuture(Optional.of(user))
            locationRepository.save(_ as Location) >> { Location saved -> CompletableFuture.completedFuture(saved) }

        when:
            def result = locationService.saveLocation(location).join()

        then:
            1 * historyEventPublisher.publishHistoryCreatedEvent(uid, "LOCATION", location)
            result == new Location(uid: 1L, name: name, address: address)
    }

    def "saveLocation should return null when the user with the given UID does not exist"() {

        given:
            def uid = 1l
            def name = "Location 1"
            def address = "Address 1"
            userRepository.findById(uid) >> CompletableFuture.completedFuture(Optional.empty())
            def location = new Location(uid, name, address)
        when:
            def result = locationService.saveLocation(location).exceptionally({ throwable ->

                if (throwable.cause instanceof NotFoundException) {
                    return null
                } else {
                    throw throwable
                }
            }).join()

        then:
            result == null
            0 * historyEventPublisher.publishHistoryCreatedEvent(_)
    }

    def "findById should return the location with the given ID"() {

        given:
            def lid = 1L
            def location = new Location(lid: lid, name: "Location 1", address: "Address 1")
            locationRepository.findById(lid) >> CompletableFuture.completedFuture(Optional.of(location))

        when:
            def result = locationService.findById(lid).join()

        then:

            result == Optional.of(location)
    }

    def "findAllLocations should return a list of shared locations for a given user"() {

        given:
            def uid = "1"
            def uidL = 1L
            def user = new User(uid: 1L, email: "user@example.com")
            def location1 = new Location(uid: uid as Long, name: "Location 1", address: "Address 1")
            def location2 = new Location(uid: uid as Long, name: "Location 2", address: "Address 2")
            def allSharedLocations = [new SharedLocation(location1, user.email),
                                      new SharedLocation(location2, user.email)]
            userRepository.findById(uidL) >> CompletableFuture.completedFuture(Optional.of(user))
            locationRepository.findAllSharedLocation(uidL) >> CompletableFuture.completedFuture(allSharedLocations)
            locationRepository.findAllByUid(uidL) >> CompletableFuture.completedFuture([location1, location2])

        when:
            def result = locationService.findAllLocations(uid).join()

        then:
            result == allSharedLocations
    }

    def "deleteById should return true when location with ID '#lid' exists"() {

        given:
            def locationId = 1L
            def location = new Location(uid: 1L, name: "Location 1", address: "Address 1")
            locationRepository.findById(locationId) >> CompletableFuture.completedFuture(Optional.of(location))
            locationRepository.deleteById(locationId) >> CompletableFuture.completedFuture(Boolean.TRUE)

        when:
            def result = locationService.deleteById(locationId).join()

        then:
            result == true
            1 * historyEventPublisher.publishHistoryDeletedEvent(1L, "LOCATION", location)
    }

    def "deleteById should throw NotFoundException when location with ID '#lid' does not exist"() {

        given:
            def locationId = 1L
            locationRepository.findById(locationId) >> CompletableFuture.completedFuture(Optional.empty())

        when:
            def result = locationService.deleteById(locationId).handle({ _, ex ->
                if (ex.cause instanceof NotFoundException) {
                    return null
                } else {
                    throw ex
                }
            }).join()

        then:
            result == null
            0 * historyEventPublisher.publishHistoryDeletedEvent(_)
    }
}


