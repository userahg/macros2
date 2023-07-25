// Simcenter STAR-CCM+ macro: Test.java
// Written by Simcenter STAR-CCM+ 18.02.008
package macro;

import java.util.*;

import star.common.*;
import star.base.neo.*;
import star.prismmesher.*;
import star.meshing.*;

public class Test extends StarMacro {

  public void execute() {
    execute0();
  }

  private void execute0() {

    Simulation simulation_0 = 
      getActiveSimulation();

    AutoMeshOperation autoMeshOperation_0 = 
      ((AutoMeshOperation) simulation_0.get(MeshOperationManager.class).getObject("Automated Mesh"));

    PrismAutoMesher prismAutoMesher_0 = 
      ((PrismAutoMesher) autoMeshOperation_0.getMeshers().getObject("Prism Layer Mesher"));

    prismAutoMesher_0.setGapFillPercentage(20.0);

    prismAutoMesher_0.setMinimumThickness(15.0);

    prismAutoMesher_0.setLayerChoppingPercentage(55.0);

    prismAutoMesher_0.setBoundaryMarchAngle(55.0);

    prismAutoMesher_0.setConcaveAngleLimit(10.0);

    prismAutoMesher_0.setConvexAngleLimit(350.0);

    prismAutoMesher_0.setNearCoreLayerAspectRatio(0.5);
  }
}
