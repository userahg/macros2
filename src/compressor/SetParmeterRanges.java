// Simcenter STAR-CCM+ macro: SetRange.java
// Written by Simcenter STAR-CCM+ 18.02.008
package compressor;

import java.util.ArrayList;
import java.util.Collection;
import star.mdx.*;

public class SetParmeterRanges extends MdxMacro {

    String _study = "SQP1";
    Object[][] _rules = {{"P_W", true, 0.8},
    {"leadingEdge", false, 10.0},
    {"trailingEdge", true, 0.3},
    {"angle", false, 10.0},
    {"chord", false, 25.0}};

    double _default_ratio = 0.1;
    double _default_min = -4.0;
    double _default_max = 4.0;

    MdxProject _proj;
    ArrayList<ParameterDefinition> _paramRules;

    @Override
    public void execute() {
        _proj = getActiveMdxProject();
        createParameterRules();
        MdxDesignStudy study = _proj.getDesignStudyManager().getDesignStudy(_study);
        Collection<MdxStudyParameter> params = study.getStudyParameters().getObjectsOf(MdxStudyParameter.class);
        _proj.println("Study has " + params.size() + " parameters");

        for (MdxStudyParameter p_i : params) {
            boolean is_set = false;
            MdxContinuousParameterValue p_i_cont_value = p_i.getContinuousParameterValue();
            double ref_val = p_i.getReferenceQuantity().getRawValue();
            for (ParameterDefinition rule : _paramRules) {
                if (p_i.getPresentationName().contains(rule.regex)) {
                    double min = rule.getMinVal(ref_val);
                    double max = rule.getMaxVal(ref_val);
                    p_i_cont_value.getMaximumQuantity().setValue(max);
                    p_i_cont_value.getMinimumQuantity().setValue(min);
                    _proj.println("Parameter " + p_i.getPresentationName() + " matches " + rule.regex);
                    _proj.println("Parmeter " + p_i.getPresentationName());
                    _proj.println("\tmin = " + Double.toString(min));
                    _proj.println("\tmax = " + Double.toString(max));
                    is_set = true;
                    break;
                }
            }
            if (!is_set) {
                double max_val = _default_max;
                double min_val = _default_min;
                if (java.lang.Math.abs(ref_val) > 0.000000000001) {
                    max_val = ref_val > 0 ? (1.0 + _default_ratio) * ref_val : (1.0 - _default_ratio) * ref_val;
                    min_val = ref_val > 0 ? (1.0 - _default_ratio) * ref_val : (1.0 + _default_ratio) * ref_val;
                }
                p_i_cont_value.getMaximumQuantity().setValue(max_val);
                p_i_cont_value.getMinimumQuantity().setValue(min_val);
                _proj.println("Parmeter " + p_i.getPresentationName());
                _proj.println("\tmin = " + Double.toString(min_val));
                _proj.println("\tmax = " + Double.toString(max_val));
            }
        }
    }

    private void createParameterRules() {
        _paramRules = new ArrayList<>();
        for (Object[] rule : _rules) {
            _paramRules.add(new ParameterDefinition(rule));
        }
    }

    private class ParameterDefinition {

        final String regex;
        final boolean relative;
        final double offset;
        final double eps = 0.0000000001;

        public ParameterDefinition(Object[] options) {
            regex = (String) options[0];
            relative = (Boolean) options[1];
            offset = (Double) options[2];
        }

        public double getMaxVal(double refVal) {
            if (relative) {
                return refVal > 0 ? (1.0 + offset) * refVal : (1.0 - offset) * refVal;
            } else {
                return refVal + offset;
            }
        }

        public double getMinVal(double refVal) {
            if (relative) {
                return refVal > 0 ? (1.0 - offset) * refVal : (1.0 + offset) * refVal;
            } else {
                return refVal - offset;
            }
        }
    }
}
