/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package adjointCAD;

import star.common.*;
/**
 *
 * @author cd8unu
 */
public class RunSolverWAdjoint extends StarMacro {

    Simulation _sim;
    
    @Override
    public void execute() {
        _sim = getActiveSimulation();
        ScalarGlobalParameter maxIter = (ScalarGlobalParameter) _sim.get(GlobalParameterManager.class).getObject("MaxIter");
        maxIter.getQuantity().setValue(500.0);
        _sim.getSimulationIterator().run();
        maxIter.getQuantity().setValue(1000.0);
        AdjointSolver adjointSolver = _sim.getSolverManager().getSolver(AdjointSolver.class);
        adjointSolver.runAdjoint();
        adjointSolver.computeSurfaceSensitivity();
    }    
}
