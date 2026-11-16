package org.greasenbrine.shellwesay.desktop;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.greasenbrine.shellwesay.ShellWeSayEngine;
import org.greasenbrine.shellwesay.config.AppConfig;
import org.greasenbrine.shellwesay.screens.DefaultScreen;

/**
 * Desktop entrypoint that boots the core engine in a Swing host.
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
        SwingUtilities.invokeLater(DesktopLauncher::launch);
    }

    private static void launch() {
        AppConfig appConfig = AppConfig.defaultConfig();
        ShellWeSayEngine engine = new ShellWeSayEngine(appConfig);
        engine.initialize();

        JFrame frame = new JFrame(appConfig.title());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setContentPane(new DefaultScreenPanel(engine.defaultScreen()));
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private static final class DefaultScreenPanel extends JPanel {
        private final DefaultScreen screen;
        private final Color backgroundColor;

        private DefaultScreenPanel(DefaultScreen screen) {
            this.screen = screen;
            AppConfig appConfig = screen.appConfig();

            setPreferredSize(new Dimension(appConfig.windowWidth(), appConfig.windowHeight()));
            backgroundColor = new Color(
                    appConfig.clearRed(),
                    appConfig.clearGreen(),
                    appConfig.clearBlue(),
                    appConfig.clearAlpha()
            );
            setFont(new Font(Font.MONOSPACED, Font.PLAIN, 20));
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            graphics.setColor(backgroundColor);
            graphics.fillRect(0, 0, getWidth(), getHeight());

            graphics.setColor(Color.WHITE);
            graphics.setFont(getFont());

            int lineHeight = graphics.getFontMetrics().getHeight() + 8;
            int y = 40;
            for (String line : screen.startupLines()) {
                graphics.drawString(line, 32, y);
                y += lineHeight;
            }
        }
    }
}
