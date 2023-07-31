import com.example.location.services.HistoryService;
import spock.lang.Specification
import spock.lang.Subject
import com.example.location.entities.History
import com.example.location.repositories.HistoryRepository

import java.sql.Timestamp
import java.util.concurrent.CompletableFuture

class HistoryServiceTest extends Specification {

    @Subject
    HistoryService historyService

    HistoryRepository historyRepository = Mock(HistoryRepository)

    def setup() {
        historyService = new HistoryService(historyRepository)
    }

    def "test handleObjectEvent - success"() {
        given:
            History historyEvent = new History(hid: 123, actionBy: 456, objectType: "Object", action: "CREATE", actionDetails: "Details", date: new Timestamp(System.currentTimeMillis()))

        when:
            def result = historyService.handleObjectEvent(historyEvent)

        then:
            result instanceof CompletableFuture
            1 * historyRepository.save(historyEvent) >> CompletableFuture.completedFuture(historyEvent)

        and:
            notThrown(Exception)



    }

    def "test handleObjectEvent - failure"() {
        given:
            History historyEvent = new History(actionBy: 456, objectType: "Object", action: "CREATE", actionDetails: "Details", date: new Timestamp(System.currentTimeMillis()))

        when:
            def result = historyService.handleObjectEvent(historyEvent)

        then:
            result instanceof CompletableFuture
            1 * historyRepository.save(historyEvent) >> CompletableFuture.completedFuture(new History())

        and:
            notThrown(Exception)

    }
}
