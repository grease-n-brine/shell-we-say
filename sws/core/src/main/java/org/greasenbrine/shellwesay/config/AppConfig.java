package org.greasenbrine.shellwesay.config;

/**
 * Immutable application configuration shared by all runtime modules.
 *
 * <p>This keeps launcher decisions and in-game defaults in one place so the
 * desktop module can wire platform concerns while the core module focuses on
 * gameplay.</p>
 */
public record AppConfig(
        String title,
        int windowWidth,
        int windowHeight,
        float clearRed,
        float clearGreen,
        float clearBlue,
        float clearAlpha
) {

    /**
     * Provides a sensible default configuration for local development.
     *
     * @return default configuration for Shell We Say.
     */
    public static AppConfig defaultConfig() {
        return new AppConfig("Shell We Say", 1280, 720, 0.08f, 0.10f, 0.14f, 1.0f);
    }
}
