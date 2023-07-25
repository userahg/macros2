/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gradientBasedOpt;

import java.util.ArrayList;
import java.util.Collection;
import java.util.regex.Pattern;
import star.common.Dimensions;
import star.common.GlobalParameterManager;
import star.common.ScalarGlobalParameter;
import star.common.Simulation;
import star.common.StarMacro;
import star.common.Units;
import star.starcad2.StarCadDesignParameterDouble;
import star.starcad2.StarCadDocument;
import star.starcad2.StarCadDocumentManager;

/**
 *
 * @author cd8unu
 */
public class MakeSimParamsForCADClientParams extends StarMacro {
    Simulation _sim;

    @Override
    public void execute() {
        _sim = getActiveSimulation();
        
        createSimulationParameters(getParameters());
    }
    
    private Collection<StarCadDesignParameterDouble> getParameters() {
        ArrayList<StarCadDesignParameterDouble> params = new ArrayList<>();
        StarCadDocumentManager scdm = _sim.get(StarCadDocumentManager.class);
        for (StarCadDocument doc : scdm.getObjects()) {
            for (StarCadDesignParameterDouble scdpd : doc.getStarCadDesignParameters().getObjectsOf(StarCadDesignParameterDouble.class)) {
                //if (scdpd.getPresentationName().contains("\\P_")) {
                params.add(scdpd);                            
                //}
            }
        }
        return params;
    }
    
    private void createSimulationParameters(Collection<StarCadDesignParameterDouble> cadParams) {
        GlobalParameterManager simParamManager = _sim.get(GlobalParameterManager.class);
        for (StarCadDesignParameterDouble d : cadParams) {
            if (!d.getQuantity().getDefinition().contains("$")) {
                String name = d.getPresentationName().split(Pattern.quote("\\"))[1];
                Dimensions dim = d.getDimensions();
                Units units = d.getQuantity().getUnits();
                double value = d.getQuantity().getRawValue();
                ScalarGlobalParameter sgp = (ScalarGlobalParameter) simParamManager.createGlobalParameter(ScalarGlobalParameter.class, name);
                sgp.setDimensions(dim);
                sgp.getQuantity().setValueAndUnits(value, units);
                d.getQuantity().setDefinition("${" + sgp.getPresentationName() + "}");
            }
        }
    }
}
