package com.convexcreative.pposcinterpreter.swing;

import com.bulenkov.darcula.DarculaLaf;
import com.convexcreative.pposcinterpreter.Main;
import com.convexcreative.pposcinterpreter.obj.ProjectData;
import com.convexcreative.pposcinterpreter.swing.callback.ReinitalizeHostCallback;
import com.convexcreative.propres.ProPresAPI;
import com.convexcreative.propres.event.server.ServerStatusChangeEvent;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;

public class ConvexFrame extends JFrame {

    private Main instance;

    public ConvexFrame(Main instance){
        super("ProPres -> Arena Interpreter");
        this.instance = instance;
        setupFrame();
    }


    public void setupFrame(){

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000,1000);

        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;


        JPanel hostPanel = new JPanel(new GridBagLayout());
        hostPanel.add(buildProHostPanel(), gbc);
        gbc.gridy = 1;
        hostPanel.add(buildOSCHostPanel(), gbc);
        JButton reinitHostButton = new JButton("Reinitialize Host...");
        reinitHostButton.addActionListener(new ReinitalizeHostCallback(instance));
        gbc.gridy = 2;
        hostPanel.add(reinitHostButton,gbc);


        add(hostPanel);


        setVisible(true);


    }

    public JPanel buildProHostPanel(){

        JPanel panel = new JPanel(new GridBagLayout());

        panel.setSize(500,500);
        panel.setBorder(BorderFactory.createLoweredSoftBevelBorder());

        ConvexTextField ppHostField = new ConvexTextField(ProjectData.DEF_HOST, "pphost", 8);
        ConvexTextField ppPortField = new ConvexTextField(ProjectData.DEF_PRO_PORT, "ppport", 8);
        ConvexTextField ppPasswordField = new ConvexTextField(ProjectData.DEF_PRO_PASSWORD, "pppass", 8);


        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 40;
        gbc.weighty = 40;


        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("ProPres Host"), gbc);
        gbc.gridx = 1;
        panel.add(ppHostField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(new JLabel("ProPres Port"), gbc);
        gbc.gridx = 1;
        panel.add(ppPortField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(new JLabel("ProPres Pass"), gbc);
        gbc.gridx = 1;
        panel.add(ppPasswordField, gbc);

        return panel;
    }

    public JPanel buildOSCHostPanel(){

        JPanel panel = new JPanel(new GridBagLayout());

        panel.setSize(500,500);
        panel.setBorder(BorderFactory.createLoweredSoftBevelBorder());

        ConvexTextField oscHostField = new ConvexTextField(ProjectData.DEF_HOST, "oschost", 8);
        ConvexTextField oscPortField = new ConvexTextField(ProjectData.DEF_OSC_PORT, "oscport", 8);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 40;
        gbc.weighty = 40;

        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("OSC Host"), gbc);
        gbc.gridx = 1;
        panel.add(oscHostField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(new JLabel("OSC Port"), gbc);
        gbc.gridx = 1;
        panel.add(oscPortField, gbc);

        return panel;


    }

    public Main getInstance(){
        return instance;
    }

}
