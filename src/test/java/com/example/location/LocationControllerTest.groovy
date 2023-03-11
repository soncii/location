package com.example.location.controllers

import com.example.location.dto.SharedLocation
import com.example.location.entities.Access
import com.example.location.entities.Location
import com.example.location.entities.User
import com.example.location.services.AccessService
import com.example.location.services.LocationService
import com.example.location.services.UserService
import org.junit.jupiter.params.provider.EnumSource
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.CookieValue
import spock.lang.Specification
import spock.lang.Subject
import spock.lang.Unroll
import spock.mock.DetachedMockFactory


import java.util.Collections
import java.util.Optional

class LocationControllerTest extends Specification {

    LocationService locationService = Mock()

    AccessService accessService = Mock()

    UserService userService = Mock()

    @Subject
    LocationController locationController = new LocationController(
            locationService: locationService,
            accessService: accessService,
            userService: userService
    )

    def "addLocation should return newLocation view if the user is logged in"() {
        given:
        String uid = "1"
        String expectedView = "newLocation"

        when:
        String result = locationController.addLocation(uid)

        then:
        result == expectedView
    }

    def "addLocation should redirect to login page if the user is not logged in"() {
        given:
        String uid = "empty"
        String expectedView = "redirect:/login"

        when:
        String result = locationController.addLocation(uid)

        then:
        result == expectedView
    }

    def "allLocations should add locations to the model and return sharedLocations view"() {
        given:
        String uid = "1"
        def location = new SharedLocation()
        List<SharedLocation> locations = [location]
        Model model = Mock(Model.class)
        String expectedView = "sharedLocations"

        locationService.findAllLocations(uid) >> locations

        when:
        String result = locationController.allLocations(uid, model)

        then:
        result == expectedView
        1 * model.addAttribute("locations",locations)
    }

    def "saveLocation should save the location and redirect to the homepage"() {
        given:
        String uid = "1"
        String name = "Location1"
        String address = "123 Main St"
        Location location = new Location(lid: 1)
        String expectedView = "redirect:/"

        locationService.saveLocation(uid, name, address) >> location

        when:
        String result = locationController.saveLocation(uid, name, address)

        then:
        result == expectedView
        location.getLid() == 1
    }

    @Unroll
    def "saveShare should save access for #shareMode and redirect to homepage"() {
        given:
        String email = "test@example.com"
        String shareMode = "admin"
        Long lid = 1
        String uid = "1"
        String expectedView = "redirect:/"

        when:
        String result = locationController.saveShare(email, shareMode, lid, uid)

        then:
        result == expectedView
        1 * accessService.saveAccess(email, shareMode, lid)
    }
    def "getLocation returns error when user is not authorized"() {
        given:
        def lid = 123
        def uid = "abc"
        def model = Mock(Model)

        userService.authorizeOwner(uid, lid) >> false

        when:
        def result = locationController.getLocation(lid, model, uid)

        then:
        result == "error"
        1 * userService.authorizeOwner(uid, lid)
        0 * locationService.findById(lid)
        0 * model.addAttribute(_, _)
        0 * accessService.getUsersOnLocation(lid)
    }


    def "unfriend returns FORBIDDEN when user is not authorized"() {
        given:
        def lid = 123
        def email = "alice@example.com"
        def uidString = "abc"

        userService.authorizeOwner(uidString, lid) >> false

        when:
        def result = locationController.unfriend(lid, email, uidString)

        then:
        result.status == HttpStatus.FORBIDDEN
        result.body == "You are not authorized to perform this action."
        1 * userService.authorizeOwner(uidString, lid)
        0 * accessService.delete(_, _, _)
    }
    def "should return 403 forbidden when user is not authorized to change mode"() {
        given:
        def lid = 123L
        def email = "john@example.com"
        def uidString = "not-an-owner"
        userService.authorizeOwner(uidString, lid) >> false

        when:
        def response = locationController.changeMode(lid, email, uidString)

        then:
        response.statusCode == HttpStatus.FORBIDDEN
        response.body == "You are not authorized to perform this action."
    }

    def "should return 302 found when user is authorized to change mode"() {
        given:
        def lid = 123L
        def email = "john@example.com"
        def uidString = "owner"
        userService.authorizeOwner(uidString, lid) >> true
        accessService.change(lid, email) >> true

        when:
        def response = locationController.changeMode(lid, email, uidString)

        then:
        response.statusCode == HttpStatus.FOUND
    }


}