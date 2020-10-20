package com.convexcreative.pposcinterpreter.cmdonly;

import javax.swing.filechooser.FileFilter;
import java.io.File;

public class ConvexFileFilter extends FileFilter {
    @Override
    public boolean accept(File f) {
        if(f.isDirectory()){
            return true;
        }
        return f.getName().toLowerCase().endsWith(".cjson");
    }

    @Override
    public String getDescription() {
        return "Convex Translator Project File (.cjson)";
    }
}
