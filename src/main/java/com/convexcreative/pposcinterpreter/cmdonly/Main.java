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
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.io.*;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Scanner;

public class Main {

    private static JTextArea logArea;
    private static JFrame frame;

    private static JPanel mainPanel;
    private static JPanel tilePanel;

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


        // INIT FRAME

        frame = new JFrame("convex translator " + VERSION + " -- by @jepayne_");
        frame.setLayout(new GridBagLayout());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(500,550);
        frame.setMinimumSize(new Dimension(500,550));
        frame.setResizable(true);

        Desktop desktop = Desktop.getDesktop();

        desktop.setAboutHandler(e ->
                JOptionPane.showMessageDialog(frame, "convex translator " + VERSION + "\n---\n\nreport bugs to jesse@convexcreative.com\n\nxx with love -jesse", "about", JOptionPane.PLAIN_MESSAGE)
        );

        final String JETBRAINS_AWT_WINDOW_DARK_APPEARANCE = "jetbrains.awt.windowDarkAppearance";
        frame.getRootPane().putClientProperty(JETBRAINS_AWT_WINDOW_DARK_APPEARANCE, true);

        //init main panel & secondary panel

        mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setMinimumSize(new Dimension(500,550));
        mainPanel.setSize(500,550);
       // mainPanel.setBorder(BorderFactory.createLoweredSoftBevelBorder());

        GridBagConstraints mainConstraint = constraint(0,0);

        frame.add(mainPanel, mainConstraint);


        JPanel mainStatusPanel = buildMainStatusPanel();
        mainStatusPanel.setMaximumSize(new Dimension(250,500));
        mainStatusPanel.setSize(new Dimension(250,500));

        mainPanel.add(mainStatusPanel, constraint(0,0,GridBagConstraints.NORTH));
        mainPanel.add(buildMainProjectPanel(), constraint(1,0, GridBagConstraints.NORTH));


        //mainPanel.setBorder(BorderFactory.createLoweredSoftBevelBorder());

        frame.pack();
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

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 0;
        gbc.gridx = 0;
        gbc.anchor = GridBagConstraints.NORTHWEST;

        // GENERATE CLIP SQUARES

        if(tilePanel != null) {
            tilePanel.removeAll();
        }

        for(ClipData clip : data.getClips()){

           gbc.gridy++;

            tilePanel.add(generateClipTile(clip),gbc);
        }

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

    public static JPanel generateClipTile(ClipData clip){

        GridBagConstraints gbc = new GridBagConstraints();

        gbc.anchor = GridBagConstraints.WEST;

        JPanel tile = new JPanel(new GridBagLayout());
        tile.setBorder(BorderFactory.createRaisedSoftBevelBorder());

        final Dimension SIZE = new Dimension(120,115);

        tile.setMinimumSize(SIZE);
        tile.setMaximumSize(SIZE);
        tile.setSize(SIZE);

        gbc.gridx = 0;
        gbc.gridy = 0;
        tile.add(textField( "Layer/Clip: " + clip.getLayer() + "/" + clip.getClip(), Color.CYAN), gbc);

        gbc.gridy = 1;
        tile.add(textField("---------------------------"), gbc);

        gbc.gridy = 2;
        tile.add(textField("Clip Type: " + clip.getType().getFormattedName()), gbc);

        gbc.gridy = 3;
        tile.add(textField("Line Break Str: \"" + clip.getBreakLineString() + "\""), gbc);

        gbc.gridy = 4;
        tile.add(textField("Text Transform: " + clip.getTextTransform()),gbc);

        gbc.gridy = 5;
        tile.add(textField("Repeat: " + (clip.getRepeat() == 0 ? "NO" : clip.getRepeat())),gbc);

        return tile;
    }

    public static JLabel textField(String str, Color... color){
        JLabel field = new JLabel(str);

        if(color == null){
            return field;
        }

        if(color.length > 0){
            field.setForeground(color[0]);
        }

        return field;

    }

    public static JLabel strikeThruField(String str, Color... color){
        JLabel label = textField(str, color);
        return label;

    }


    public static GridBagConstraints constraint(int x, int y, Integer... anchor){

        GridBagConstraints constraint = new GridBagConstraints();

        if(anchor != null){
            if(anchor.length > 1) {
                constraint.anchor = anchor[0];
            }
        }

        constraint.gridx = x;
        constraint.gridy = y;

        return constraint;

    }


    // PRIMARY UI BUILDER METHODS

    public static JPanel buildMainStatusPanel(){

        JPanel statusPanel = new JPanel(new GridBagLayout());

        //statusPanel.setBorder(BorderFactory.createLoweredSoftBevelBorder());

        statusPanel.setSize(250,500);
        statusPanel.setMinimumSize(new Dimension(250,500));

        statusPanel.add(buildStatusPanel(), constraint(0,0, GridBagConstraints.NORTH));
        statusPanel.add(buildLoggerPanel(),  constraint(0,1, GridBagConstraints.NORTH));

        return statusPanel;

    }

    public static JPanel buildMainProjectPanel(){

        JPanel projectPanel = new JPanel(new GridBagLayout());

      //  projectPanel.setBorder(BorderFactory.createRaisedSoftBevelBorder());


        tilePanel = buildTilePanel();
        JScrollPane tileScroller = new JScrollPane(tilePanel);

        tileScroller.setMaximumSize(new Dimension(250,500));
        tileScroller.setMinimumSize(new Dimension(250,500));
        tileScroller.setSize(new Dimension(250,500));

        projectPanel.add(tileScroller, constraint(0,0, GridBagConstraints.NORTH));
        projectPanel.add(buildButtonPanel(), constraint(0,1, GridBagConstraints.NORTH));

        return projectPanel;
    }

    // SECONDARY UI BUILDER METHODS

    public static JPanel buildTilePanel(){

        JPanel panel = new JPanel(new GridBagLayout());

        panel.setMinimumSize(new Dimension(250,500));
        panel.setSize(new Dimension(250,500));

        GridBagConstraints constraints = constraint(0,0);
        constraints.anchor = GridBagConstraints.CENTER;

        constraints.ipady = 500;

        panel.add(textField("                  Load project\nto view info.                  ", Color.GRAY), constraints);

        return panel;

    }

    public static JPanel buildStatusPanel(){

        JPanel statusPanel = new JPanel(new GridBagLayout());
        statusPanel.setBorder(BorderFactory.createRaisedSoftBevelBorder());


        ppStatusLabel = textField("[cur pp status]", Color.RED);
        oscStatusLabel = textField("[cur osc status]", Color.RED);

        updateStatus("pp", false);
        updateStatus("osc", false);

        statusPanel.add(new JLabel("Current Status"),constraint(0,0));
        statusPanel.add(ppStatusLabel, constraint(0,1));
        statusPanel.add(oscStatusLabel, constraint(0,2));

        return statusPanel;

    }

    public static JPanel buildButtonPanel(){

        JPanel buttonPanel = new JPanel(new GridBagLayout());


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

        buttonPanel.add(loadProjectButton,constraint(0,0, GridBagConstraints.CENTER) );

        // generate project button

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

        buttonPanel.add(generateProjectButton, constraint(1,0, GridBagConstraints.CENTER));
        return buttonPanel;

    }

    public static JScrollPane buildLoggerPanel(){


        logArea = new JTextArea();
        logArea.setEditable(false);


        JScrollPane scrollPane = new JScrollPane(logArea);

        logArea.setText("---");


        scrollPane.setMinimumSize(new Dimension(250,300));
        scrollPane.setMaximumSize(new Dimension(250,300));
        scrollPane.setSize(250,300);


        ConvexLogger.registerTask(new LoggerEvent() {
            @Override
            public void run(String s) {
                logArea.setText(s);
                scrollPane.getVerticalScrollBar().setValue(scrollPane.getMaximumSize().width);
            }
        });

        logArea.setMinimumSize(new Dimension(250,300));
        logArea.setMaximumSize(new Dimension(250,300));
        logArea.setSize(250,300);

        //loggerPanel.setMaximumSize(new Dimension(250,300));
        //loggerPanel.setMinimumSize(new Dimension(250,300));
        //loggerPanel.setSize(new Dimension(250,300));

        //loggerPanel.add(textField("Log"), constraint(0,0, GridBagConstraints.NORTH));
        //loggerPanel.add(scrollPane, constraint(0,1, GridBagConstraints.NORTH));

        return scrollPane;
    }


}
