// STAR-CCM+ macro: ConstantDensity2IdealGas.java
// Written by STAR-CCM+ 14.06.012
package honda;

import java.io.File;
import star.common.*;
import star.flow.*;
import star.meshing.MeshPipelineController;

public class ConstantDensity2IdealGas extends StarMacro {

    Simulation _sim;
    
    @Override
    public void execute() {
        execute0();
    }

    private void execute0() {

        _sim = getActiveSimulation();
        
        MeshPipelineController meshPipeline = _sim.get(MeshPipelineController.class);
        meshPipeline.generateVolumeMesh();
        
        _sim.getSimulationIterator().run();
        
        _sim.saveState(_sim.getSessionDir() + File.separator + _sim.getPresentationName() + "_refState.sim");
        
        PhysicsContinuum physics = ((PhysicsContinuum) _sim.getContinuumManager().getContinuum("Air"));
        ConstantDensityModel constant = physics.getModelManager().getModel(ConstantDensityModel.class);
        physics.disableModel(constant);
        physics.enable(IdealGasModel.class);
        
        _sim.getSimulationIterator().run();
        
        _sim.saveState(_sim.getSessionDir() + File.separator + _sim.getPresentationName() + ".sim");
    }
}
