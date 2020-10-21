package com.convexcreative.pposcinterpreter.manager;

import com.convexcreative.pposcinterpreter.obj.ProjectData;

import java.net.InetSocketAddress;

public abstract class ConvexServerConnection {

    private boolean connected;
    private ProjectData projectData;

    public ConvexServerConnection(ProjectData projectData){
        this.projectData = projectData;
        this.connected = false;
    }


    public abstract void initialize();

    public abstract void connect();

    public abstract void disconnect();

    public boolean getStatus(){
        return connected;
    }

    public void setStatus(boolean connected){
        this.connected = connected;
    }

    public ProjectData getProjectData(){
        return projectData;
    }

    public void setProjectData(ProjectData projectData){
        this.projectData = projectData;
    }

}
