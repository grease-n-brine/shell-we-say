package org.greasenbrine.shellwesay.desktop;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.greasenbrine.shellwesay.ShellWeSayEngine;
import org.greasenbrine.shellwesay.config.AppConfig;
import org.greasenbrine.shellwesay.desktop.ui.map.MapPanel;

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
        frame.setResizable(false);
        frame.setContentPane(new MapPanel(engine.defaultScreen().appConfig()));
        frame.pack();
        frame.setMinimumSize(frame.getSize());
        frame.setMaximumSize(frame.getSize());
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
