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
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

public class ProPresManager extends ConvexServerConnection {

    private ProPresAPI api;
    private ProPresAPIConfig config;

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
                    StringBuilder stringBuilder = new StringBuilder();

                    //if the slide is blank, just send a blank message instead of processing
                    if(s.getSlideText().replaceAll(" ","").equalsIgnoreCase("")) {

                        textArray.add("");
                        Main.resolumeManager.sendOSCMessage(msg, textArray);


                    }


                        // bring each line into the stringbuilder with the breakline string between
                    Arrays.stream(s.getSplitSlideText()).forEach(line -> stringBuilder.append(line + clip.getBreakLineString()));

                    //delete extra breakline at end
                    stringBuilder.delete(stringBuilder.length() - clip.getBreakLineString().length(), stringBuilder.length());

                    //repeat string
                    if(clip.getRepeat() > 0){

                        for(int i = 0; i < clip.getRepeat(); i++){
                            stringBuilder.append(clip.getBreakLineString() + stringBuilder.toString());
                        }

                    }

                    //transform string
                    final String transform = stringBuilder.toString();
                    stringBuilder.setLength(0);


                    switch(clip.getTextTransform()){
                        case ALL_LOWERCASE:
                            stringBuilder.append(transform.toLowerCase());
                            break;
                        case ALL_UPPERCASE:
                            stringBuilder.append(transform.toUpperCase());
                            break;
                        case NONE:
                        default:
                            stringBuilder.append(transform);
                    }

                    textArray.add(stringBuilder.toString());
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
        try {
            api.openConnection(config);
        }catch(IllegalThreadStateException e){
            System.out.println("----ERROR: " + e.getMessage());
            e.printStackTrace();
        }

    }

    @Override
    public void disconnect() {
        api.closeConnection();
    }

    public void updateInstance(){
        config = new ProPresAPIConfig( getProjectData().getProHost() + ":" + getProjectData().getProPort(), getProjectData().getProPassword(), ProPresAPIConfig.V6);
        api = ProPresAPI.getInstance(config);
    }
}
