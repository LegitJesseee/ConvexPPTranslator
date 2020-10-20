package com.convexcreative.pposcinterpreter.swing.callback;

import com.convexcreative.pposcinterpreter.Main;
import com.convexcreative.pposcinterpreter.swing.ConvexFrame;

import java.awt.event.ActionListener;

public abstract class ConvexCallback implements ActionListener {

    private Main instance;

    public ConvexCallback(Main instance){
        this.instance = instance;
    }

    public Main getInstance(){
        return instance;
    }

}
