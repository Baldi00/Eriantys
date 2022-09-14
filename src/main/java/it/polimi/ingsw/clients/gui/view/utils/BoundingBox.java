package it.polimi.ingsw.clients.gui.view.utils;

import java.awt.*;

// TODO: do note that BoundingBox is almost identical to Java AWT Rectangle class
public record BoundingBox(int xOffset, int yOffset, int width, int height) {

    public Point getCenter() {
        return new Point(xOffset + (width / 2), yOffset + (height / 2));
    }

    public boolean contains(Point point) {
        return (xOffset <= point.x && point.x <= xOffset + width)
                && (yOffset <= point.y && point.y <= yOffset + height);
    }

}
