// Simcenter STAR-CCM+ macro: GetSensitivitesInUnits.java
// Written by Simcenter STAR-CCM+ 16.01.012
package adjointCAD;

import star.common.*;

public class GetSensitivitesInUnits extends StarMacro {

    Simulation _sim;
    String[] _params = {"StX", "StY", "StZ", "WingX", "WingY", "WingZ"};
    String[] _costFs = {"Drag", "Lift"};

    @Override
    public void execute() {
        _sim = getActiveSimulation();
        AdjointSolver adjSolver = (AdjointSolver) _sim.getSolverManager().getSolver(AdjointSolver.class);
        GlobalParameterManager paramManager = _sim.get(GlobalParameterManager.class);
        AdjointCostFunctionManager costFMan = _sim.get(AdjointCostFunctionManager.class);

        for (String param_name : _params) {
            ScalarGlobalParameter parameter = (ScalarGlobalParameter) paramManager.getObject(param_name);

            for (String costF_name : _costFs) {
                AdjointCostFunction cost = costFMan.getAdjointCostFunction(costF_name);
                if (adjSolver.isGeometricParameterSensitivityAvailable(cost, parameter)) {
                    double sens = adjSolver.getGeometricParameterSensitivityValueInUnits(cost, parameter);
                    _sim.println("Sens for " + cost.getPresentationName() + " w.r.t " + parameter.getPresentationName() + ": " + sens);
                }
            }

        }
    }
}
