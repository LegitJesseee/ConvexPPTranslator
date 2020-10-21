package com.convexcreative.pposcinterpreter.manager;

import com.convexcreative.ezlogger.ConvexLogger;
import com.convexcreative.pposcinterpreter.cmdonly.Main;
import com.convexcreative.pposcinterpreter.enu.ClipType;
import com.convexcreative.pposcinterpreter.obj.ClipData;
import com.convexcreative.pposcinterpreter.obj.ProjectData;
import com.convexcreative.propres.ProPresAPI;
import com.convexcreative.propres.ProPresAPIConfig;
import com.convexcreative.propres.event.server.ServerConnectEvent;
import com.convexcreative.propres.event.server.ServerDisconnectEvent;
import com.convexcreative.propres.event.slide.SlideChangeEvent;
import com.convexcreative.propres.serializable.Slide;
import com.illposed.osc.OSCMessage;
import com.illposed.osc.OSCSerializeException;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class ProPresManager extends ConvexServerConnection {

    private ProPresAPI api;

    public ProPresManager(ProjectData projectData) {
        super(projectData);
    }

    @Override
    public void initialize() {

        api.registerRecurringEvent(new SlideChangeEvent() {
            @Override
            public void run() {

                Slide s = (Slide) getEventMetadata()[0];

                ProPresAPI.log("MSG RECEIVED: " + s.getSlideText());

                for(ClipData clip : getProjectData().getClips()){

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
                    Main.resolumeManager.sendOSCMessage(msg, textArray);
            }
        }
        });

        api.registerRecurringEvent(new ServerDisconnectEvent() {
            @Override
            public void run() {
                setStatus(false);
                Main.updateStatus("pp", false);
                int code = (int) getEventMetadata()[1];
                if(code == 1006){
                    ConvexLogger.log("ProPres Client", "Disconnected from remote host. Reinitializing server... (1006)");
                    connect();
                }
            }
        });

        api.registerRecurringEvent(new ServerConnectEvent() {
            @Override
            public void run() {
                Main.updateStatus("pp", true);
                setStatus(true);
            }
        });
    }

    @Override
    public void connect() {

        ConvexLogger.log("ProPres", "Attempting connection to ProPres server...");

        //close other connections
        if(getStatus()){
            disconnect();
        }

        TimerTask timerTask = new TimerTask(){

            @Override
            public void run() {
                if(!getStatus()){
                    ConvexLogger.log("ProPres", "Could not connect to ProPres server... (timeout)");
                }
            }

        };

        Timer timer = new Timer();
        timer.schedule(timerTask, 1000*5);


        // start new connection
        api.openConnection();

    }

    @Override
    public void disconnect() {
        api.closeConnection();
    }

    public void updateInstance(){
        ProPresAPIConfig config = new ProPresAPIConfig( getProjectData().getProHost() + ":" + getProjectData().getProPort(), getProjectData().getProPassword(), ProPresAPIConfig.V6);
        api = ProPresAPI.getInstance(config);
    }
}
