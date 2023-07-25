// Simcenter STAR-CCM+ macro: InvalidCells2.java
// Written by Simcenter STAR-CCM+ 15.04.008
package feaf;

import star.common.*;
import star.base.neo.*;

public class RemoveInvalidCells extends StarMacro {

    @Override
    public void execute() {
        execute0();
    }

    private void execute0() {

        Simulation sim = getActiveSimulation();

        MeshManager meshManager = sim.getMeshManager();
        Region region = sim.getRegionManager().getRegion("Air");
        meshManager.removeInvalidCells(new NeoObjectVector(new Object[]{region}), NeoProperty.fromString(""
                + "{\'minimumContiguousFaceArea\': 1E-12,"
                + " \'minimumCellVolumeEnabled\': true,"
                + " \'minimumVolumeChangeEnabled\': true,"
                + " \'functionOperator\': 0,"
                + " \'minimumContiguousFaceAreaEnabled\': true,"
                + " \'minimumFaceValidityEnabled\': true,"
                + " \'functionValue\': 0.0,"
                + " \'functionEnabled\': false,"
                + " \'function\': \'\',"
                + " \'minimumVolumeChange\': 1.0E-4,"
                + " \'minimumCellVolume\': 0.0,"
                + " \'minimumCellQualityEnabled\': true,"
                + " \'minimumCellQuality\': 1.0E-4,"
                + " \'minimumDiscontiguousCells\': 2000,"
                + " \'minimumDiscontiguousCellsEnabled\': true,"
                + " \'minimumFaceValidity\': 0.75}"));
        meshManager.splitNonContiguousRegions(new NeoObjectVector(new Object[]{region}), 1E-12);
        PhysicsContinuum physics = ((PhysicsContinuum) sim.getContinuumManager().getContinuum("RANS-SST"));
        
        for (Region reg : sim.getRegionManager().getRegions()) {
            for (int i = 1; i <= sim.getRegionManager().getRegions().size(); i++) {
                if (reg.getPresentationName().equals(region.getPresentationName() + " " + Integer.toString(i))) {
                    physics.erase(reg);
                }
            }
        }
    }
}
