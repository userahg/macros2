package volvo;


import star.common.FunctionScalarProfileMethod;
import star.common.ImplicitUnsteadyModel;
import star.common.PhysicsContinuum;
import star.common.ShellRegion;
import star.common.Simulation;
import star.common.StarMacro;
import star.common.SteadyModel;
import star.common.StepStoppingCriterion;
import star.fluidfilm.FluidFilmMassUserSource;
import star.fluidfilm.SegregatedFluidFilmFlowSolver;
import star.fluidfilm.SegregatedFluidFilmMultiphaseSolver;
import star.keturb.KeTurbSolver;
import star.keturb.KeTurbViscositySolver;
import star.segregatedflow.SegregatedFlowSolver;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author cd8unu
 */
public class SteadyStateToTransient4Volvo extends StarMacro {

    Simulation _sim;

    @Override
    public void execute() {
        _sim = getActiveSimulation();
        changeSolvers();
        changeBCs();
        freezeUnfreezeSolvers();
        runSolver();
    }

    private void changeSolvers() {
        PhysicsContinuum physics = (PhysicsContinuum) _sim.getContinuumManager().getContinuum("Physics 1");
        SteadyModel steadyState = physics.getModelManager().getModel(SteadyModel.class);
        physics.disableModel(steadyState);
        physics.enable(ImplicitUnsteadyModel.class);
        StepStoppingCriterion maxSteps = ((StepStoppingCriterion) _sim.getSolverStoppingCriterionManager().getSolverStoppingCriterion("Maximum Steps"));
        maxSteps.setMaximumNumberSteps(250000);
    }

    private void changeBCs() {
        ShellRegion shell = (ShellRegion) _sim.getRegionManager().getRegion("Region shell");
        FluidFilmMassUserSource massSource = shell.getValues().get(FluidFilmMassUserSource.class);
        massSource.setMethod(FunctionScalarProfileMethod.class);
    }

    private void freezeUnfreezeSolvers() {
        SegregatedFlowSolver segFlow = ((SegregatedFlowSolver) _sim.getSolverManager().getSolver(SegregatedFlowSolver.class));
        segFlow.setFreezeFlow(true);

        KeTurbSolver keTurb = ((KeTurbSolver) _sim.getSolverManager().getSolver(KeTurbSolver.class));
        keTurb.setFrozen(true);

        KeTurbViscositySolver keTurbVisc = ((KeTurbViscositySolver) _sim.getSolverManager().getSolver(KeTurbViscositySolver.class));
        keTurbVisc.setFrozen(true);

        SegregatedFluidFilmMultiphaseSolver ffSolver = ((SegregatedFluidFilmMultiphaseSolver) _sim.getSolverManager().getSolver(SegregatedFluidFilmMultiphaseSolver.class));
        SegregatedFluidFilmFlowSolver segFFSolver = ((SegregatedFluidFilmFlowSolver) ffSolver.getSolvers().getSolver(SegregatedFluidFilmFlowSolver.class));
        segFFSolver.setFrozen(false);
    }
    
    private void runSolver() {
        _sim.getSimulationIterator().run();
    }

}
