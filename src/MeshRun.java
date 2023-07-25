
import star.common.Simulation;
import star.common.StarMacro;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author cd8unu
 */
public class MeshRun extends StarMacro {

    @Override
    public void execute() {
        Simulation _sim = getActiveSimulation();
        _sim.getMeshPipelineController().generateVolumeMesh();
        _sim.getSimulationIterator().run();
    }
    
}
