package edu.buffalo.cse.cse486586.simpledynamo.model;

import java.io.File;
import java.io.FilenameFilter;

/**
 * Created by kevinrathbun on 4/12/18.
 */

public class GlobalFileFilter implements FilenameFilter {
    @Override
    public boolean accept(File file, String s) {
        return true;
    }
}
