package location.service


import com.example.location.entities.Access
import com.example.location.entities.Location
import com.example.location.entities.User
import com.example.location.repositories.HistoryRepository
import com.example.location.repositories.HistoryRepositoryImpl
import com.example.location.services.HistoryService
import com.fasterxml.jackson.databind.ObjectMapper
import org.spockframework.spring.SpringBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.event.ApplicationEvents
import org.springframework.test.context.event.RecordApplicationEvents
import spock.lang.Specification
import org.springframework.context.ApplicationEventPublisher
import com.example.location.component.HistoryEventPublisher
import com.example.location.entities.History
import com.example.location.util.Util
import spock.lang.Unroll

import java.util.concurrent.CompletableFuture


@SpringBootTest(classes = [HistoryEventPublisher.class, ObjectMapper.class, HistoryRepositoryImpl.class, HistoryService.class, JdbcTemplate.class, ApplicationEvents.class])
@RecordApplicationEvents
class HistoryEventPublisherTest extends Specification {

    @Autowired
    ApplicationEventPublisher eventPublisher
    @Autowired
    ObjectMapper objectMapper
    @Autowired
    ApplicationEvents applicationEvents
    @Autowired
    HistoryEventPublisher historyEventPublisher
    @Autowired
    HistoryService historyService
    @SpringBean
    HistoryRepository historyRepository = Mock()
    @SpringBean
    JdbcTemplate jdbcTemplate = Mock()

    @Unroll
    def "test publishHistoryCreatedEvent with #objectType"() {
        given:
        History history = null
        def object = eventObject
        def expectedObjectType = Util.ObjectType.valueOf(objectType)
        def expectedAction = Util.ActionType.CREATED.name()
        def expectedActionBy = 123L

        when:
        historyEventPublisher.publishHistoryCreatedEvent(expectedActionBy, expectedObjectType, object)

        then:
        1 * historyRepository.save(_) >> { arg ->
            history = arg[0] as History
            CompletableFuture.completedFuture(arg[0])
        }

        and:
        history != null
        history.getAction() == expectedAction
        history.getObjectType() == expectedObjectType.name()
        history.getActionBy() == expectedActionBy
        history.getActionDetails() == objectMapper.writeValueAsString(object)

        where:
        objectType | eventObject
        "ACCESS"   | new User(uid: 1L, firstName: "test", password: "test", email: "test")
        "LOCATION" | new Location(lid: 1L, name: "test", address: "test")
        "ACCESS"   | new Access(uid: 123L, lid: 123L, type: "test")
    }

    @Unroll
    def "test publishHistoryDeletedEvent with #objectType"() {
        given:
        History history = null
        def object = eventObject
        def expectedObjectType = Util.ObjectType.valueOf(objectType)
        def expectedAction = Util.ActionType.DELETED.name()
        def expectedActionBy = 123L

        when:
        historyEventPublisher.publishHistoryDeletedEvent(expectedActionBy, expectedObjectType, object)

        then:
        1 * historyRepository.save(_) >> { arg ->
            history = arg[0] as History
            CompletableFuture.completedFuture(arg[0])
        }

        and:
        history != null
        history.getAction() == expectedAction
        history.getObjectType() == expectedObjectType.name()
        history.getActionBy() == expectedActionBy
        history.getActionDetails() == objectMapper.writeValueAsString(object)

        where:
        objectType | eventObject
        "ACCESS"   | new User(uid: 1L, firstName: "test", password: "test", email: "test")
        "LOCATION" | new Location(lid: 1L, name: "test", address: "test")
        "ACCESS"   | new Access(uid: 123L, lid: 123L, type: "test")
    }

    @Unroll
    def "test publishHistoryUpdatedEvent with #objectType"() {
        given:
        History history = null
        def expectedObjectType = Util.ObjectType.valueOf(objectType)
        def expectedAction = Util.ActionType.UPDATED.name()
        def expectedActionBy = 123L

        when:
        historyEventPublisher.publishHistoryUpdatedEvent(expectedActionBy, expectedObjectType, oldObject, newObject)

        then:
        1 * historyRepository.save(_) >> { arg ->
            history = arg[0] as History
            CompletableFuture.completedFuture(arg[0])
        }

        and:
        history != null
        history.getAction() == expectedAction
        history.getObjectType() == expectedObjectType.name()
        history.getActionBy() == expectedActionBy
        history.getActionDetails() == objectMapper.writeValueAsString(oldObject) + " -> " + objectMapper.writeValueAsString(newObject)
        where:
        objectType | oldObject                                                             | newObject
        "ACCESS"   | new User(uid: 1L, firstName: "test", password: "test", email: "test") | new User(uid: 1L, firstName: "test2", password: "test2", email: "test2")
        "LOCATION" | new Location(lid: 1L, name: "test", address: "test")                  | new Location(lid: 1L, name: "test2", address: "test2")
        "ACCESS"   | new Access(uid: 123L, lid: 123L, type: "test")                        | new Access(uid: 123L, lid: 123L, type: "test2")

    }
}
