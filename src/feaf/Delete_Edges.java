// STAR-CCM+ macro: Delete_Edges.java
// Written by STAR-CCM+ 14.04.011
package feaf;

import java.util.ArrayList;
import star.common.*;

public class Delete_Edges extends StarMacro {

    @Override
    public void execute() {
        execute0();
    }

    private void execute0() {

        Simulation sim = getActiveSimulation();

        CompositePart schabloneT5 = ((CompositePart) sim.get(SimulationPartManager.class).getPart("Schablone_x_t5"));
        CompositePart assemblyBody = ((CompositePart) schabloneT5.getChildParts().getPart("Assembly body"));
        CompositePart bumper = ((CompositePart) schabloneT5.getChildParts().getPart("Bumper_close_only_t4"));
        CompositePart id66xt = ((CompositePart) schabloneT5.getChildParts().getPart("Schablone_id66_x_t"));
        CompositePart id66xt2 = (CompositePart) schabloneT5.getChildParts().getPart("Schablone_id66_x_t 2");

        for (GeometryPart parti : assemblyBody.getChildParts().getParts()) {
            sim.println(parti.getPartCurves().toString());
            parti.deletePartCurves(parti.getPartCurves());
        }

        for (GeometryPart parti : bumper.getChildParts().getParts()) {
            sim.println(parti.getPartCurves().toString());
            parti.deletePartCurves(parti.getPartCurves());
        }

        for (GeometryPart parti : id66xt.getChildParts().getParts()) {
            sim.println(parti.getPartCurves().toString());
            parti.deletePartCurves(parti.getPartCurves());
        }

        for (GeometryPart parti : id66xt2.getChildParts().getParts()) {
            sim.println(parti.getPartCurves().toString());
            parti.deletePartCurves(parti.getPartCurves());
        }
        
        for (Region r : sim.getRegionManager().getRegions()) {
            ArrayList<Region> toDelete = new ArrayList<>();
            if (r.getPresentationName().toLowerCase().contains("cells deleted from air")) {
                toDelete.add(r);
            }
            sim.getRegionManager().removeRegions(toDelete);
        }
    }
}
