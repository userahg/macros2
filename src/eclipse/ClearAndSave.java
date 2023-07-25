// Simcenter STAR-CCM+ macro: ClearAndSave.java
// Written by Simcenter STAR-CCM+ 16.01.040
package eclipse;


import java.io.File;
import star.common.*;
import star.meshing.*;

public class ClearAndSave extends StarMacro {

  @Override
  public void execute() {
    Simulation _sim = getActiveSimulation();
    Solution solution = _sim.getSolution();
    solution.clearSolution(Solution.Clear.History, Solution.Clear.Fields, Solution.Clear.LagrangianDem);
    MeshPipelineController mesher = _sim.get(MeshPipelineController.class);
    mesher.clearGeneratedMeshes();
    _sim.saveState(_sim.getSessionDir() + File.separator + _sim.getPresentationName() + ".sim");
  }
}
