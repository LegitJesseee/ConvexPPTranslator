package com.convexcreative.pposcinterpreter.data;

import java.util.HashMap;

public class ValueManager {

    private HashMap<String, String> values;

    public ValueManager(){
        values = new HashMap<>();
    }

    public void update(String key, String value){
        values.put(key, value);
    }

    public String get(String key){
        if(!values.containsKey(key)){
            return null;
        }
        return values.get(key);
    }

}
