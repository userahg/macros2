/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ga;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import star.automation.SimDriverWorkflow;
import star.automation.SimDriverWorkflowManager;
import star.base.neo.LongVector;
import star.base.neo.NamedObject;
import star.base.neo.NeoFileUtils;
import star.base.neo.NeoProperty;
import star.base.report.Report;
import star.common.AdjointCostFunction;
import star.common.AdjointCostFunctionManager;
import star.common.AdjointSolver;
import star.common.FixedStepsStoppingCriterion;
import star.common.GlobalParameterBase;
import star.common.GlobalParameterManager;
import star.common.ReportCostFunction;
import star.common.ScalarGlobalParameter;
import star.common.SetParameterAutomationBlock;
import star.common.Simulation;
import star.common.StarMacro;
import star.common.StarPlot;
import star.common.SteadySolver;
import star.meshing.AutoMeshOperation;
import star.meshing.GeometricSensitivityOptions;
import star.meshing.MeshOperationManager;
import star.post.RecordedSolutionView;
import star.post.SolutionHistory;
import star.post.SolutionHistoryManager;
import star.post.SolutionRepresentation;
import star.post.SolutionViewManager;

/**
 *
 * @author cd8unu
 */
public class BeforeAdjoint extends StarMacro {

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
    ArrayList<Report> _reportsWithSens = new ArrayList<>();
    LongVector _paramIds = new LongVector();
    final String OC1_PRIMAL = "Primal_Loiter";
    final String OC2_PRIMAL = "Primal_Dash";
    final String OC2_2_PRIMAL = "Primal_Dash2";
    final String ADJ = "RunAdj";
    final String SENS_EXT = ".sens";
    final String MACRO_MESSAGE_TAG = "BEFORE_ADJOINT: ";
    final String[] conv_plots = new String[]{"CD Asy Convergence", "CL Asy Convergence", "Cm Asy Convergence", "WBM Asy Convergence"};

    @Override
    public void execute() {
        initialize();
        try {
            if (!oc2HasRun()) {
                runWorkflow(_oc2_2Workflow);
                createSolutionHistorySnapshot("Dash");
                setViewsAndRepresentations();
            }
            runAdjoint();
            exportSensitivities(false);
            runWorkflow(_oc1Workflow);
            runAdjoint();
            exportSensitivities(true);
        } catch (Exception ex) {
            print(ex);
        }
    }

    private void runAdjoint() {
        _sim.println(MACRO_MESSAGE_TAG + "runAdjoint method");
        _simOpMan.setSelectedWorkflow(_adjWorkflow);
        _adjWorkflow.reset();
        _adjWorkflow.execute();
    }

    private void runWorkflow(SimDriverWorkflow workflow) {
        _sim.println(MACRO_MESSAGE_TAG + "runWorkflow method. Workflow = " + workflow);
        _simOpMan.setSelectedWorkflow(workflow);
        workflow.reset();
        workflow.execute();
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
        _oc1View.setStateName("Loiter");
        _oc2View.setStateName("Dash");
    }
    
    private void exportResiduals(SimDriverWorkflow workflow) {
        _sim.println(MACRO_MESSAGE_TAG + "exportResiduals method for workflow " + workflow.getPresentationName());
        StarPlot residuals = _sim.getPlotManager().getPlot("Residuals");
        residuals.encode(_sim.getSessionDir() + File.separator + "res" + workflow.getPresentationName() + ".png", "png", 1920, 1200, true, false);
        for (String s : conv_plots) {
            StarPlot conv = _sim.getPlotManager().getPlot(s);
            String report = conv.getPresentationName().split(" ")[0];
            String cond = workflow.getPresentationName().split("_")[1];
            String name = "conv_" + report + "_" + cond + ".png";
            conv.encode(_sim.getSessionDir() + File.separator + name, "png", 1920, 1200, true, false);
        }
    }
    
    private void printFinalParameterValues(SimDriverWorkflow workflow) {
        _sim.println(MACRO_MESSAGE_TAG + workflow.getPresentationName() + " final parameter values:");
        for (SetParameterAutomationBlock block : getSetParameterOperations(workflow)) {
            HashMap<String, Double> dict = getParameterValue(block);
            for (Map.Entry<String, Double> entry : dict.entrySet()) {
                _sim.println("\t" + MACRO_MESSAGE_TAG + entry.getKey() + ": " + entry.getValue());
            }
        }
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

    private void exportSensitivities(boolean op1) {
        _sim.println(MACRO_MESSAGE_TAG + "exportSensitivities method. op1 = " + Boolean.toString(op1));
        for (Report report : _reportsWithSens) {
            String oc = op1 ? "_1" : "_2";
            oc += SENS_EXT;
            String reportFileName = report.getPresentationName() + oc;
            writeSensitivitiesToFile(report, _paramIds, reportFileName);
        }
    }
    
    private void writeSensitivitiesToFile(Report report, LongVector paramIds, String fileName) {
        String r = report.getPresentationName();
        _sim.println(MACRO_MESSAGE_TAG + "writeSensitivitiesToFile method. Report = " + r + ", fileName = " + fileName);
        final String SENSITIVITIES_TAG = "Sensitivities"; // NOI18N
        final String PARAMETER_ID_TAG = "ParameterId"; // NOI18N
        final String SENSITIVITY_TAG = "Sensitivity"; // NOI18N

        // Get sensitivities from adjoint solver.
        AdjointSolver adjointSolver = _sim.getSolverManager().getSolver(AdjointSolver.class);
        GlobalParameterManager paramManager = _sim.get(GlobalParameterManager.class);
        ReportCostFunction reportCostFunc = hasReportCostFunction(report.getObjectId());

        if (adjointSolver != null && paramManager != null && reportCostFunc != null) {
            Vector<NeoProperty> gProps = new Vector<>();
            // Check if sensitivity is available for Simulation Parameters
            for (NamedObject parameter : paramManager.getObjects()) {
                if (parameter instanceof ScalarGlobalParameter && paramIds.contains(parameter.getObjectId())) {
                    ScalarGlobalParameter scalarParameter = (ScalarGlobalParameter) parameter;
                    if (adjointSolver.isGeometricParameterSensitivityAvailable(reportCostFunc, scalarParameter)) {

                        // NOTE: get Sensitivity value in reference Units of Report/Parameter dimensions.
                        double sensitivityInUnits = adjointSolver.getGeometricParameterSensitivityValueInUnits(reportCostFunc, scalarParameter);

                        NeoProperty gProp = new NeoProperty();
                        gProp.put(PARAMETER_ID_TAG, parameter.getObjectId());
                        gProp.put(SENSITIVITY_TAG, sensitivityInUnits);
                        gProps.add(gProp);
                        _sim.println("\tSensitivity for " + reportCostFunc.getPresentationName()
                                + " w.r.t to " + parameter.getPresentationName()
                                + " : " + sensitivityInUnits);
                    }
                }
            }
            NeoProperty out = new NeoProperty();
            out.put(SENSITIVITIES_TAG, gProps);
            String sensFilePath = NeoFileUtils.getAbsolutePath(NeoFileUtils.getPwd(), new File(fileName)); // NOI18N
            writeToFile(sensFilePath, out.toString());
        }
    }

    // Helper method to get report cost function for a given report object id.
    private ReportCostFunction hasReportCostFunction(long reportId) {
        AdjointCostFunctionManager costFuncManager = _sim.get(AdjointCostFunctionManager.class);
        ReportCostFunction reportCostFunc = null;
        if (costFuncManager != null) {
            // Get the report cost function.
            for (AdjointCostFunction cost : costFuncManager.getObjects()) {
                // Get cost function of the report
                if (cost instanceof ReportCostFunction && ((ReportCostFunction) cost).getReport().getObjectId() == reportId) {
                    reportCostFunc = (ReportCostFunction) cost;
                }
            }
        }
        return reportCostFunc;
    }

    private boolean writeToFile(String path, String str) {
        File file = new File(path);
        try (BufferedWriter fileWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {
            if (str != null) { // Allow to write empty file.
                fileWriter.write(str);
            }
            fileWriter.flush();
            fileWriter.close();
        } catch (Exception e) {
            print(e);
            return false;
        }
        return true;
    }
    
    private boolean oc2HasRun() {
        _sim.println(MACRO_MESSAGE_TAG + "oc2HasRun method");
        ScalarGlobalParameter dummyConv2 = (ScalarGlobalParameter) _sim.get(GlobalParameterManager.class).getObject("Asym_Conv_OC1");
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
        _reportsWithSens.add(_sim.getReportManager().getReport("CD"));
        _reportsWithSens.add(_sim.getReportManager().getReport("CL"));
        _reportsWithSens.add(_sim.getReportManager().getReport("WBM"));
        AutoMeshOperation meshOp = (AutoMeshOperation) _sim.get(MeshOperationManager.class).getObject("Automated Mesh");
        GeometricSensitivityOptions geomSensOps = meshOp.getDefaultValues().get(GeometricSensitivityOptions.class);
        for (GlobalParameterBase param : geomSensOps.getParameters().getObjects()) {
            _paramIds.add(param.getObjectId());
        }
    }

    private void print(Exception ex) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        ex.printStackTrace(pw);
        _sim.println(pw);
    }
}
