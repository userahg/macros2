/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gradientBasedOpt;

import java.util.ArrayList;
import java.util.Collection;
import star.base.neo.DoubleVector;
import star.cadmodeler.CadModel;
import star.cadmodeler.DesignParameter;
import star.cadmodeler.ScalarQuantityDesignParameter;
import star.cadmodeler.SolidModelManager;
import star.cadmodeler.VectorQuantityDesignParameter;
import star.common.CadModelBase;
import star.common.Dimensions;
import star.common.GlobalParameterManager;
import star.common.ScalarGlobalParameter;
import star.common.Simulation;
import star.common.StarMacro;
import star.common.Units;
import star.common.VectorGlobalParameter;

/**
 *
 * @author cd8unu
 */
public class MakeSimParamsFor3DCADParams extends StarMacro {

    String geom_sens_tag = "CAD";
    Simulation _sim;

    @Override
    public void execute() {
        _sim = getActiveSimulation();

        createSimulationParameters(getParameters());
    }

    private Collection<DesignParameter> getParameters() {
        ArrayList<DesignParameter> params = new ArrayList<>();
        for (CadModelBase base : _sim.get(SolidModelManager.class).getObjects()) {
            CadModel cad = (CadModel) base;
            for (DesignParameter dp : cad.getDesignParameterManager().getDesignParameters()) {
                if (dp instanceof VectorQuantityDesignParameter) {
                    VectorQuantityDesignParameter vqdp = (VectorQuantityDesignParameter) dp;
                    if (!vqdp.getQuantity().getIsExpression() && vqdp.getPresentationName().startsWith(geom_sens_tag)) {
                        params.add(dp);
                    }
                } else if (dp instanceof ScalarQuantityDesignParameter) {
                    ScalarQuantityDesignParameter sqdp = (ScalarQuantityDesignParameter) dp;
                    if (!sqdp.getQuantity().getIsExpression() && sqdp.getPresentationName().startsWith(geom_sens_tag)) {
                        params.add(dp);
                    }
                }
            }

        }
        return params;
    }

    private void createSimulationParameters(Collection<DesignParameter> cadParams) {
        GlobalParameterManager simParamManager = _sim.get(GlobalParameterManager.class);
        for (DesignParameter d : cadParams) {
            if (d instanceof ScalarQuantityDesignParameter) {
                ScalarQuantityDesignParameter sqdp = (ScalarQuantityDesignParameter) d;
                String name = "P_" + sqdp.getPresentationName();
                Dimensions dim = sqdp.getDimensions();
                Units units = sqdp.getQuantity().getUnits();
                double value = sqdp.getQuantity().getRawValue();
                ScalarGlobalParameter sgp = (ScalarGlobalParameter) simParamManager.createGlobalParameter(ScalarGlobalParameter.class, name);
                sgp.setDimensions(dim);
                sgp.getQuantity().setValueAndUnits(value, units);
                sqdp.getQuantity().setDefinition("${" + sgp.getPresentationName() + "}");
            } else if (d instanceof VectorQuantityDesignParameter) {
                VectorQuantityDesignParameter vqdp = (VectorQuantityDesignParameter) d;
                String name = "P_" + vqdp.getPresentationName();
                Dimensions dim = vqdp.getDimensions();
                Units units = vqdp.getQuantity().getUnits();
                DoubleVector value = vqdp.getQuantity().getRawVector();
                String definition = "[";
                for (int i = 0; i < value.size() - 1; i++) {
                    String current_name = name + "_" + Integer.toString(i);
                    double val = value.getComponent(i);
                    ScalarGlobalParameter sgp = (ScalarGlobalParameter) simParamManager.createGlobalParameter(ScalarGlobalParameter.class, current_name);
                    sgp.setDimensions(dim);
                    sgp.getQuantity().setValueAndUnits(val, units);
                    definition +=  "${" + sgp.getPresentationName() + "}, ";
                }
                int size = value.size() - 1;
                String current_name = name + "_" + Integer.toString(size);
                double val = value.getComponent(size);
                ScalarGlobalParameter sgp = (ScalarGlobalParameter) simParamManager.createGlobalParameter(ScalarGlobalParameter.class, current_name);
                sgp.setDimensions(dim);
                sgp.getQuantity().setValueAndUnits(val, units);
                definition += "${" + sgp.getPresentationName() + "}]";
                _sim.println(sgp.getPresentationName() + ": " + definition);
                vqdp.getQuantity().setDefinition(definition);
            }
        }
    }
}
