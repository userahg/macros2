/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ga;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import star.automation.SimDriverWorkflow;
import star.automation.SimDriverWorkflowManager;
import star.common.GlobalParameterManager;
import star.common.ScalarGlobalParameter;
import star.common.SetParameterAutomationBlock;
import star.common.Simulation;
import star.common.StarMacro;
import star.common.StarPlot;
import star.post.RecordedSolutionView;
import star.post.SolutionHistory;
import star.post.SolutionHistoryManager;
import star.post.SolutionRepresentation;
import star.post.SolutionViewManager;

/**
 *
 * @author cd8unu
 */
public class BeforeResults extends StarMacro {

    Simulation _sim;
    SimDriverWorkflowManager _simOpMan;
    SimDriverWorkflow _oc1Workflow;
    SimDriverWorkflow _oc2Workflow;
    SimDriverWorkflow _oc2_2Workflow;
    SimDriverWorkflow _adjWorkflow;
    SolutionHistory _solutionHist;
    RecordedSolutionView _oc1View;
    RecordedSolutionView _oc2View;
    SolutionRepresentation _oc1Rep;
    SolutionRepresentation _oc2Rep;
    final String OC1_PRIMAL = "Primal_Loiter";
    final String OC2_PRIMAL = "Primal_Dash";
    final String OC2_2_PRIMAL = "Primal_Dash2";
    final String ADJ = "RunAdj";
    final String MACRO_MESSAGE_TAG = "BEFORE_RESULTS: ";

    @Override
    public void execute() {
        initialize();
        try {
            if (!oc2HasRun()) {
                runWorkflow(_oc2_2Workflow);
                createSolutionHistorySnapshot("OC2");
                setViewsAndRepresentations();
            }
        } catch (Exception ex) {
            print(ex);
        }
    }

    private void runWorkflow(SimDriverWorkflow workflow) {
        _sim.println(MACRO_MESSAGE_TAG + "runWorkflow method for Operation " + workflow.getPresentationName());
        _simOpMan.setSelectedWorkflow(workflow);
        workflow.reset();
        workflow.execute();
        _simOpMan.setSelectedWorkflow(null);
        printFinalParameterValues(workflow);
        exportResiduals(workflow);
    }

    private void createSolutionHistorySnapshot(String snapshotName) {
        _sim.println(MACRO_MESSAGE_TAG + "createSolutionHistorySnapshot method. Snapshot name = " + snapshotName);
        _solutionHist.createSnapshot(snapshotName);
        _solutionHist.rescanFile();
    }

    private void setViewsAndRepresentations() {
        _sim.println(MACRO_MESSAGE_TAG + "setViewsAndRepresentations");
        _solutionHist.rescanFile();
        _oc1View.setStateName("OC1");
        _oc2View.setStateName("OC2");
    }

    private void printFinalParameterValues(SimDriverWorkflow workflow) {
        _sim.println(MACRO_MESSAGE_TAG + workflow.getPresentationName() + " final parameter values:");
        for (SetParameterAutomationBlock block : getSetParameterOperations(workflow)) {
            HashMap<String, Double> dict = getParameterValue(block);
            for (Entry<String, Double> entry : dict.entrySet()) {
                _sim.println("\t" + MACRO_MESSAGE_TAG + entry.getKey() + ": " + entry.getValue());
            }
        }
    }

    private void exportResiduals(SimDriverWorkflow workflow) {
        _sim.println(MACRO_MESSAGE_TAG + "exportResiduals method for workflow " + workflow.getPresentationName());
        StarPlot residuals = _sim.getPlotManager().getPlot("Residuals");
        StarPlot conv = _sim.getPlotManager().getPlot("Conv");
        residuals.encode(_sim.getSessionDir() + File.separator + "res" + workflow.getPresentationName() + ".png", "png", 1920, 1200, true, false);
        conv.encode(_sim.getSessionDir() + File.separator + "conv" + workflow.getPresentationName() + ".png", "png", 1920, 1200, true, false);
    }

    private ArrayList<SetParameterAutomationBlock> getSetParameterOperations(SimDriverWorkflow workflow) {
        _sim.println(MACRO_MESSAGE_TAG + "getSetParameterOperations method for workflow " + workflow.getPresentationName());
        ArrayList<SetParameterAutomationBlock> blocks = new ArrayList<>();
        for (SetParameterAutomationBlock block : workflow.getBlocks().getObjectsOf(SetParameterAutomationBlock.class)) {
            blocks.add(block);
        }
        return blocks;
    }

    private HashMap<String, Double> getParameterValue(SetParameterAutomationBlock block) {
        _sim.println(MACRO_MESSAGE_TAG + "getParameterValue method for SetParameterAutomationBlock " + block.getPresentationName());
        HashMap<String, Double> dict = new HashMap<>();
        ScalarGlobalParameter param = (ScalarGlobalParameter) block.getParameter();
        dict.put(param.getPresentationName(), param.getQuantity().getRawValue());
        return dict;
    }

    private boolean oc2HasRun() {
        ScalarGlobalParameter dummyConv2 = (ScalarGlobalParameter) _sim.get(GlobalParameterManager.class).getObject("DummyConv2");
        boolean hasRun = dummyConv2.getQuantity().getRawValue() > 0.0;
        return hasRun;
    }

    private void initialize() {
        _sim = getActiveSimulation();
        _simOpMan = _sim.get(SimDriverWorkflowManager.class);
        _oc1Workflow = _simOpMan.getObject(OC1_PRIMAL);
        _oc2Workflow = _simOpMan.getObject(OC2_PRIMAL);
        _oc2_2Workflow = _simOpMan.getObject(OC2_2_PRIMAL);
        _adjWorkflow = _simOpMan.getObject(ADJ);
        _solutionHist = (SolutionHistory) _sim.get(SolutionHistoryManager.class).getObject("solHist");
        _oc1View = (RecordedSolutionView) _sim.get(SolutionViewManager.class).getObject("Loiter");
        _oc2View = (RecordedSolutionView) _sim.get(SolutionViewManager.class).getObject("Dash");
        _oc1Rep = (SolutionRepresentation) _sim.getRepresentationManager().getObject("Loiter");
        _oc1Rep = (SolutionRepresentation) _sim.getRepresentationManager().getObject("Dash");
    }

    private void print(Exception ex) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        ex.printStackTrace(pw);
        _sim.println(pw);
    }
}
