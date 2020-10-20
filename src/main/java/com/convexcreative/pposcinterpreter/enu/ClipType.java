package com.convexcreative.pposcinterpreter.enu;

public enum ClipType {

   TEXT_BLOCK("Text Block"),
   TEXT_ANIMATOR("Text Animator");

    String formattedName;

    ClipType(String formattedName){
        this.formattedName = formattedName;
    }

    public String getFormattedName(){
        return formattedName;
    }

}
