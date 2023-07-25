// STAR-CCM+ macro: Test.java
// Written by STAR-CCM+ 14.04.008

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Vector;
import star.common.*;
import star.base.neo.*;
import star.meshing.*;

public class FixGeom4HondaCase extends StarMacro {
    
    Simulation _sim;
    PartSurface _toSplit;
    String _splitName;
    
    @Override
    public void execute() {
        _sim = getActiveSimulation();
        try {
            getSurfaceToSplit();
            execute0();
        } catch (Exception ex) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            ex.printStackTrace(pw);
            _sim.println(sw.toString());
        }
    }
    
    private void execute0() {
        CompositePart part = ((CompositePart) _sim.get(SimulationPartManager.class).getPart("Part5 2"));
        CadPart cadPart = ((CadPart) part.getChildParts().getPart("FluidBody"));
        Vector<Integer> patches = cadPart.getPartSurfacePatches(_toSplit);
        int min = Integer.MAX_VALUE;
        for (Integer i : patches) {
            if (i < min) {
                min = i;
            }
        }
        cadPart.splitPartSurfaceByPatch(_toSplit, new IntVector(new int[]{min}), "Spar 2");
        PartSurface spar = ((PartSurface) cadPart.getPartSurfaceManager().getPartSurface("Spar"));
        PartSurface sparOutlet2 = ((PartSurface) cadPart.getPartSurfaceManager().getPartSurface("Spar 2"));
        cadPart.combinePartSurfaces(new NeoObjectVector(new Object[]{spar, sparOutlet2}));
    }
    
    private void getSurfaceToSplit() {
        CompositePart part = ((CompositePart) _sim.get(SimulationPartManager.class).getPart("Part5 2"));
        CadPart cadPart = ((CadPart) part.getChildParts().getPart("FluidBody"));
        PartSurface sparOutlet = ((PartSurface) cadPart.getPartSurfaceManager().getPartSurface("Spar Outlet"));
        PartSurface spar = ((PartSurface) cadPart.getPartSurfaceManager().getPartSurface("Spar"));
        
        if (cadPart.getPartSurfacePatches(sparOutlet).size() > 1) {
            _toSplit = sparOutlet;
            _splitName = "Spar Outlet";
            _sim.println("Part Surface: Spar Outlet needs to be split");
        } else {
            _toSplit = spar;
            _splitName = "Spar";
            _sim.println("Part Surface: Spar needs to be split");
        }
    }
}
