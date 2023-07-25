// Simcenter STAR-CCM+ macro: GetSensitivites.java
// Written by Simcenter STAR-CCM+ 16.01.012
package adjointCAD;

import star.base.report.ExpressionReport;
import star.common.*;

public class GetSensitivites extends StarMacro {

    Simulation _sim;
    String[] params = {"D1x", "D1y", "D2x", "D2y"};
    String costFunc = "PressureDropAdj";

    @Override
    public void execute() {
        _sim = getActiveSimulation();
        AdjointSolver adjointSolver = (AdjointSolver) _sim.getSolverManager().getSolver(AdjointSolver.class);
        GlobalParameterManager paramManager = _sim.get(GlobalParameterManager.class);
        AdjointCostFunctionManager costFunctionManager = _sim.get(AdjointCostFunctionManager.class);

        for (String param : params) {
            ScalarGlobalParameter sgp = (ScalarGlobalParameter) paramManager.getObject(param);
            AdjointCostFunction costFunction = costFunctionManager.getObject(costFunc);
            if (adjointSolver.isGeometricParameterSensitivityAvailable(costFunction, sgp)) {
                ExpressionReport er = (ExpressionReport) _sim.getReportManager().getReport("gradFwrt" + param);
                double sens = adjointSolver.getGeometricParameterSensitivityValue(costFunction, sgp);
                _sim.println("Sens for " + costFunction.getPresentationName() + " w.r.t " + sgp.getPresentationName() + ": " + sens);
                er.setDefinition(Double.toString(sens));
            }
        }
    }
}
