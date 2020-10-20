package com.convexcreative.pposcinterpreter.swing;

import javax.swing.*;
import java.awt.*;

public class JLEDIndicator extends JPanel {

    private String labelText;

    public JLEDIndicator(String labelText){
        super(new GridBagLayout());
        this.labelText = labelText;
        refresh(Color.blue);
    }

    public void refresh(Color color){

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridy = 0;
        gbc.gridx = 0;

        JLabel text = new JLabel(labelText);

        removeAll();
        add(text, gbc);

        gbc.gridx = 1;
        JLabel dot = new JLabel("•");
        dot.setFont(new Font("TimesNewRoman", Font.PLAIN, 100));
        dot.setForeground(color);

        add(dot, gbc);

    }




}
