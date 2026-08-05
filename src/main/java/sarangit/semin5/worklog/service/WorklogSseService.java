package sarangit.semin5.worklog.service;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class WorklogSseService {
    private final Set<SseEmitter> emitters = ConcurrentHashMap.newKeySet();

    public SseEmitter subscribe() {
        SseEmitter emitter = new SseEmitter(0L);
        emitters.add(emitter);
        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> emitters.remove(emitter));
        emitter.onError(error -> emitters.remove(emitter));
        send(emitter, "connected");
        return emitter;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void publishAfterCommit(WorklogChangedEvent ignored) {
        emitters.forEach(emitter -> send(emitter, ignored));
    }

    private void send(SseEmitter emitter, Object payload) {
        try {
            emitter.send(SseEmitter.event().name("worklog-update").data(payload, MediaType.APPLICATION_JSON));
        } catch (IOException | IllegalStateException exception) {
            emitters.remove(emitter);
        }
    }
}
