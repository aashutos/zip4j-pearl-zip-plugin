/*
 * Copyright © 2021 92AK
 */
package com.ntak.pearlzip.ui.util;

import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import static com.ntak.pearlzip.archive.util.LoggingUtil.getStackTraceFromException;
import static com.ntak.pearlzip.archive.util.LoggingUtil.resolveTextKey;
import static com.ntak.pearlzip.ui.constants.ZipConstants.LOG_GENERAL_EVENT_HANDLER_EXCEPTION;
import static com.ntak.pearlzip.ui.constants.ZipConstants.ROOT_LOGGER;
import static com.ntak.pearlzip.ui.util.JFXUtil.raiseAlert;

/**
 *  Custom event handler wrapper with pre-execution checks and post-execution clean up. The caught exceptions will
 *  raise an alert UI, which will be displayed to the user. The ERROR AlertType will force the closure of the open
 *  archive instance.
 *
 *  @param <T> - Event Type
 *
 *  @author Aashutos Kakshepati
 */
public interface CheckEventHandler<T extends Event> extends EventHandler<T> {

    @Override
    default void handle(T event) {
        try {
            try {
                check(event);
            } catch (AlertException ae) {
                // Raise alert
                raiseAlert(ae.getType(),
                           ae.getTitle(),
                           ae.getHeader(),
                           ae.getBody(),
                           ae,
                           ae.getStage(),
                           ae.getButtons()
                );

                Stage archiveWindow = Stage.getWindows()
                     .stream()
                     .map(Stage.class::cast)
                     .filter(s->s.getTitle() != null)
                     .filter((s)->s.getTitle().matches(String.format(".*%s$", ae.getArchiveInfo().getArchivePath())))
                     .findFirst()
                     .orElse(null);

                // Closing archive if error raised...
                if (ae.getType().equals(Alert.AlertType.ERROR)) {
                    ae.getArchiveInfo().getCloseBypass().set(true);
                    archiveWindow.fireEvent(new WindowEvent(archiveWindow, WindowEvent.WINDOW_CLOSE_REQUEST));
                    return;
                }
            }

            handleEvent(event);
        }catch (Exception e) {
            // LOG: Event handler <Class Name> threw Exception of type <Exception> with message <Message>
            // Stack trace:
            // <Stack trace>
            ROOT_LOGGER.error(resolveTextKey(LOG_GENERAL_EVENT_HANDLER_EXCEPTION, this.getClass().getCanonicalName(),
                              e.getClass().getCanonicalName(), e.getMessage(), getStackTraceFromException(e)));
        } finally {
            clearUp(event);
        }
    }

    default void clearUp(T event) {}

    void handleEvent(T event);

    default void check(T event) throws AlertException {}
}
