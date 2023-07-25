/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package adjointCAD;

import star.automation.SimDriverWorkflow;
import star.automation.SimDriverWorkflowManager;
import star.common.*;
/**
 *
 * @author cd8unu
 */
public class RunSimOp extends StarMacro {

    Simulation _sim;
    
    @Override
    public void execute() {
        _sim = getActiveSimulation();
        SimDriverWorkflow simOp = (SimDriverWorkflow) _sim.get(SimDriverWorkflowManager.class).getObject("Solve with Adjoint");
        simOp.execute();
    }    
}
