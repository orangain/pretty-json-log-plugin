package io.github.orangain.prettyjsonlog;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

@State(
        name = "io.github.orangain.prettyjsonlog.AppSettings",
        storages = @Storage("PrettyJsonLogPlugin.xml")
)
public final class AppSettings
        implements PersistentStateComponent<AppSettings.State> {

    public static class State {
        @NonNls
        public String messageFields = "message, msg, sMsg.msg, error.message, msgType";
        @NonNls
        public String errorFields = "stack_trace, exception, errorMessage, Exception, error, error.stack_trace";
        public boolean ideaStatus = false;

    }

    private State myState = new State();

    public static AppSettings getInstance() {
        return ApplicationManager.getApplication()
                .getService(AppSettings.class);
    }

    @Override
    public State getState() {
        return myState;
    }

    @Override
    public void loadState(@NotNull State state) {
        myState = state;
    }

}
