package it.polimi.ingsw.clients.gui.view.components.panels.playing_field.utils;

import it.polimi.ingsw.clients.gui.view.exceptions.ElementNotInSceneException;
import it.polimi.ingsw.clients.gui.view.utils.BoundingBox;
import it.polimi.ingsw.models.components.Student;

import java.awt.*;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

// TODO: maybe this class should be split in other classes
/**
 * Contains the bounding-boxes of the sprites on the scene.
 * Contains the students currently placed on the entrance.
 */
public class SceneInfoManager {
    private static SceneInfoManager instance;
    private final Map<SceneSprite, BoundingBox> lut;
    /**
     * Colors of the students in the board entrance
     */
    // TODO: maybe, it should be removed in favor of SceneSprites.getStudentColor()
    private final Map<SceneSprite, Student> currentEntranceColors;

    private SceneInfoManager() {
        lut = new EnumMap<>(SceneSprite.class);
        currentEntranceColors = new EnumMap<>(SceneSprite.class);
    }

    public static SceneInfoManager getInstance() {
        if (instance == null)
            instance = new SceneInfoManager();
        return instance;
    }

    public void addElement(SceneSprite name, int x, int y, int w, int h) {
        lut.put(name, new BoundingBox(x, y, w, h));
    }

    public void setEntranceColor(SceneSprite sprite, Student color) {
        currentEntranceColors.put(sprite, color);
    }

    public Student getEntranceColor(SceneSprite sprite) {
        return currentEntranceColors.get(sprite);
    }

    public BoundingBox getBoundingBox(SceneSprite element) throws ElementNotInSceneException {
        BoundingBox boundingBox = lut.get(element);
        if (boundingBox == null)
            throw new ElementNotInSceneException("The requested element is not on the scene");
        return boundingBox;
    }

    /**
     * Computes the sprite on which the point is laying
     *
     * @param clickPosition the point where it has been clicked.
     * @return The intersected sprite.
     */
    public SceneSprite rayCast(Point clickPosition) {
        List<SceneSprite> intersectedSprites = new ArrayList<>();
        for (Map.Entry<SceneSprite, BoundingBox> el : lut.entrySet()) {
            if (el.getValue().contains(clickPosition))
                intersectedSprites.add(el.getKey());
        }

        // TODO: consider using z-index to avoid these kind of checks
        if (intersectedSprites.size() > 1 && intersectedSprites.get(0).isCharacterCard())
            return intersectedSprites.get(1);
        else if (!intersectedSprites.isEmpty())
            return intersectedSprites.get(0);
        else
            return null;
    }

    @Override
    public String toString() {
        return lut.toString();
    }
}
