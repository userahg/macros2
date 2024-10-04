// Simcenter STAR-CCM+ macro: GetSensitivites.java
// Written by Simcenter STAR-CCM+ 16.01.012
package adjointCAD;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import star.base.report.ExpressionReport;
import star.common.*;
import star.meshing.AutoMeshOperation;
import star.meshing.GeometricSensitivityOptions;
import star.meshing.MeshOperationManager;

public class GetSensitivites extends StarMacro {

    Simulation _sim;
    final String _paramName = "Adj";

    @Override
    public void execute() {
        _sim = getActiveSimulation();

        try {
            if (ranAdjoint()) {
                updateReports();
            }
        } catch (Exception ex) {
            print(ex);
        }
    }
    
    private boolean ranAdjoint() {
        ScalarGlobalParameter param = (ScalarGlobalParameter) _sim.get(GlobalParameterManager.class).getObject(_paramName);
        return param.getQuantity().getRawValue() > 0.5;        
    }

    private void updateReports() {
        AdjointSolver adjointSolver = (AdjointSolver) _sim.getSolverManager().getSolver(AdjointSolver.class);
        ArrayList<ScalarGlobalParameter> sensitivityParams = getSensitivityParameters();
        ArrayList<AdjointCostFunction> costFunctions = getCostFunctions();

        for (ScalarGlobalParameter p : sensitivityParams) {
            for (AdjointCostFunction cf : costFunctions) {
                double sens = adjointSolver.getGeometricParameterSensitivityValue(cf, p);
                double sensWUnits = adjointSolver.getGeometricParameterSensitivityValueInUnits(cf, p);
                ExpressionReport er = getExpressionReport(p, cf);
                if (er != null) {
                    er.setDefinition(Double.toString(sens));
                }
                _sim.println("Sensitivity of " + cf.getPresentationName() + " w.r.t " + p.getPresentationName() + " = " + Double.toString(sens));
                _sim.println("Sensitivity of " + cf.getPresentationName() + " w.r.t " + p.getPresentationName() + " in Units = " + Double.toString(sensWUnits));
            }
        }
    }

    private ArrayList<ScalarGlobalParameter> getSensitivityParameters() {
        ArrayList<ScalarGlobalParameter> sensitivityParams = new ArrayList<>();
        Collection<AutoMeshOperation> meshers = _sim.get(MeshOperationManager.class).getObjectsOf(AutoMeshOperation.class);
        for (AutoMeshOperation mesher : meshers) {
            try {
                GeometricSensitivityOptions geometricSensitivityOptions = mesher.getDefaultValues().get(GeometricSensitivityOptions.class);
                if (geometricSensitivityOptions == null) {
                    _sim.println(mesher.getPresentationName() + " has no geometric sensitivity.");
                } else {
                    for (GlobalParameterBase p : geometricSensitivityOptions.getParameters().getObjects()) {
                        if (p instanceof ScalarGlobalParameter) {
                            sensitivityParams.add((ScalarGlobalParameter) p);
                        }
                    }
                }
            } catch (Exception ex) {
                _sim.println(mesher.getPresentationName() + " has no geometric sensitivity.");
                print(ex);
            }
        }
        return sensitivityParams;
    }

    private ArrayList<AdjointCostFunction> getCostFunctions() {
        ArrayList<AdjointCostFunction> costFunctions = new ArrayList<>();
        AdjointCostFunctionManager cfManager = _sim.get(AdjointCostFunctionManager.class);
        costFunctions.addAll(cfManager.getObjects());
        return costFunctions;
    }

    private ExpressionReport getExpressionReport(ScalarGlobalParameter p, AdjointCostFunction cf) {
        ReportCostFunction rcf = (ReportCostFunction) cf;
        String[] reportNameSplit = rcf.getReport().getPresentationName().split(" ");
        String reportName = reportNameSplit[reportNameSplit.length - 1];
        String paramName = p.getPresentationName();
        String expReportName = "Sens " + reportName + " w.r.t. " + paramName;
        ExpressionReport er = null;
        try {
            er = (ExpressionReport) _sim.getReportManager().getReport(expReportName);
        } catch (Exception ex) {
            er = _sim.getReportManager().createReport(ExpressionReport.class);
            er.setPresentationName(expReportName);
        }
        return er;
    }

    private void print(Exception ex) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        ex.printStackTrace(pw);
        _sim.println(sw);
    }
}
