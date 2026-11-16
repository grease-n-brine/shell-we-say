package org.greasenbrine.shellwesay.desktop.ui.map;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

import org.greasenbrine.shellwesay.config.AppConfig;

/**
 * Reusable map viewport panel.
 *
 * <p>This panel owns map-space projection and pointer rendering so later
 * gameplay screens can reuse it without changing launcher wiring.</p>
 */
public final class MapPanel extends JPanel {
    private static final int MAP_PADDING = 32;

    private static final Color MAP_BASE_COLOR = new Color(24, 36, 28);
    private static final Color MAP_GRID_COLOR = new Color(63, 80, 66);
    private static final Color MAP_BORDER_COLOR = new Color(121, 141, 124);
    private static final Color POINTER_OUTLINE_COLOR = new Color(238, 243, 238);

    private final Color backgroundColor;
    private final List<MapPointer> pointers;

    public MapPanel(AppConfig appConfig) {
        setPreferredSize(new Dimension(appConfig.windowWidth(), appConfig.windowHeight()));
        backgroundColor = new Color(
                appConfig.clearRed(),
                appConfig.clearGreen(),
                appConfig.clearBlue(),
                appConfig.clearAlpha()
        );

        setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));
        pointers = new ArrayList<>();
    }

    /**
     * Replaces the current pointer set.
     */
    public void setPointers(List<MapPointer> newPointers) {
        pointers.clear();
        pointers.addAll(newPointers);
        repaint();
    }

    /**
     * Adds one pointer marker to the map panel.
     */
    public void addPointer(MapPointer pointer) {
        pointers.add(pointer);
        repaint();
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D g2 = (Graphics2D) graphics.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2.setColor(backgroundColor);
        g2.fillRect(0, 0, getWidth(), getHeight());

        Rectangle2D mapBounds = new Rectangle2D.Float(
                MAP_PADDING,
                MAP_PADDING,
                getWidth() - (MAP_PADDING * 2f),
                getHeight() - (MAP_PADDING * 2f)
        );

        drawMapArea(g2, mapBounds);
        drawPointers(g2, mapBounds);
        drawLegend(g2);
        g2.dispose();
    }

    private void drawMapArea(Graphics2D g2, Rectangle2D mapBounds) {
        g2.setColor(MAP_BASE_COLOR);
        g2.fill(mapBounds);

        g2.setStroke(new BasicStroke(1.0f));
        g2.setColor(MAP_GRID_COLOR);

        int cells = 12;
        for (int i = 1; i < cells; i++) {
            double x = mapBounds.getX() + (mapBounds.getWidth() * i / cells);
            double y = mapBounds.getY() + (mapBounds.getHeight() * i / cells);
            g2.drawLine((int) x, (int) mapBounds.getY(), (int) x, (int) (mapBounds.getY() + mapBounds.getHeight()));
            g2.drawLine((int) mapBounds.getX(), (int) y, (int) (mapBounds.getX() + mapBounds.getWidth()), (int) y);
        }

        g2.setColor(MAP_BORDER_COLOR);
        g2.setStroke(new BasicStroke(2.0f));
        g2.draw(mapBounds);
    }

    private void drawPointers(Graphics2D g2, Rectangle2D mapBounds) {
        for (MapPointer pointer : pointers) {
            float px = (float) (mapBounds.getX() + pointer.mapX() * mapBounds.getWidth());
            float py = (float) (mapBounds.getY() + pointer.mapY() * mapBounds.getHeight());
            float radius = pointer.radius();

            g2.setColor(pointer.color());
            g2.fill(new Ellipse2D.Float(px - radius, py - radius, radius * 2f, radius * 2f));

            g2.setColor(POINTER_OUTLINE_COLOR);
            g2.setStroke(new BasicStroke(1.25f));
            g2.draw(new Ellipse2D.Float(px - radius, py - radius, radius * 2f, radius * 2f));

            if (!pointer.label().isBlank()) {
                g2.setColor(POINTER_OUTLINE_COLOR);
                g2.drawString(pointer.label(), px + radius + 6f, py - radius - 2f);
            }
        }
    }

    private void drawLegend(Graphics2D g2) {
        g2.setColor(new Color(201, 214, 204));
        g2.drawString("Map Panel (fixed frame).", 14, 20);
        g2.drawString("Pointers can be added later via addPointer/setPointers.", 14, 38);
    }
}
