// STAR-CCM+ macro: test.java
// Written by STAR-CCM+ 14.06.012
package macro;

import java.util.*;

import star.common.*;
import star.meshing.*;

public class test extends StarMacro {

  public void execute() {
    execute0();
  }

  private void execute0() {

    Simulation simulation_0 = 
      getActiveSimulation();

    MeshPipelineController meshPipelineController_0 = 
      simulation_0.get(MeshPipelineController.class);

    meshPipelineController_0.generateVolumeMesh();
  }
}
