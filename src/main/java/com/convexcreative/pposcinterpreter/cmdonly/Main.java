package com.convexcreative.pposcinterpreter.cmdonly;

import com.bulenkov.darcula.DarculaLaf;
import com.convexcreative.ezlogger.ConvexLogger;
import com.convexcreative.ezlogger.LoggerEvent;
import com.convexcreative.pposcinterpreter.enu.ClipType;
import com.convexcreative.pposcinterpreter.manager.ProPresManager;
import com.convexcreative.pposcinterpreter.manager.ResolumeManager;
import com.convexcreative.pposcinterpreter.obj.ClipData;
import com.convexcreative.pposcinterpreter.obj.ProjectData;
import com.convexcreative.propres.ProPresAPI;
import com.convexcreative.propres.ProPresAPIConfig;
import com.convexcreative.propres.event.server.ServerDisconnectEvent;
import com.convexcreative.propres.event.slide.SlideChangeEvent;
import com.convexcreative.propres.serializable.Slide;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.illposed.osc.OSCMessage;
import com.illposed.osc.OSCSerializeException;
import com.illposed.osc.transport.udp.OSCPortOut;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Scanner;

public class Main {

    private static JTextArea hostTextArea;
    private static JTextArea logArea;
    private static JFrame frame;

    private static JLabel ppStatusLabel;
    private static JLabel oscStatusLabel;
    private static boolean firstInit = true;

    private static final String VERSION = "v0.3";

    public static ProPresManager proPresManager = new ProPresManager(null);
    public static ResolumeManager resolumeManager = new ResolumeManager(null);

    public static void main(String... args){

        try {
           UIManager.setLookAndFeel(new DarculaLaf());

        } catch (UnsupportedLookAndFeelException e) {
            ConvexLogger.log("ERROR", "Could not load Darcula LAF...");
        }

        System.setProperty("apple.laf.useScreenMenuBar", "true");
        System.setProperty("com.apple.mrj.application.apple.menu.about.name", "Convex Translator");


        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;

        //init frame

        frame = new JFrame("convex translator " + VERSION + " -- by @jepayne_");
        frame.setLayout(new GridBagLayout());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(500,600);
        frame.setResizable(true);

        Desktop desktop = Desktop.getDesktop();

        desktop.setAboutHandler(e ->
                JOptionPane.showMessageDialog(frame, "convex translator " + VERSION + "\n---\n\nreport bugs to jesse@convexcreative.com\n\nxx with love -jesse", "about", JOptionPane.PLAIN_MESSAGE)
        );

        final String JETBRAINS_AWT_WINDOW_DARK_APPEARANCE = "jetbrains.awt.windowDarkAppearance";
        frame.getRootPane().putClientProperty(JETBRAINS_AWT_WINDOW_DARK_APPEARANCE, true);

        //init main panel

        JPanel mainPanel = new JPanel(new GridBagLayout());
        frame.add(mainPanel,gbc);

        // status leds

        JPanel statusPanel = new JPanel(new GridBagLayout());
        statusPanel.setBorder(BorderFactory.createRaisedSoftBevelBorder());

        ppStatusLabel = new JLabel("---pp");
        oscStatusLabel = new JLabel("---osc");

        gbc.gridx = 0;
        gbc.gridy = 0;

        updateStatus("pp", false);
        updateStatus("osc", false);


        mainPanel.add(new JLabel("Status"),gbc);

        gbc.gridy = 1;

        statusPanel.add(ppStatusLabel, gbc);

        gbc.gridy = 2;

        statusPanel.add(oscStatusLabel, gbc);

        gbc.gridy = 1;
        gbc.gridx = 0;

        mainPanel.add(statusPanel, gbc);


        // ---


        hostTextArea = new JTextArea("\nLoad file to view project info...\n");
        hostTextArea.setSize(300,300);
        hostTextArea.setEditable(false);

        mainPanel.add(new JLabel("Project Info\n"), gbc);
        gbc.gridy = 2;

        mainPanel.setBorder(BorderFactory.createLoweredSoftBevelBorder());

        mainPanel.add(hostTextArea, gbc);

        // BUTTONS INIT --------------

        JPanel buttonPanel = new JPanel(new GridBagLayout());

        gbc.gridy = 3;
        gbc.gridx = 0;
        mainPanel.add(buttonPanel, gbc);

        // load project button
        gbc.gridx = 0;
        gbc.gridy = 3;

        JButton loadProjectButton = new JButton("Load Project");
        loadProjectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                JFileChooser chooser = new JFileChooser();
                chooser.setFileFilter(new ConvexFileFilter());
                int returnVal = chooser.showOpenDialog(frame);
                if(returnVal == JFileChooser.APPROVE_OPTION){
                    File file = chooser.getSelectedFile();
                    loadFile(file);
                }
            }
        });
        buttonPanel.add(loadProjectButton,gbc );

        // generate project button

        gbc.gridx = 1;
        gbc.gridy = 3;

        JButton generateProjectButton = new JButton("Generate Example Project");
        generateProjectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser chooser = new JFileChooser();
                chooser.setFileFilter(new ConvexFileFilter());
                int returnVal = chooser.showSaveDialog(frame);
                if(returnVal == JFileChooser.APPROVE_OPTION){
                    File file = chooser.getSelectedFile();

                    if(!file.getName().toLowerCase().endsWith(".cjson")){
                        file = new File(file.getPath() + ".cjson");
                    }

                    generateFile(file);
                }


            }
        });

        buttonPanel.add(generateProjectButton, gbc);


        // ------------------------------------------

        // init logger

        logArea = new JTextArea();
        logArea.setText("--");
        logArea.setSize(300,300);
        logArea.setEditable(false);

        ConvexLogger.registerTask(new LoggerEvent() {
            @Override
            public void run(String s) {
                logArea.setText(s);
            }
        });



        gbc.gridy = 4;
        gbc.gridx = 0;
        mainPanel.add(new JLabel("Log\n"), gbc);
        gbc.gridy = 5;

        mainPanel.add(logArea, gbc);

        frame.setVisible(true);

    }

    public static void updateStatus(String type, boolean connection){
        if(type.equalsIgnoreCase("pp")){

            if(connection){
                ppStatusLabel.setForeground(Color.GREEN);
                ppStatusLabel.setText("ProPres: CONNECTED.");
            }else{
                ppStatusLabel.setForeground(Color.RED);
                ppStatusLabel.setText("ProPres: NOT CONNECTED.");
            }

        }else if(type.equalsIgnoreCase("osc")){

            if(connection){
                oscStatusLabel.setForeground(Color.GREEN);
                oscStatusLabel.setText("OSC: RUNNING.");
            }else{
                oscStatusLabel.setForeground(Color.RED);
                oscStatusLabel.setText("OSC: NOT RUNNING.");
            }


        }
    }

    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static void loadFile(File file){

        Scanner input = null;
        try {
            input = new Scanner(file);
        } catch (FileNotFoundException e) {
            ConvexLogger.log("CT","File not found...");
        }

        String text = "";

        while(input.hasNext()) {
            text+=input.nextLine();
        }

        input.close();
        ProjectData data = GSON.fromJson(text, ProjectData.class);


        // LOAD SERVERS


        resolumeManager.setProjectData(data);
        proPresManager.setProjectData(data);

        proPresManager.updateInstance();


        if(firstInit){
            resolumeManager.initialize();
            proPresManager.initialize();
            firstInit = false;
        }

        resolumeManager.connect();
        proPresManager.connect();

        //----------------------

        String fileInfo = "\n";

        fileInfo += "Project Name: " + data.getProjectName();
        fileInfo += "\nProject Path: " + data.getSavePath();
        fileInfo += "\n";
        fileInfo += "\nProPres Server Host:  ws://" + data.getProHost() + ":" + data.getProPort() + "/stagedisplay";
        fileInfo += "\nProPres Pass: " + data.getProPassword();
        fileInfo += "\n";
        fileInfo += "\nOSC Server Host: " + data.getOscHost() + ":" + data.getOscPort();
        fileInfo += "\n";

        if(data.getClips().size() < 1){
            fileInfo += "\nNo clips in this project...?";
        }else{
            fileInfo += "\nClips:";

            int i = 1;
            for(ClipData clip : data.getClips()){
                fileInfo += "\n";
                fileInfo += "\n" + i + " ---- ";
                fileInfo += "\n* Layer/Clip: " + clip.getLayer() + "/" + clip.getClip();
                fileInfo += "\n* Clip Type: " + clip.getType().getFormattedName();
                fileInfo += "\n* Remove Linebreaks: " + clip.removeLinebreaks();
                i++;
            }

        }

        hostTextArea.setText(fileInfo);

    }

    public static void saveFile(File toSave){

    }

    public static void generateFile(File file){

        ProjectData exampleData = new ProjectData(file.getPath());

        String json = GSON.toJson(exampleData);

        FileWriter myWriter = null;
        try {
            myWriter = new FileWriter(file);
            myWriter.write(json);
            myWriter.close();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(frame,
                    "ERROR", "Could not generate file... (" + e.getMessage() + ")",
                    JOptionPane.WARNING_MESSAGE);
            e.printStackTrace();
        }

        loadFile(file);
    }

}
