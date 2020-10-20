package com.convexcreative.pposcinterpreter.obj;

import javax.sound.sampled.Clip;
import java.util.ArrayList;

public class ProjectData {

    public static final String DEF_NAME = "Untitled Project";
    public static final String DEF_PATH = "~/Desktop/" + DEF_NAME + ".json";
    public static final String DEF_HOST = "127.0.0.1";
    public static final String DEF_PRO_PORT = "25565";
    public static final String DEF_PRO_PASSWORD = "1234";
    public static final String DEF_OSC_PORT = "25565";

    private static final ArrayList<ClipData> DEF_CLIPS = new ArrayList<>();

    {
        DEF_CLIPS.add(new ClipData());
    }

    private String projectName = DEF_NAME;
    private String savePath = DEF_PATH;

    private String ppHost = DEF_HOST;
    private String ppPort = DEF_PRO_PORT;
    private String ppPassword = DEF_PRO_PASSWORD;

    private String oscHost = DEF_HOST;
    private String oscPort = DEF_OSC_PORT;

    private ArrayList<ClipData> clips = DEF_CLIPS;

    public ProjectData(String ppHost, String ppPort, String ppPassword, String oscHost, String oscPort, ArrayList<ClipData>... clips){
        this.ppHost = ppHost;
        this.ppPort = ppPort;
        this.ppPassword = ppPassword;
        this.oscHost = oscHost;
        this.oscPort = oscPort;

        if(clips == null){
            this.clips = DEF_CLIPS;
        }else{
            this.clips = clips[0];
        }

    }

    public ProjectData(String path){
        this.savePath = path;
    }

    public String getProjectName(){
        return projectName == null ? projectName = DEF_NAME : projectName;
    }

    public void setProjectName(String projectName){
        this.projectName = projectName;
    }

    public String getSavePath(){
        return savePath == null ? savePath = DEF_PATH : savePath;
    }

    public void setSavePath(String path){
        this.savePath = path;
    }

    public String getProHost(){
        return ppHost == null ? ppHost = DEF_HOST : ppHost;
    }

    public void setProHost(String ppHost){
        this.ppHost = ppHost;
    }

    public String getProPort(){
        return ppPort == null ? ppPort = DEF_PRO_PORT : ppPort;
    }

    public void setProPort(String ppPort){
        this.ppPort = ppPort;
    }

    public String getProPassword(){
        return ppPassword == null ? ppPassword = DEF_PRO_PASSWORD : ppPassword;
    }

    public void setProPassword(String ppPassword){
        this.ppPassword = ppPassword;
    }

    public String getOscHost(){
        return oscHost == null ? oscHost = DEF_HOST : oscHost;
    }

    public void setOscHost(String oscHost){
        this.oscHost = oscHost;
    }

    public String getOscPort(){
        return oscPort == null ? oscPort = DEF_OSC_PORT : oscPort;
    }

    public ArrayList<ClipData> getClips(){
        return clips;
    }

    public void setClips(ArrayList<ClipData> clips){
        this.clips = clips;
    }

    public boolean sameAs(ProjectData toCompare){

        if(!(
                toCompare.ppHost == ppHost
                && toCompare.ppPort == ppPort
                && toCompare.ppPassword == ppPassword
                && toCompare.oscHost == oscHost
                && toCompare.oscPort == oscPort
                )){
            return false;
        }

        if(!toCompare.clips.equals(clips)){
            return false;
        }

        return true;

    }



}
