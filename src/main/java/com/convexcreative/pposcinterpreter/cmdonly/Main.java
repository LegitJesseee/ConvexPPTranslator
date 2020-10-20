package com.convexcreative.pposcinterpreter.cmdonly;

import com.bulenkov.darcula.DarculaLaf;
import com.convexcreative.ezlogger.ConvexLogger;
import com.convexcreative.ezlogger.LoggerEvent;
import com.convexcreative.pposcinterpreter.enu.ClipType;
import com.convexcreative.pposcinterpreter.obj.ClipData;
import com.convexcreative.pposcinterpreter.obj.ProjectData;
import com.convexcreative.propres.ProPresAPI;
import com.convexcreative.propres.ProPresAPIConfig;
import com.convexcreative.propres.event.server.ServerDisconnectEvent;
import com.convexcreative.propres.event.slide.SlideChangeEvent;
import com.convexcreative.propres.serializable.Slide;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.MalformedJsonException;
import com.illposed.osc.OSCMessage;
import com.illposed.osc.OSCSerializeException;
import com.illposed.osc.transport.udp.OSCPort;
import com.illposed.osc.transport.udp.OSCPortOut;

import javax.swing.*;
import java.awt.*;
import java.awt.desktop.AboutEvent;
import java.awt.desktop.AboutHandler;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Scanner;

public class Main {

    private static JTextArea hostTextArea;
    private static JTextArea logArea;
    private static JFrame frame;
    private static OSCPortOut portOut = null;

    private static boolean firstAPIinit = true;

    private static final String VERSION = "v0.2";


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

        hostTextArea = new JTextArea("\nLoad file to view project info...\n");
        hostTextArea.setSize(300,300);
        hostTextArea.setEditable(false);

        mainPanel.add(new JLabel("Project Info\n"), gbc);
        gbc.gridy = 1;

        mainPanel.setBorder(BorderFactory.createLoweredSoftBevelBorder());
        mainPanel.add(hostTextArea, gbc);

        // BUTTONS INIT --------------

        JPanel buttonPanel = new JPanel(new GridBagLayout());

        gbc.gridy = 2;
        gbc.gridx = 0;
        mainPanel.add(buttonPanel, gbc);

        // load project button
        gbc.gridx = 0;
        gbc.gridy = 2;

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
        gbc.gridy = 2;

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



        gbc.gridy = 3;
        gbc.gridx = 0;
        mainPanel.add(new JLabel("Log\n"), gbc);
        gbc.gridy = 4;

        mainPanel.add(logArea, gbc);

        frame.setVisible(true);
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

        loadOscServer(new InetSocketAddress(data.getOscHost(), Integer.parseInt(data.getOscPort())));

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

        ProPresAPIConfig config = new ProPresAPIConfig( data.getProHost() + ":" + data.getProPort(), data.getProPassword(), ProPresAPIConfig.V6);

        ProPresAPI api = ProPresAPI.getInstance(config);

        if(firstAPIinit){
            api.registerRecurringEvent(new ServerDisconnectEvent() {
                @Override
                public void run() {
                    int code = (int) getEventMetadata()[1];
                    if(code == 1006){
                        ConvexLogger.log("PPAPI", "Disconnected from remote host. Reinitializing server... (1006)");
                        api.openConnection();
                    }
                }
            });
            firstAPIinit = false;
        }

        api.closeConnection();
        api.openConnection();

        api.registerRecurringEvent(new SlideChangeEvent() {
            @Override
            public void run() {

                Slide s = (Slide) getEventMetadata()[0];

               ProPresAPI.log("MSG RECEIVED: " + s.getSlideText());

               for(ClipData clip : data.getClips()){

                   String msg = "/composition/layers/" + clip.getLayer() + "/clips/" + clip.getClip() + "/video/source/";

                   if(clip.getType() == ClipType.TEXT_ANIMATOR){
                       msg += "textgenerator/";
                   }else{
                       msg += "blocktextgenerator/";
                   }

                   msg += "text/params/lines";

                   ArrayList<String> textArray = new ArrayList<>();
                   String text = "";
                   if(!clip.removeLinebreaks()) {
                       for (String sl : s.getSplitSlideText()) {
                           switch (clip.getTextTransform()){
                               case ALL_LOWERCASE:
                                   text+= sl.toLowerCase() + "\n";
                                   break;
                               case ALL_UPPERCASE:
                                   text+= sl.toUpperCase() + "\n";
                                   break;
                               default:
                                   text+= sl + "\n";
                           }
                       }
                   }else{
                       switch (clip.getTextTransform()){
                           case ALL_LOWERCASE:
                               text = s.getSlideText().toLowerCase();
                               break;
                           case ALL_UPPERCASE:
                               text = s.getSlideText().toUpperCase();
                               break;
                           default:
                               text = s.getSlideText();
                       }
                   }

                   if(clip.getRepeat() > 0){
                       for(int i = 0; i < clip.getRepeat(); i++){
                           text = text + text;
                       }
                   }

                   textArray.add(text);

                   final OSCMessage oscMSG = new OSCMessage(msg, textArray);

                   ConvexLogger.log("OSC", "COMPILED MSG --> " + msg);


                   try {
                       portOut.send(oscMSG);
                   } catch (IOException e) {
                       ConvexLogger.log("OSC", "Error sending OSC data... (" + e.getMessage() + ")");
                   } catch (OSCSerializeException e) {
                       ConvexLogger.log("OSC", "Error sending OSC data... (" + e.getMessage() + ")");
                   }
                   ConvexLogger.log("OSC", "Sending data for " + clip.getLayer() + "/" + clip.getClip());
               }

            }
        });




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

    static boolean oscServerRunning = false;
    public static void loadOscServer(InetSocketAddress addy) {

        if (oscServerRunning = true && portOut != null) {
            ConvexLogger.log("OSC", "Closing current OSC server... " + (portOut.getRemoteAddress().toString()));
            try {
                portOut.close();
                oscServerRunning = false;
            } catch (IOException e) {
                ConvexLogger.log("OSC", "Error closing OSC server... (" + e.getMessage() + ")");
                return;
            }

            oscServerRunning = false;

        }

        ConvexLogger.log("OSC", "Loading OSC server... + (" + addy.toString() + ")");
        try {
            portOut = new OSCPortOut(addy);
        } catch (IOException e) {
            ConvexLogger.log("[OSC]", "Could not open OSC socket... (" + e.getMessage() + ")");
            return;
        }
        ConvexLogger.log("OSC", "OSC Server successfully initialized!");
        oscServerRunning = true;

    }


}
