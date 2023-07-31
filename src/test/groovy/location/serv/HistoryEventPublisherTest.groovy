import com.example.location.entities.User
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.event.ApplicationEvents
import org.springframework.test.context.event.RecordApplicationEvents
import spock.lang.Specification
import org.springframework.context.ApplicationEventPublisher
import com.example.location.component.HistoryEventPublisher
import com.example.location.entities.History
import com.example.location.util.Util

import java.sql.Timestamp

@SpringBootTest(classes = [HistoryEventPublisher.class, ObjectMapper.class])
@RecordApplicationEvents
class HistoryEventPublisherTest extends Specification {

    @Autowired
    ApplicationEventPublisher eventPublisher
    @Autowired
    ObjectMapper objectMapper = new ObjectMapper()
    @Autowired
    ApplicationEvents applicationEvents
    @Autowired
    HistoryEventPublisher historyEventPublisher



    def "test publishHistoryCreatedEvent"() {
        given:
            def objectType = Util.ObjectType.ACCESS
            def object = new User()

        when:
            //historyEventPublisher.publishHistoryCreatedEvent(123L, objectType, object)
            def all = applicationEvents.findAll()
            println("Printing:" + all)
        then:
          //  1 * eventPublisher.publishEvent(_ as History)

            all.size() == 1

    }

    def "test publishHistoryDeletedEvent"() {

    }

    def "test publishHistoryUpdatedEvent"() {

    }
}
