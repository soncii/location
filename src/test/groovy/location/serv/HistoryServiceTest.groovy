package location.serv

import com.example.location.entities.History
import com.example.location.repositories.HistoryRepository
import com.example.location.services.HistoryService
import spock.lang.AutoCleanup
import spock.lang.Specification
import spock.lang.Subject
import org.apache.logging.log4j.message.Message
import java.util.concurrent.CompletableFuture
import org.apache.logging.log4j.Logger

class HistoryServiceTest extends Specification {

    @Subject
    HistoryService historyService

    HistoryRepository historyRepository = Mock(HistoryRepository)

    def setup() {

        historyService = new HistoryService(historyRepository)
    }

    def "test handleObjectEvent"() {

        given:
            History historyEvent = new History(hid: 123, actionBy: 456, objectType: "Object", action: "CREATE", actionDetails: "Details", date: new java.sql.Timestamp(System.currentTimeMillis()))

        when:
            historyService.handleObjectEvent(historyEvent)

        then:
            1 * historyRepository.save(historyEvent) >> CompletableFuture.completedFuture(historyEvent)
    }
}
