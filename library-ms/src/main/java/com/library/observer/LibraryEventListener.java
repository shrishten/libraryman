package com.library.observer;

/**
 * Observer interface for the Observer design pattern.
 * Any component that needs to react to library events implements this.
 */
public interface LibraryEventListener {

    /**
     * Called when a library event is published.
     *
     * @param event the event that occurred
     */
    void onEvent(LibraryEvent event);

    /**
     * Returns the event types this listener is interested in.
     */
    LibraryEvent.Type[] subscribedTypes();
}
