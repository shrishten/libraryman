package com.library.observer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Publisher in the Observer design pattern.
 * Maintains a registry of listeners and dispatches events to interested subscribers.
 *
 * Design Pattern: Observer (Subject/Publisher role)
 */
@Component
public class LibraryEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(LibraryEventPublisher.class);

    private final List<LibraryEventListener> listeners = new ArrayList<>();

    /**
     * Registers a new listener to receive events.
     */
    public void subscribe(LibraryEventListener listener) {
        listeners.add(listener);
        log.debug("Listener registered: {}", listener.getClass().getSimpleName());
    }

    /**
     * Removes a listener from the registry.
     */
    public void unsubscribe(LibraryEventListener listener) {
        listeners.remove(listener);
    }

    /**
     * Fires an event to all listeners that are subscribed to its type.
     *
     * @param event the event to publish
     */
    public void publish(LibraryEvent event) {
        log.info("Publishing event: type={}, bookId={}, patronId={}",
                event.getType(), event.getBookId(), event.getPatronId());

        for (LibraryEventListener listener : listeners) {
            for (LibraryEvent.Type type : listener.subscribedTypes()) {
                if (type == event.getType()) {
                    try {
                        listener.onEvent(event);
                    } catch (Exception e) {
                        log.error("Error in listener {} handling event {}: {}",
                                listener.getClass().getSimpleName(), event.getType(), e.getMessage());
                    }
                    break;
                }
            }
        }
    }
}
