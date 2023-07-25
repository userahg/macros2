// Simcenter STAR-CCM+ macro: Test1.java
// Written by Simcenter STAR-CCM+ 17.06.007
package macro;

import java.util.*;

import star.solidstress.*;
import star.common.*;
import star.base.neo.*;

public class Test1 extends StarMacro {

  public void execute() {
    execute0();
  }

  private void execute0() {

    Simulation simulation_0 = 
      getActiveSimulation();

    FiniteElementStressSolver finiteElementStressSolver_0 = 
      ((FiniteElementStressSolver) simulation_0.getSolverManager().getSolver(FiniteElementStressSolver.class));

    finiteElementStressSolver_0.setFrozen(true);

    finiteElementStressSolver_0.setFrozen(false);
  }
}
