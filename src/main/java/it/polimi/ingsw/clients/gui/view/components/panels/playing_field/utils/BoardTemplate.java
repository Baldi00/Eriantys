package it.polimi.ingsw.clients.gui.view.components.panels.playing_field.utils;

import it.polimi.ingsw.models.components.Student;
import it.polimi.ingsw.models.constants.GameConstants;

import java.awt.*;

/**
 * computes the coordinates of all the relevant elements which should be clickable on the board
 */
public class BoardTemplate {
    private final Point offset;
    private final int width;
    private final int height;
    private final Student[] colorOrder = {Student.GREEN, Student.RED, Student.YELLOW, Student.PINK, Student.CYAN};

    // relevant points on the board, expressed in percentages from the northwest point
    private static final double STUDENT_W = .047;
    private static final double STUDENT_H = .11;
    private static final double TOWER_W = .06;
    private static final double TOWER_H = .137;
    private static final double PROF_H = .116;
    private static final double PROF_W = .05;
    private static final double HALL_NW_X = .185;
    private static final double HALL_NW_Y = .117;
    private static final double HALL_ROW_GAP = .055;
    private static final double PROF_TABLE_NW_X = .707;
    private static final double PROF_TABLE_NW_Y = .11;
    private static final double PROF_TABLE_ROW_GAP = .05;
    private static final double TOWERS_NW_X = .81;
    private static final double TOWERS_NW_Y = .185;
    private static final double TOWERS_COL_GAP = .023;
    private static final double ENTRANCE_NW_X = .03;
    private static final double ENTRANCE_NW_Y = .282;
    private static final double ENTRANCE_COL_GAP = .015;

    public BoardTemplate(int xOffset, int yOffset, int width, int height) {
        offset = new Point(xOffset, yOffset);
        this.width = width;
        this.height = height;
        buildRelevantPoints();
    }

    private void buildRelevantPoints() {
        buildHall();
        buildProfessorTable();
        buildTowerZone();
        buildEntrance();
    }

    private void buildHall() {
        for (int i = 0; i < colorOrder.length; ++i)
            buildHallRow(i, colorOrder[i]);
    }

    private void buildHallRow(int rowIndex, Student student) {
        for (int i = 0; i < GameConstants.MAX_STUDENTS_ON_HALL_PER_COLOR; i++) {
            SceneInfoManager.getInstance().addElement(
                    SceneSprite.getBoardHall(i + 1, student),
                    (int) (offset.x + (HALL_NW_X * width) + (i * STUDENT_W * width)),
                    (int) (offset.y + (rowIndex * ((STUDENT_H * height) + (HALL_ROW_GAP * height))) + (HALL_NW_Y * height)),
                    (int) (STUDENT_W * width),
                    (int) (STUDENT_H * height)
            );
        }
    }

    private void buildProfessorTable() {
        for (int i = 0; i < colorOrder.length; i++) {
            SceneInfoManager.getInstance().addElement(
                    SceneSprite.getBoardProfessor(colorOrder[i]),
                    (int) (offset.x + (PROF_TABLE_NW_X * width)),
                    (int) (offset.y + (PROF_TABLE_NW_Y * height) + (i * ((PROF_H * height) + (PROF_TABLE_ROW_GAP * height)))),
                    (int) (PROF_W * width),
                    (int) (PROF_H * height)
            );
        }
    }

    private void buildTowerZone() {
        for (int i = 0; i < 4; i++) {
            SceneInfoManager.getInstance().addElement(
                    SceneSprite.getBoardTower(i + 1),
                    (int) (offset.x + (TOWERS_NW_X * width)),
                    (int) (offset.y + (TOWERS_NW_Y * height) + (i * ((TOWER_H * height) + (PROF_TABLE_ROW_GAP * height)))),
                    (int) (TOWER_W * width),
                    (int) (TOWER_H * height)
            );
        }

        for (int i = 0; i < 4; i++) {
            SceneInfoManager.getInstance().addElement(
                    SceneSprite.getBoardTower(i + 5),
                    (int) (offset.x + (TOWERS_NW_X + TOWER_W + TOWERS_COL_GAP) * width),
                    (int) (offset.y + (TOWERS_NW_Y * height) + (i * ((TOWER_H * height) + (PROF_TABLE_ROW_GAP * height)))),
                    (int) (TOWER_W * width),
                    (int) (TOWER_H * height)
            );
        }
    }

    private void buildEntrance() {
        for (int i = 0; i < 4; i++) {
            SceneInfoManager.getInstance().addElement(
                    SceneSprite.getBoardEntrance(i + 1),
                    (int) (offset.x + (ENTRANCE_NW_X * width)),
                    (int) (offset.y + (ENTRANCE_NW_Y * height) + (i * ((STUDENT_H * height) + (HALL_ROW_GAP * height)))),
                    (int) (STUDENT_W * width),
                    (int) (STUDENT_H * height)
            );
        }

        for (int i = 0; i < 4; i++) {
            SceneInfoManager.getInstance().addElement(
                    SceneSprite.getBoardEntrance(i + 5),
                    (int) (offset.x + (ENTRANCE_NW_X + STUDENT_W + ENTRANCE_COL_GAP) * width),
                    (int) (offset.y + (ENTRANCE_NW_Y * height) + (i * ((STUDENT_H * height) + (HALL_ROW_GAP * height)))),
                    (int) (STUDENT_W * width),
                    (int) (STUDENT_H * height)
            );
        }

        SceneInfoManager.getInstance().addElement(
                SceneSprite.getBoardEntrance(9),
                (int) (offset.x + (ENTRANCE_NW_X + STUDENT_W + ENTRANCE_COL_GAP) * width),
                (int) (offset.y + (ENTRANCE_NW_Y * height) - ((STUDENT_H * height) + (HALL_ROW_GAP * height))),
                (int) (STUDENT_W * width),
                (int) (STUDENT_H * height)
        );
    }
}
