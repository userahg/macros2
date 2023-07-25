// Simcenter STAR-CCM+ macro: UpdateTransform.java
// Written by Simcenter STAR-CCM+ 16.04.007
package feaf;

import star.common.*;
import star.meshing.*;
import star.vis.Displayer;
import star.vis.Scene;

public class UpdateTransform extends StarMacro {

    String[] sceneNames = {"01_Geom_Bottom_View", "01_Geom_Bottom_View_2", "01_Geom_Front_View", "01_Geom_Front_View_2"};
    
    @Override
    public void execute() {
        Simulation _sim = getActiveSimulation();
        TransformPartsOperation transform = ((TransformPartsOperation) _sim.get(MeshOperationManager.class).getObject("Transform"));
        transform.execute();
        
        PartSubRepresentation transformRep = (PartSubRepresentation) ((PartRepresentation) _sim.getRepresentationManager().getObject("Geometry")).getSubRepresentations().getObject("Transform");

        for (String s : sceneNames) {
            Scene scene = _sim.getSceneManager().getScene(s);
            Displayer disp = scene.getDisplayerManager().getObject("Front end");
            disp.setRepresentation(transformRep);
        }
    }
}
