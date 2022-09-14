package it.polimi.ingsw.clients.gui.view.components.custom;

import it.polimi.ingsw.clients.gui.view.utils.GuiUtils;
import it.polimi.ingsw.models.components.Student;

import javax.swing.*;

public class StudentCounter extends JLabel {
    private final Student color;

    public StudentCounter(Student color) {
        super();
        this.color = color;
        setFont(GuiUtils.DEFAULT_FONT);
    }

    public Student getColor() {
        return color;
    }
}
