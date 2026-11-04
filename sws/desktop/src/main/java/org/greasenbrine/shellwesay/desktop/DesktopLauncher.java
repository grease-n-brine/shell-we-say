package org.greasenbrine.shellwesay.desktop;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import org.greasenbrine.shellwesay.ShellWeSayEngine;
import org.greasenbrine.shellwesay.config.AppConfig;

/**
 * Desktop entrypoint that configures LWJGL3 and boots the core engine.
 */
public final class DesktopLauncher {

    private DesktopLauncher() {
        // Utility class.
    }

    /**
     * Launches the desktop runtime.
     *
     * @param args command-line arguments, currently unused.
     */
    public static void main(String[] args) {
        AppConfig appConfig = AppConfig.defaultConfig();

        Lwjgl3ApplicationConfiguration configuration = new Lwjgl3ApplicationConfiguration();
        configuration.setTitle(appConfig.title());
        configuration.setWindowedMode(appConfig.windowWidth(), appConfig.windowHeight());
        configuration.useVsync(true);

        new Lwjgl3Application(new ShellWeSayEngine(appConfig), configuration);
    }
}
