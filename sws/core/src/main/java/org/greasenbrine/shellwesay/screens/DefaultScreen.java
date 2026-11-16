package org.greasenbrine.shellwesay.screens;

import org.greasenbrine.shellwesay.config.AppConfig;

import java.util.List;

/**
 * The first screen shown when the game starts.
 *
 * <p>This screen intentionally holds render-neutral state so different desktop
 * hosts (Swing, OpenGL, etc.) can present the same data.</p>
 */
public final class DefaultScreen {

    private final AppConfig appConfig;
    private final List<String> startupLines;

    /**
     * Creates the default startup screen.
     *
     * @param appConfig immutable app-level rendering configuration.
     */
    public DefaultScreen(AppConfig appConfig) {
        this.appConfig = appConfig;
        startupLines = List.of(
                "Default Screen",
                "Pipeline is alive. Next: swap to MenuScreen/GameplayScreen."
        );
    }

    /**
     * Returns startup text that the active UI host should render.
     *
     * @return immutable startup text lines.
     */
    public List<String> startupLines() {
        return startupLines;
    }

    /**
     * Returns app configuration associated with this screen.
     *
     * @return immutable app config.
     */
    public AppConfig appConfig() {
        return appConfig;
    }
}
