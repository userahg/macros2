package brakeCooling;

import star.common.*;
import star.base.neo.*;

public class RemoveInvalidCells extends StarMacro {

    Simulation _sim;
    String[] region_names = {"Air_rotating", "Air_stationary", "Solid_rotating"};

    @Override
    public void execute() {

        _sim = getActiveSimulation();

        PhysicsContinuum physics = ((PhysicsContinuum) _sim.getContinuumManager().getContinuum("RANS"));
        for (String reg_name : region_names) {
            Region region = _sim.getRegionManager().getRegion(reg_name);
            removeInvalidCells(region);
            for (Region reg : _sim.getRegionManager().getRegions()) {
                if (reg.getPresentationName().startsWith(reg_name + " ")) {
                    physics.erase(reg);
                }

            }
        }
    }

    private void removeInvalidCells(Region r) {
        MeshManager meshManager = _sim.getMeshManager();
        meshManager.removeInvalidCells(new NeoObjectVector(new Object[]{r}), NeoProperty.fromString(""
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
        meshManager.splitNonContiguousRegions(new NeoObjectVector(new Object[]{r}), 1E-12);
    }
}
