package location.service

import com.example.location.entities.History
import com.example.location.repositories.HistoryRepository
import com.example.location.services.HistoryService
import spock.lang.Specification
import spock.lang.Subject

import java.util.concurrent.CompletableFuture

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
