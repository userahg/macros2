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

public class UpdateGometricSensitivityMesher extends StarMacro {

    Simulation _sim;
    String[] _params = {"StX", "StY", "StZ", "WingX", "WingY", "WingZ"};
    String[] _costFs = {"Drag", "Lift"};

    @Override
    public void execute() {
        _sim = getActiveSimulation();

        updateGeomSensMesher();
    }

    private void storeAdjSens() {
        for (String param_i : _params) {
            ScalarGlobalParameter param = (ScalarGlobalParameter) _sim.get(GlobalParameterManager.class).getObject(param_i);
            for (String costFun_i : _costFs) {
                AdjointCostFunction costFun = _sim.get(AdjointCostFunctionManager.class).getAdjointCostFunction(param_i);
                double sens = _sim.getSolverManager().getSolver(AdjointSolver.class).getGeometricParameterSensitivityValueInUnits(costFun, param);
                String reportName = costFun_i.contains("Lift") ? "DM_SensL_" + param_i : "DM_SensD_" + param_i;
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

        if (nAdjParam.getQuantity().evaluate() < 1) {
            geomSensAutoMesh.getParameters().setObjects();
        } else {
            Collection<ScalarGlobalParameter> params = new ArrayList<>();
            for (String param_i : _params) {
                params.add((ScalarGlobalParameter) _sim.get(GlobalParameterManager.class).getObject(param_i));
            }
            geomSensAutoMesh.getParameters().setObjects(params);
        }
    }
}
