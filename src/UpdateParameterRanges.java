
import static java.lang.Math.abs;
import java.util.Collection;
import star.mdx.MdxContinuousParameterValue;
import star.mdx.MdxDesignStudy;
import star.mdx.MdxMacro;
import star.mdx.MdxProject;
import star.mdx.MdxStudyParameter;
import star.mdx.MdxStudyParameterBase;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author cd8unu
 */
public class UpdateParameterRanges extends MdxMacro {

    Method method = Method.ABSOLUTE;
    double relative = -25.0;
    double absolute = 0.1;
    double threshold = 1.0;
    String study_name = "CADR7";

    MdxProject _proj;
    MdxDesignStudy _study;

    @Override
    public void execute() {
        _proj = getActiveMdxProject();
        _study = _proj.getDesignStudyManager().getDesignStudy(study_name);

        Collection<MdxStudyParameter> params = _study.getStudyParameters().getObjectsOf(MdxStudyParameter.class);
        _proj.println("Study has " + params.size() + " parameters");

        for (MdxStudyParameter p_i : params) {
            if (p_i.getParameterType() == MdxStudyParameterBase.ParameterType.CONTINUOUS) {
                MdxContinuousParameterValue p_i_cont_value = p_i.getContinuousParameterValue();
                if (method == Method.ABSOLUTE) {
                    updateAbsolute(p_i_cont_value);
                } else {
                    updateRelative(p_i_cont_value);
                }
            }
        }
    }
    
    private void updateRelative(MdxContinuousParameterValue val) {
        double max = val.getMaximumQuantity().getRawValue();
        double min = val.getMinimumQuantity().getRawValue();
        double ratio = relative / 100.0;
        if (abs(max) > threshold) {
            double delta_max = ratio * max;
            if (max == 0.0) {
                delta_max = ratio * min;
            }
            double max_sign = max > 0.0 ? 1.0 : -1.0;
            max = max + (max_sign * delta_max);
        }

        if (abs(min) > threshold) {
            double delta_min = ratio * min;
            if (min == 0.0) {
                delta_min = ratio * max;
            }
            double min_sign = min > 0.0 ? 1.0 : -1.0;
            min = min - (min_sign * delta_min);
        }

        val.getMaximumQuantity().setValue(max);
        val.getMinimumQuantity().setValue(min);
    }
    
    private void updateAbsolute(MdxContinuousParameterValue val) {
        double max = val.getMaximumQuantity().getRawValue();
        double min = val.getMinimumQuantity().getRawValue();
        val.getMaximumQuantity().setValue(max + absolute);
        val.getMinimumQuantity().setValue(min - absolute);
    }
    
    private enum Method {
        ABSOLUTE, RELATIVE
    }
}
