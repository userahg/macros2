// Simcenter STAR-CCM+ macro: RunSimOp.java
// Written by Simcenter STAR-CCM+ 16.04.007
package eCoolFESS;


import star.common.*;
import star.automation.*;

public class RunSimOp extends StarMacro {

    @Override
    public void execute() {
        Simulation _sim = getActiveSimulation();
        SimDriverWorkflow simOp = ((SimDriverWorkflow) _sim.get(SimDriverWorkflowManager.class).getObject("Coupled Solve"));
        simOp.resume();
    }
}
