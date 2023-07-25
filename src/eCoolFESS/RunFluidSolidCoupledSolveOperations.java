/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eCoolFESS;

import java.io.File;
import star.automation.SimDriverWorkflow;
import star.automation.SimDriverWorkflowManager;
import star.base.report.Report;
import star.common.Simulation;
import star.common.StarMacro;
import star.segregatedflow.SegregatedFlowSolver;
import star.solidstress.FiniteElementStressSolver;

/**
 *
 * @author cd8unu
 */
public class RunFluidSolidCoupledSolveOperations extends StarMacro {

    Simulation _sim;
    Report _energyBalance;
    
    double maxEnergyImbalance = 15.0;
    int maxLoops = 50;

    @Override
    public void execute() {
        _sim = getActiveSimulation();
        _energyBalance = _sim.getReportManager().getReport("System Energy Balance by Heat Loss");
        

        SegregatedFlowSolver flowSolver = (SegregatedFlowSolver) _sim.getSolverManager().getSolver(SegregatedFlowSolver.class);
        flowSolver.setFreezeFlow(false);
        FiniteElementStressSolver ssSolver = (FiniteElementStressSolver) _sim.getSolverManager().getSolver(FiniteElementStressSolver.class);
        ssSolver.setFrozen(true);

        SimDriverWorkflow initFlowOperation = (SimDriverWorkflow) _sim.get(SimDriverWorkflowManager.class).getObject("Init Flow");
        SimDriverWorkflow updateSolidsOperation = (SimDriverWorkflow) _sim.get(SimDriverWorkflowManager.class).getObject("Update Solids");
        SimDriverWorkflow updateFlowOperation = (SimDriverWorkflow) _sim.get(SimDriverWorkflowManager.class).getObject("Update Flow");
        
        _sim.get(SimDriverWorkflowManager.class).setSelectedWorkflow(initFlowOperation);
        initFlowOperation.reset();
        initFlowOperation.execute();
        flowSolver.setFreezeFlow(true);
        _sim.get(SimDriverWorkflowManager.class).setSelectedWorkflow(updateSolidsOperation);
        updateSolidsOperation.reset();
        updateSolidsOperation.execute();
        
        for (int i = 1; i <= maxLoops; i++) {
            _sim.get(SimDriverWorkflowManager.class).setSelectedWorkflow(updateFlowOperation);
            updateFlowOperation.reset();
            updateFlowOperation.execute();
            _sim.get(SimDriverWorkflowManager.class).setSelectedWorkflow(updateSolidsOperation);
            updateSolidsOperation.reset();
            updateSolidsOperation.execute();
            
            if (i % 5 == 0) {
                _sim.saveState(_sim.getSessionDir() + File.separator + _sim.getPresentationName() + ".sim");
            }
            
            if (Math.abs(_energyBalance.getReportMonitorValue()) < maxEnergyImbalance) {
                break;
            }
        }
        
        ssSolver.setFrozen(false);
        _sim.get(SimDriverWorkflowManager.class).setSelectedWorkflow(updateSolidsOperation);
        updateSolidsOperation.reset();
        updateSolidsOperation.execute();
    }
}
