
import java.util.ArrayList;
import star.base.neo.DoubleVector;
import star.mdx.MdxDesignStudy;
import star.mdx.MdxDiscreteParameterValue;
import star.mdx.MdxMacro;
import star.mdx.MdxProject;
import star.mdx.MdxStudyParameter;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author cd8unu
 */
public class SetDiscreteParameterValues extends MdxMacro {

    @Override
    public void execute() {
        MdxProject _proj = getActiveMdxProject();
        MdxDesignStudy study = _proj.getDesignStudyManager().getDesignStudy("RSM Training");
        MdxStudyParameter omega = (MdxStudyParameter) study.getStudyParameters().getObject("Omega");
        MdxDiscreteParameterValue omegaDiscreteParameterVals = omega.getDiscreteParameterValue();
        double val = 25.0;
        double inc = 25.0;
        double max = 10150.0;
        ArrayList<Double> vals = new ArrayList<>();
        while (val <= max) {
            vals.add(val);
            val += inc;
        }
        double[] dv = new double[vals.size()];
        for (int i = 0; i< vals.size(); i++) {
            dv[i] = vals.get(i);
        }
        omegaDiscreteParameterVals.getQuantity().setArray(new DoubleVector(dv));
    }    
}
