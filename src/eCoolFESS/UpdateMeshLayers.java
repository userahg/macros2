// Simcenter STAR-CCM+ macro: UpdateMeshLayers.java
// Written by Simcenter STAR-CCM+ 17.04.007
package eCoolFESS;

import star.common.*;
import star.meshing.*;
import star.solidstress.FiniteElementStressSolver;
import star.sweptmesher.*;

public class UpdateMeshLayers extends StarMacro {

    Simulation _sim;

    @Override
    public void execute() {
        _sim = getActiveSimulation();
        
        GlobalParameterManager gpm = _sim.get(GlobalParameterManager.class);
        double run_ss = ((ScalarGlobalParameter) gpm.getObject("P_SolidStress")).getQuantity().getRawValue();
        int n_layers = (int) ((ScalarGlobalParameter) _sim.get(GlobalParameterManager.class).getObject("P_N_Layers")).getQuantity().getRawValue();
        
        FiniteElementStressSolver fess = (FiniteElementStressSolver) _sim.getSolverManager().getSolver(FiniteElementStressSolver.class);
        fess.setFrozen(run_ss <= 0.001);
        
        DirectedMeshOperation dmIGBTDiode = (DirectedMeshOperation) _sim.get(MeshOperationManager.class).getObject("Directed IGBT/Diode");
        DirectedMeshDistribution dmIGBTDiodeVolDist = (DirectedMeshDistribution) dmIGBTDiode.getDirectedMeshDistributionManager().getObject("Volume Distribution");
        dmIGBTDiodeVolDist.getDefaultValues().setNumLayers(n_layers);
        PartCustomMeshControl dmIGBTDiodePartControl = (PartCustomMeshControl) dmIGBTDiodeVolDist.getCustomValues().getObject("Part Control");
        DirectedMeshNumLayers dmIGBTDiodeNLayers = dmIGBTDiodePartControl.getCustomValues().get(DirectedMeshNumLayers.class);
        dmIGBTDiodeNLayers.setNumLayers(n_layers);
        
        DirectedMeshOperation dmDieDieAttachTraceSub3 = (DirectedMeshOperation) _sim.get(MeshOperationManager.class).getObject("Directed Die/DieAttach/Trace/Sub3");
        DirectedMeshDistribution dmDieDieAttachTraceSub3VolDist = (DirectedMeshDistribution) dmDieDieAttachTraceSub3.getDirectedMeshDistributionManager().getObject("Volume Distribution");
        dmDieDieAttachTraceSub3VolDist.getDefaultValues().setNumLayers(n_layers * 4);
        PartCustomMeshControl dmDieDieAttachTraceSub3PartControl = (PartCustomMeshControl) dmDieDieAttachTraceSub3VolDist.getCustomValues().getObject("Part Control");
        DirectedMeshNumLayers dmDieDieAttachTraceSub3NLayers = dmDieDieAttachTraceSub3PartControl.getCustomValues().get(DirectedMeshNumLayers.class);
        dmDieDieAttachTraceSub3NLayers.setNumLayers(n_layers);
        
        DirectedMeshOperation dmSubLowerPlate = (DirectedMeshOperation) _sim.get(MeshOperationManager.class).getObject("Directed Substrates/LowerPlate");
        DirectedMeshDistribution dmSubLowerPlateVolDist = (DirectedMeshDistribution) dmSubLowerPlate.getDirectedMeshDistributionManager().getObject("Volume Distribution");
        dmSubLowerPlateVolDist.getDefaultValues().setNumLayers(n_layers * 3);
        PartCustomMeshControl dmSubLowerPlatePartControl = (PartCustomMeshControl) dmSubLowerPlateVolDist.getCustomValues().getObject("Part Control");
        DirectedMeshNumLayers dmSubLowerPlateNLayers = dmSubLowerPlatePartControl.getCustomValues().get(DirectedMeshNumLayers.class);
        dmSubLowerPlateNLayers.setNumLayers(n_layers);

        DirectedMeshOperation dmUpperPlate = (DirectedMeshOperation) _sim.get(MeshOperationManager.class).getObject("Upper Plate");
        DirectedMeshDistribution dmUpperPlateVolDist = (DirectedMeshDistribution) dmUpperPlate.getDirectedMeshDistributionManager().getObject("Volume Distribution");
        dmUpperPlateVolDist.getDefaultValues().setNumLayers(n_layers);
        DirectedMeshNumLayers dmUpperPlateNLayers = dmUpperPlateVolDist.getDefaultValues().get(DirectedMeshNumLayers.class);
        dmUpperPlateNLayers.setNumLayers(n_layers);
    }
}
