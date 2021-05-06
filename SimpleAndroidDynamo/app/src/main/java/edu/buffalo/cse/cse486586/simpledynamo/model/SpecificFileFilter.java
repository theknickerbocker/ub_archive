package edu.buffalo.cse.cse486586.simpledynamo.model;

import java.io.File;
import java.io.FilenameFilter;

/**
 * Created by kevinrathbun on 4/12/18.
 */

public class SpecificFileFilter implements FilenameFilter{

    private final String selection;

    public SpecificFileFilter(String selection) {
        this.selection = selection;
    }

    @Override
    public boolean accept(File file, String s) {
        return s.equals(selection);
    }
}
