package com.convexcreative.pposcinterpreter.obj;

import com.convexcreative.pposcinterpreter.enu.ClipType;
import com.convexcreative.pposcinterpreter.enu.TextTransform;

public class ClipData {

    public final static int DEF_LAYER = 1;
    public final static int DEF_CLIP  = 1;
    public final static ClipType DEF_TYPE = ClipType.TEXT_ANIMATOR;
    public final static boolean DEF_BREAKLINE = false;
    public final static TextTransform DEF_TEXT_TRANSFORM = TextTransform.NONE;
    public final static int DEF_REPEAT = 0;

    public ClipData(int layer, int clip, ClipType type, boolean removeLinebreaks, TextTransform textTransform, int repeat){
        this.layer = layer;
        this.clip = clip;
        this.type = type;
        this.removeLinebreaks = removeLinebreaks;
        this.textTransform = textTransform;
    }

    public ClipData(){};

    private int layer = DEF_LAYER;
    private int clip = DEF_CLIP;
    private int repeat = DEF_REPEAT;

    private ClipType type = DEF_TYPE;

    private TextTransform textTransform = DEF_TEXT_TRANSFORM;

    private boolean removeLinebreaks = DEF_BREAKLINE;

    public int getLayer(){
        return layer;
    }

    public void setLayer(int layer){
        this.layer = layer;
    }

    public int getClip(){
        return clip;
    }

    public void setClip(int clip){
        this.clip = clip;
    }

    public ClipType getType(){
        return type == null ? DEF_TYPE : type;
    }

    public boolean removeLinebreaks(){
        return removeLinebreaks;
    }

    public TextTransform getTextTransform(){
        return textTransform == null ? textTransform = DEF_TEXT_TRANSFORM : textTransform;
    }

    public int getRepeat(){
        return repeat;
    }

    public void setRepeat(int repeat){
        this.repeat = repeat;
    }
    public void setRemoveLinebreaks(boolean removeLinebreaks){
        this.removeLinebreaks = removeLinebreaks;
    }

    public boolean sameAs(ClipData data){
        return
                data.layer == layer
                && data.clip == clip
                && data.type == type
                && data.removeLinebreaks == true;
    }

}
