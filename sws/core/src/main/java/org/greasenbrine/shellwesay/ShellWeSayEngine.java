package org.greasenbrine.shellwesay;

import org.greasenbrine.shellwesay.config.AppConfig;
import org.greasenbrine.shellwesay.screens.DefaultScreen;

/**
 * Core game entrypoint used by all platform launchers.
 */
public final class ShellWeSayEngine {

    private final AppConfig appConfig;
    private DefaultScreen defaultScreen;

    /**
     * Constructs the game with immutable runtime configuration.
     *
     * @param appConfig shared app configuration.
     */
    public ShellWeSayEngine(AppConfig appConfig) {
        this.appConfig = appConfig;
    }

    /**
     * Initializes gameplay state for the active runtime.
     */
    public void initialize() {
        defaultScreen = new DefaultScreen(appConfig);
    }

    /**
     * Returns the active startup screen state.
     *
     * @return initialized startup screen.
     */
    public DefaultScreen defaultScreen() {
        if (defaultScreen == null) {
            throw new IllegalStateException("Engine is not initialized. Call initialize() first.");
        }
        return defaultScreen;
    }
}
