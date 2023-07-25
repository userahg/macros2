// Simcenter STAR-CCM+ macro: ClearSolutionChangeTurbulence.java
// Written by Simcenter STAR-CCM+ 15.04.008
package eclipse;


import star.common.*;
import star.kwturb.*;
import star.walldistance.*;
import star.saturb.*;

public class ClearSolutionChangeTurbulence extends StarMacro {
    Simulation _sim;

    @Override
    public void execute() {
        _sim = getActiveSimulation();
        Solution sol = _sim.getSolution();
        sol .clearSolution(Solution.Clear.History, Solution.Clear.Fields, Solution.Clear.LagrangianDem);
        PhysicsContinuum airPhysics = ((PhysicsContinuum) _sim.getContinuumManager().getContinuum("Air"));
        KwAllYplusWallTreatment kwAllYplus = airPhysics.getModelManager().getModel(KwAllYplusWallTreatment.class);
        airPhysics.disableModel(kwAllYplus);
        WallDistanceModel wallDist = airPhysics.getModelManager().getModel(WallDistanceModel.class);
        airPhysics.disableModel(wallDist);
        SstKwTurbModel sst = airPhysics.getModelManager().getModel(SstKwTurbModel.class);
        airPhysics.disableModel(sst);
        KOmegaTurbulence kOmega = airPhysics.getModelManager().getModel(KOmegaTurbulence.class);
        airPhysics.disableModel(kOmega);
        airPhysics.enable(SpalartAllmarasTurbulence.class);
        airPhysics.enable(SaTurbModel.class);
        airPhysics.enable(SaAllYplusWallTreatment.class);
        _sim.saveState("/u/cd8unu/projects/eclipse/SA/Design_1_SA.sim");
    }
}
