// Simcenter STAR-CCM+ macro: RunAdjoint.java
// Written by Simcenter STAR-CCM+ 16.01.070-2-g06755fe
package macro;

import java.util.*;

import star.common.*;
import star.base.neo.*;
import java.io.*;
import star.base.report.*;
import star.vis.*;
import star.flow.*;
import star.energy.*;
import star.automation.*;

public class ComputeSensitivities extends StarMacro {

  public void execute() {
    execute0();
  }

  private void execute0() {

    Simulation simulation_0 = getActiveSimulation();

    // Run the Adjoint solver directly
    AdjointSolver adjointSolver_0 =  ((AdjointSolver) simulation_0.getSolverManager().getSolver(AdjointSolver.class));

    // Compute Surface sensitivity for desired report cost functions
    if (adjointSolver_0 != null) {
        adjointSolver_0.runAdjoint();
        adjointSolver_0.computeSurfaceSensitivity();
    }

    // Print sensitivities
    GlobalParameterManager paramManager = simulation_0.get(GlobalParameterManager.class);
    AdjointCostFunctionManager manager = simulation_0.get(AdjointCostFunctionManager.class);

    for (NamedObject parameter : paramManager.getObjects()) {
        if (parameter instanceof ScalarGlobalParameter) {
            for (AdjointCostFunction cost : manager.getObjects()) {        
                if (adjointSolver_0.isGeometricParameterSensitivityAvailable(cost, (ScalarGlobalParameter) parameter)) {
                    double sens = adjointSolver_0.getGeometricParameterSensitivityValue(cost, (ScalarGlobalParameter) parameter);
                    simulation_0.println("Sens for " + cost.getPresentationName() + " w.r.t " + parameter.getPresentationName() + ": " + sens);
                }
            }
        }
    }

  }
}
