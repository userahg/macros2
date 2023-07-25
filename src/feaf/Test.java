// Simcenter STAR-CCM+ macro: Test.java
// Written by Simcenter STAR-CCM+ 15.04.008
package macro;

import java.util.*;

import star.common.*;
import star.base.neo.*;

public class Test extends StarMacro {

  public void execute() {
    execute0();
  }

  private void execute0() {

    Simulation simulation_0 = 
      getActiveSimulation();

    Units units_0 = 
      simulation_0.getUnitsManager().getPreferredUnits(new IntVector(new int[] {0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}));

    MeshManager meshManager_0 = 
      simulation_0.getMeshManager();

    Region region_0 = 
      simulation_0.getRegionManager().getRegion("Air");

    meshManager_0.splitNonContiguousRegions(new NeoObjectVector(new Object[] {region_0}), 1.0E-12);

    PhysicsContinuum physicsContinuum_0 = 
      ((PhysicsContinuum) simulation_0.getContinuumManager().getContinuum("RANS-SST"));

    Region region_1 = 
      simulation_0.getRegionManager().getRegion("Air 2");

    physicsContinuum_0.erase(region_1);
  }
}
