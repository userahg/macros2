
import java.io.File;
import star.common.RootObject;
import star.common.Simulation;
import star.common.StarMacro;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author cd8unu
 */
public class SaveRootObject extends StarMacro{

    @Override
    public void execute() {
        RootObject rootObject = getActiveRootObject();
        rootObject.saveState(rootObject.getSessionDir() + File.separator + rootObject.getPresentationName() + getExtension(rootObject));
    }
    
    private String getExtension(RootObject obj) {
        if (obj instanceof Simulation) {
            return ".sim";
        } else {
            return ".dmprj";
        }
    }
    
}
