/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eCoolFESS;

import star.common.GlobalParameterManager;
import star.common.Region;
import star.common.ScalarGlobalParameter;
import star.common.Simulation;
import star.common.StarMacro;
import star.solvermeshing.MidSideVertexOption;

/**
 *
 * @author cd8unu
 */
public class UpdateMidNodeVertex extends StarMacro {
    Simulation _sim;
    
    @Override
    public void execute() {
        _sim = getActiveSimulation();

        ScalarGlobalParameter midNodeVertexParam = (ScalarGlobalParameter) _sim.get(GlobalParameterManager.class).getObject("P_MN_Vertex");
        double midNodeVertex = midNodeVertexParam.getQuantity().getRawValue();
        
        Region bar = _sim.getRegionManager().getRegion("BAR");
        
        if (midNodeVertex > 0.001) {
            bar.getConditions().get(MidSideVertexOption.class).setSelected(MidSideVertexOption.Type.LINEAR_INTERPOLATION);            
        } else {
            bar.getConditions().get(MidSideVertexOption.class).setSelected(MidSideVertexOption.Type.NO_CHANGE);
        }
        
    }
    
}
