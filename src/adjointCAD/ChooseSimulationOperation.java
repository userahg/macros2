/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package adjointCAD;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import star.automation.SimDriverWorkflow;
import star.automation.SimDriverWorkflowManager;
import star.common.GlobalParameterManager;
import star.common.PhysicsContinuum;
import star.common.Region;
import star.common.ScalarGlobalParameter;
import star.common.Simulation;
import star.common.SolvePhysics;
import star.common.StarMacro;

/**
 *
 * @author cd8unu
 */
public class ChooseSimulationOperation extends StarMacro {

    Simulation _sim;
    final String[] _opNames = new String[]{"PrimalOnly", "PrimalAndAdjoint"};
    final String _paramName = "Adj";
    final String _adjOrderParamName = "TurbOrder";
    final String _flowOrderParamName = "FlowOrder";

    SimDriverWorkflowManager _workflowMan;
    ArrayList<SimDriverWorkflow> _workflows = new ArrayList<>();
    ScalarGlobalParameter _adjParam;
    ScalarGlobalParameter _adjOrderParam;
    ScalarGlobalParameter _flowOrderParam;

    @Override
    public void execute() {
        _sim = getActiveSimulation();
        try {
            init();
            activateWorkflow();
        } catch (Exception ex) {
            print(ex);
        }
    }
    
    private void activateWorkflow() {
        boolean frozenTurb = _adjOrderParam.getQuantity().getRawValue() < 0.5;
        int flowOrder = (int) _flowOrderParam.getQuantity().getRawValue();
        int adjOrder = (int) _adjOrderParam.getQuantity().getRawValue();
        PhysicsContinuum physics = getPhysics(frozenTurb, flowOrder, adjOrder);
        Region domain = _sim.getRegionManager().getRegion("Domain");
        physics.add(domain);
        double paramValue = _adjParam.getQuantity().getRawValue();
        int index = (int) java.lang.Math.floor(paramValue);
        SimDriverWorkflow workflow = _workflows.get(index);
        SolvePhysics solvePhysics = (SolvePhysics) workflow.getBlocks().getObject("Solve Physics");
        solvePhysics.getSimulationObjects().setQuery(null);
        solvePhysics.getSimulationObjects().setObjects(physics);
        _workflowMan.setSelectedWorkflow(workflow);
        workflow.reset();
    }

    private void init() throws Exception {
        _workflowMan = _sim.get(SimDriverWorkflowManager.class);
        for (String s : _opNames) {
            boolean found = false;
            for (SimDriverWorkflow workflow : _workflowMan.getObjects()) {
                if (workflow.getPresentationName().equals(s)) {
                    found = true;
                    _workflows.add(workflow);
                }
            }
            if (!found) {
                throw new Exception("No SimDriverWorkflow " + s + " found!");
            }            
        }
        GlobalParameterManager man = _sim.get(GlobalParameterManager.class);
        _adjParam = (ScalarGlobalParameter) man.getObject(_paramName);
        _adjOrderParam = (ScalarGlobalParameter) man.getObject(_adjOrderParamName);
        _flowOrderParam = (ScalarGlobalParameter) man.getObject(_flowOrderParamName);        
    }
    
    private PhysicsContinuum getPhysics(boolean frozenTurb, int flowOrder, int adjOrder) {
        String name;
        if (frozenTurb) {
            name = "FrozenTurb_Flow";
            name += (flowOrder == 1) ? "1st" : "2nd";
        } else {
            name = "AdjTurb_Turb";
            name += (adjOrder == 1) ? "1st_Flow" : "2nd_Flow";
            name += (flowOrder == 1) ? "1st" : "2nd";
        }
        PhysicsContinuum continuum = (PhysicsContinuum) _sim.getContinuumManager().getContinuum(name);
        return continuum;        
    }

    private void print(Exception ex) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        ex.printStackTrace(pw);
        _sim.println(sw);
    }

}
