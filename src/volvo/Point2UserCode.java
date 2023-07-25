/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package volvo;

import java.io.File;
import star.common.Simulation;
import star.common.StarMacro;
import star.common.UserLibrary;

/**
 *
 * @author cd8unu
 */
public class Point2UserCode extends StarMacro {

    @Override
    public void execute() {
        Simulation _sim = getActiveSimulation();
        File libRandFF = new File("/data/agency/aarong/projects/volvo/refRun/libRandFF.so");
        File libRandFF2 = new File("/data/agency/aarong/projects/volvo/refRun/libRandFF2.so");
        UserLibrary libRandFFlib = _sim.getUserFunctionManager().getLibrary("libRandFF.so");
        libRandFFlib.setLibraryPath(libRandFF);
        UserLibrary libRandFF2lib = _sim.getUserFunctionManager().getLibrary("libRandFF2.so");
        libRandFF2lib.setLibraryPath(libRandFF2);
    }
    
}
