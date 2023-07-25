// Simcenter STAR-CCM+ macro: GetSensitivitesInUnits.java
// Written by Simcenter STAR-CCM+ 16.01.012
package crmOpt;

import star.common.*;

public class GetSensitivitesInUnits extends StarMacro {

    Simulation _sim;
    String[] _params = {"P_W_L0", "P_W_L1", "P_W_L2", "P_W_L3", "P_W_L4", "P_W_L5", "P_W_L6", "P_W_L7", "P_W_L8", "P_W_L9",
    "P_W_L10", "P_W_U0", "P_W_U1", "P_W_U2", "P_W_U3", "P_W_U4", "P_W_U5", "P_W_U6", "P_W_U7", "P_W_U8", "P_W_U9",
    "P_W_U10"};
    String[] _costFs = {"L_over_D", "Area_Const"};

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
