package com.convexcreative.pposcinterpreter.manager;

import com.convexcreative.ezlogger.ConvexLogger;
import com.convexcreative.pposcinterpreter.cmdonly.Main;
import com.convexcreative.pposcinterpreter.obj.ProjectData;
import com.illposed.osc.OSCMessage;
import com.illposed.osc.OSCSerializeException;
import com.illposed.osc.transport.udp.OSCPortOut;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class ResolumeManager extends ConvexServerConnection {

    private static OSCPortOut portOut = null;

    public ResolumeManager(ProjectData projectData) {
        super(projectData);
    }

    @Override
    public void initialize() {

    }

    @Override
    public void connect() {

        InetSocketAddress addy = new InetSocketAddress(getProjectData().getOscHost(), Integer.parseInt(getProjectData().getOscPort()));

        if(getStatus()){
            disconnect();
        }

        ConvexLogger.log("OSC", "Loading OSC server... + (" + addy.toString() + ")");
        try {
            portOut = new OSCPortOut(addy);
        } catch (IOException e) {
            ConvexLogger.log("[OSC]", "Could not open OSC socket... (" + e.getMessage() + ")");
            Main.updateStatus("osc", false);
            return;
        }
        ConvexLogger.log("OSC", "OSC Server successfully initialized!");
        setStatus(true);
        Main.updateStatus("osc", true);
    }

    @Override
    public void disconnect() {
        if(!getStatus() || portOut == null){
            return;
        }

        try {
            portOut.close();
        } catch (IOException e) {
            ConvexLogger.log("OSC", "Error closing OSC server... (" + e.getMessage() + ")");
            return;
        }

        setStatus(false);
        Main.updateStatus("osc", false);


    }

    public void sendOSCMessage(String msg, ArrayList<String> text){

        if(!getStatus()){
            ConvexLogger.log("OSC", "Error sending OSC data... (" + "not connected to a server)");
        }

         final OSCMessage oscMSG = new OSCMessage(msg, text);

         ConvexLogger.log("OSC", "COMPILED MSG --> " + msg);


         try {
             portOut.send(oscMSG);
         } catch (IOException e) {
             ConvexLogger.log("OSC", "Error sending OSC data... (" + e.getMessage() + ")");
             Main.updateStatus("osc", false);

         } catch (OSCSerializeException e) {
             ConvexLogger.log("OSC", "Error sending OSC data... (" + e.getMessage() + ")");
             Main.updateStatus("osc", false);

         }

    }

}
