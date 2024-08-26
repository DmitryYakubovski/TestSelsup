package com.example.test1;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CrptApi {

    Semaphore requestSemaphore;

    ScheduledExecutorService scheduler;

    public CrptApi(TimeUnit timeUnit, int requestLimit) {

        this.requestSemaphore = new Semaphore(requestLimit);
        this.scheduler = Executors.newScheduledThreadPool(1);

        scheduler.scheduleAtFixedRate(() -> {
            requestSemaphore.release(requestLimit - requestSemaphore.availablePermits());
        }, 0, 1, timeUnit);
    }

    public void handleRequest(Object document, String signature) {

        try {
            requestSemaphore.acquire();
            performApiCall(document, signature);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void performApiCall(Object document, String signature) {

    }

    @RestController
    @RequestMapping("/api/v3/lk/documents")
    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    public static class CrptApiController {

        static CrptApi crptApi = new CrptApi(TimeUnit.MINUTES, 2);

        @PostMapping(value = "/create", consumes = "application/json")
        public void createDocument(@RequestBody(required = false) Object document, @RequestParam(required = false) String signature) {
            crptApi.handleRequest(document, signature);
        }
    }

}



