package it.polimi.ingsw.clients.gui.view.utils;

import it.polimi.ingsw.models.components.Student;
import it.polimi.ingsw.models.state.Stage;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.security.SecureRandom;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class GuiUtils {

    public static final Font DEFAULT_FONT = new Font("Sans Serif", Font.BOLD, 17);

    private GuiUtils() {
        // hide constructor
    }

    /**
     * Performs a random selection between categories with a limited number
     * of resources.
     *
     * @param thresholds resources limits.
     * @param min        interval lower-bound.
     * @param numElem    the number of elements.
     * @return random seed.
     */
    public static int limitedRand(int[] thresholds, int min, int numElem) {
        int res;

        do {
            res = new SecureRandom().nextInt(min, numElem + min);
        } while (thresholds[res] == 0);

        thresholds[res]--;
        return res;
    }

    public static Map<Student, Integer> frequenciesFromList(List<Student> list) {
        Map<Student, Integer> res = new EnumMap<>(Student.class);

        for (Student s : Student.values()) {
            int freq = 0;
            for (Student l : list)
                if (l.name().equals(s.name())) {
                    freq++;
                }
            res.put(s, freq);
        }

        return res;
    }

    public static String infoMessageFromStage(Stage s) {
        return switch (s) {
            case PLANNING_PLAY_ASSISTANTS -> "Gioca una carta assistente";
            case PLANNING_FILL_CLOUDS -> "Posiziona gli studenti sulle nuvole";
            case ACTION_MOVE_STUDENTS -> "Muovi gli studenti";
            case ACTION_MOVE_MOTHER_NATURE -> "Muovi Madre Natura";
            case ACTION_TAKE_STUDENTS_FROM_CLOUD -> "Prendi gli studenti da una nuvola";
            case ACTION_END_TURN -> "Termina turno";
            case ROUND_END -> "Fine round";
            case GAME_OVER -> "Game Over :(";
            case PREPARATION -> "Preparazione turno...";
            case WAIT_FOR_PLAYERS -> "Attendi gli altri giocatori...";
        };
    }

    public static BufferedImage resize(BufferedImage img, int width, int height) {
        Image scaled = img.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        BufferedImage bi = new BufferedImage(width, height, img.getType());

        Graphics2D g2d = bi.createGraphics();
        g2d.drawImage(scaled, 0, 0, null);
        g2d.dispose();

        return bi;
    }

    /**
     * pops-up an alert message
     *
     * @param msg the alert message
     */
    public static void alert(String msg) {
        JFrame alertWindow = new JFrame();
        alertWindow.setAlwaysOnTop(true);
        JOptionPane.showMessageDialog(alertWindow, msg);
    }

    public static int confirm(String msg) {
        JFrame optionWindow = new JFrame();
        optionWindow.setAlwaysOnTop(true);
        return JOptionPane.showConfirmDialog(optionWindow, msg, "Question", JOptionPane.OK_CANCEL_OPTION);
    }

    public static int yesNoAlert(String msg) {
        JFrame optionWindow = new JFrame();
        optionWindow.setAlwaysOnTop(true);
        return JOptionPane.showConfirmDialog(optionWindow, msg, "Question", JOptionPane.YES_NO_OPTION);
    }

    public static int option(String msg, String title, List<String> options) {
        JFrame optionWindow = new JFrame();
        optionWindow.setAlwaysOnTop(true);
        return JOptionPane.showOptionDialog(
                optionWindow,
                msg,
                title,
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options.toArray(),
                null
        );
    }
}
