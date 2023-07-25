// Simcenter STAR-CCM+ macro: GetSensitivitesInUnits.java
// Written by Simcenter STAR-CCM+ 16.01.012
package adjointCAD;

import java.util.ArrayList;
import java.util.Collection;
import star.common.*;
import star.base.report.ExpressionReport;
import star.meshing.AutoMeshOperation;
import star.meshing.GeometricSensitivityAutoMesher;
import star.meshing.MeshOperationManager;

public class UpdateSensitivityReports2 extends StarMacro {

    Simulation _sim;
    String[] _params = {"S1_DX", "S1_DY", "S1_R", "S2_DX", "S2_DY", "S2_R", "S3_DX", "S3_DY", "S3_R", "S4_DX", "S4_DY", "S4_R", "S5_DX", "S5_DY", "S5_R",
                        "S6_DX", "S6_DY", "S6_R", "S7_DX", "S7_DY", "S7_R"};
    String[] _costFs = {"Pressure_Drop"};

    @Override
    public void execute() {
        _sim = getActiveSimulation();

        storeAdjSens();
    }

    private void storeAdjSens() {
        for (String param_i : _params) {
            ScalarGlobalParameter param = (ScalarGlobalParameter) _sim.get(GlobalParameterManager.class).getObject(param_i);
            for (String costFun_i : _costFs) {
                AdjointCostFunction costFun = _sim.get(AdjointCostFunctionManager.class).getAdjointCostFunction(costFun_i);
                double sens = _sim.getSolverManager().getSolver(AdjointSolver.class).getGeometricParameterSensitivityValueInUnits(costFun, param);
                String reportName = "dPwrt" + param_i;
                ExpressionReport er = (ExpressionReport) _sim.getReportManager().getReport(reportName);
                er.setDefinition(Double.toString(sens));
                _sim.println("Sens for " + costFun.getPresentationName() + " w.r.t " + param.getPresentationName() + ": " + sens);
            }
        }
    }

    private void updateGeomSensMesher() {
        ScalarGlobalParameter nAdjParam = (ScalarGlobalParameter) _sim.get(GlobalParameterManager.class).getObject("nAdj");
        AutoMeshOperation autoMeshOp = (AutoMeshOperation) _sim.get(MeshOperationManager.class).getObject("Automated Mesh");
        GeometricSensitivityAutoMesher geomSensAutoMesh = (GeometricSensitivityAutoMesher) autoMeshOp.getMeshers().getObject("Geometric Sensitivity");
        Collection<ScalarGlobalParameter> params = new ArrayList<>();

        for (String param_i : _params) {
            params.add((ScalarGlobalParameter) _sim.get(GlobalParameterManager.class).getObject(param_i));
        }

        if (nAdjParam.getQuantity().evaluate() < 1) {
            geomSensAutoMesh.getParameters().removeObjects(params);
        } else {
            geomSensAutoMesh.getParameters().setObjects(params);
        }
    }
}
