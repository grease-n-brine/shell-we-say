package org.greasenbrine.shellwesay.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import org.greasenbrine.shellwesay.config.AppConfig;

/**
 * Minimal startup screen used to validate render lifecycle and timing.
 *
 * <p>By moving rendering out of the launcher and into a Screen, we keep
 * responsibilities explicit and make future screens (menu, game, pause)
 * easy to compose and test.</p>
 */
public final class BootScreen extends ScreenAdapter {

    private final AppConfig appConfig;

    /**
     * Creates the boot screen.
     *
     * @param appConfig immutable rendering and window defaults.
     */
    public BootScreen(AppConfig appConfig) {
        this.appConfig = appConfig;
    }

    /**
     * Renders one frame by clearing the backbuffer with configured color.
     *
     * @param delta elapsed seconds since the previous frame.
     */
    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(
                appConfig.clearRed(),
                appConfig.clearGreen(),
                appConfig.clearBlue(),
                appConfig.clearAlpha()
        );
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
    }
}
