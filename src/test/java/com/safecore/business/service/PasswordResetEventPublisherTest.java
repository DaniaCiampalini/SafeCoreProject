package com.safecore.business.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PasswordResetEventPublisherTest {

    private PasswordResetEventPublisher publisher;

    @BeforeEach
    void setUp() {
        publisher = new PasswordResetEventPublisher();
    }

    @Test
    void register_addsObserver() {
        TestObserver observer = new TestObserver();

        publisher.register(observer);
        publisher.publish(new PasswordResetCompletedEvent("test@example.com", LocalDateTime.now()));

        assertTrue(observer.wasNotified());
    }

    @Test
    void unregister_removesObserver() {
        TestObserver observer = new TestObserver();

        publisher.register(observer);
        publisher.unregister(observer);
        publisher.publish(new PasswordResetCompletedEvent("test@example.com", LocalDateTime.now()));

        assertFalse(observer.wasNotified());
    }

    @Test
    void publish_notifiesAllObservers() {
        TestObserver observer1 = new TestObserver();
        TestObserver observer2 = new TestObserver();
        TestObserver observer3 = new TestObserver();

        publisher.register(observer1);
        publisher.register(observer2);
        publisher.register(observer3);

        PasswordResetCompletedEvent event = new PasswordResetCompletedEvent("test@example.com", LocalDateTime.now());
        publisher.publish(event);

        assertTrue(observer1.wasNotified());
        assertTrue(observer2.wasNotified());
        assertTrue(observer3.wasNotified());
        assertEquals(event, observer1.getReceivedEvent());
        assertEquals(event, observer2.getReceivedEvent());
        assertEquals(event, observer3.getReceivedEvent());
    }

    @Test
    void publish_maintainsObserverOrder() {
        List<String> executionOrder = new ArrayList<>();

        PasswordResetObserver observer1 = event -> executionOrder.add("observer1");
        PasswordResetObserver observer2 = event -> executionOrder.add("observer2");
        PasswordResetObserver observer3 = event -> executionOrder.add("observer3");

        publisher.register(observer1);
        publisher.register(observer2);
        publisher.register(observer3);

        publisher.publish(new PasswordResetCompletedEvent("test@example.com", LocalDateTime.now()));

        assertEquals(3, executionOrder.size());
        assertEquals("observer1", executionOrder.get(0));
        assertEquals("observer2", executionOrder.get(1));
        assertEquals("observer3", executionOrder.get(2));
    }

    @Test
    void publish_withNoObservers_doesNotThrow() {
        assertDoesNotThrow(() ->
                publisher.publish(new PasswordResetCompletedEvent("test@example.com", LocalDateTime.now()))
        );
    }

    @Test
    void publish_withNullEvent_doesNotThrow() {
        TestObserver observer = new TestObserver();
        publisher.register(observer);

        // L'implementazione potrebbe gestire null senza lanciare eccezione
        assertDoesNotThrow(() -> publisher.publish(null));
    }

    @Test
    void register_sameObserverTwice_notifiesTwice() {
        TestObserver observer = new TestObserver();

        publisher.register(observer);
        publisher.register(observer);

        publisher.publish(new PasswordResetCompletedEvent("test@example.com", LocalDateTime.now()));

        // L'implementazione attuale permette registrazioni duplicate
        assertEquals(2, observer.getNotificationCount());
    }

    @Test
    void publish_whenObserverThrowsException_continuesNotifyingOthers() {
        TestObserver observer1 = new TestObserver();
        PasswordResetObserver failingObserver = event -> {
            throw new RuntimeException("Observer failed");
        };
        TestObserver observer3 = new TestObserver();

        publisher.register(observer1);
        publisher.register(failingObserver);
        publisher.register(observer3);

        // Non dovrebbe lanciare eccezione
        assertDoesNotThrow(() ->
                publisher.publish(new PasswordResetCompletedEvent("test@example.com", LocalDateTime.now()))
        );

        // Gli altri observer devono essere stati notificati
        assertTrue(observer1.wasNotified());
        assertTrue(observer3.wasNotified());
    }

    @Test
    void unregister_nonExistentObserver_doesNotThrow() {
        TestObserver observer = new TestObserver();

        assertDoesNotThrow(() -> publisher.unregister(observer));
    }

    // Helper class per i test
    private static class TestObserver implements PasswordResetObserver {
        private boolean notified = false;
        private PasswordResetCompletedEvent receivedEvent;
        private int notificationCount = 0;

        @Override
        public void onPasswordResetCompleted(PasswordResetCompletedEvent event) {
            notified = true;
            receivedEvent = event;
            notificationCount++;
        }

        public boolean wasNotified() {
            return notified;
        }

        public PasswordResetCompletedEvent getReceivedEvent() {
            return receivedEvent;
        }

        public int getNotificationCount() {
            return notificationCount;
        }
    }
}
