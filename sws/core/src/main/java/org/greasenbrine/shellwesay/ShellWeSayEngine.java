package org.greasenbrine.shellwesay;

import com.badlogic.gdx.Game;
import org.greasenbrine.shellwesay.config.AppConfig;
import org.greasenbrine.shellwesay.screens.BootScreen;

/**
 * Core game entrypoint used by all platform launchers.
 *
 * <p>The desktop launcher provides platform setup (window, vsync, native
 * bindings), while this class owns game lifecycle and screen composition.</p>
 */
public final class ShellWeSayEngine extends Game {

    private final AppConfig appConfig;

    /**
     * Constructs the game with immutable runtime configuration.
     *
     * @param appConfig shared app configuration.
     */
    public ShellWeSayEngine(AppConfig appConfig) {
        this.appConfig = appConfig;
    }

    /**
     * Initializes the first screen.
     */
    @Override
    public void create() {
        setScreen(new BootScreen(appConfig));
    }
}
