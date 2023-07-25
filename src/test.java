// Simcenter STAR-CCM+ macro: test.java
// Written by Simcenter STAR-CCM+ 18.05.028
package macro;


import star.common.*;
import star.mdx.*;

public class test extends MdxMacro {

  public void execute() {
    execute0();
  }

  private void execute0() {

    MdxProject mdxProject_0 = 
      getActiveMdxProject();

    MdxDesignStudy mdxDesignStudy_0 = 
      mdxProject_0.getDesignStudyManager().getDesignStudy("fourModesLHC");

    MdxStudyParameter mdxStudyParameter_0 = 
      ((MdxStudyParameter) mdxDesignStudy_0.getStudyParameters().getObject("C_Alpha"));

    mdxStudyParameter_0.setParameterType(MdxStudyParameterBase.ParameterType.CONSTANT);

    MdxStudyParameter mdxStudyParameter_1 = 
      ((MdxStudyParameter) mdxDesignStudy_0.getStudyParameters().getObject("C_Altitude"));

    mdxStudyParameter_1.setParameterType(MdxStudyParameterBase.ParameterType.CONSTANT);

    MdxStudyParameter mdxStudyParameter_2 = 
      ((MdxStudyParameter) mdxDesignStudy_0.getStudyParameters().getObject("C_Ma"));

    mdxStudyParameter_2.setParameterType(MdxStudyParameterBase.ParameterType.CONSTANT);

    MdxStudyParameter mdxStudyParameter_3 = 
      ((MdxStudyParameter) mdxDesignStudy_0.getStudyParameters().getObject("P_TE_Thickness"));

    mdxStudyParameter_3.setParameterType(MdxStudyParameterBase.ParameterType.CONSTANT);

    MdxStudyParameter mdxStudyParameter_4 = 
      ((MdxStudyParameter) mdxDesignStudy_0.getStudyParameters().getObject("P_W_0"));

    mdxStudyParameter_4.setParameterType(MdxStudyParameterBase.ParameterType.CONTINUOUS);

    MdxStudyParameter mdxStudyParameter_5 = 
      ((MdxStudyParameter) mdxDesignStudy_0.getStudyParameters().getObject("P_W_1"));

    mdxStudyParameter_5.setParameterType(MdxStudyParameterBase.ParameterType.CONTINUOUS);

    MdxStudyParameter mdxStudyParameter_6 = 
      ((MdxStudyParameter) mdxDesignStudy_0.getStudyParameters().getObject("P_W_2"));

    mdxStudyParameter_6.setParameterType(MdxStudyParameterBase.ParameterType.CONTINUOUS);

    MdxStudyParameter mdxStudyParameter_7 = 
      ((MdxStudyParameter) mdxDesignStudy_0.getStudyParameters().getObject("P_W_3"));

    mdxStudyParameter_7.setParameterType(MdxStudyParameterBase.ParameterType.CONTINUOUS);

    MdxStudyParameter mdxStudyParameter_8 = 
      ((MdxStudyParameter) mdxDesignStudy_0.getStudyParameters().getObject("P_W_4"));

    mdxStudyParameter_8.setParameterType(MdxStudyParameterBase.ParameterType.CONSTANT);

    MdxStudyParameter mdxStudyParameter_9 = 
      ((MdxStudyParameter) mdxDesignStudy_0.getStudyParameters().getObject("P_W_5"));

    mdxStudyParameter_9.setParameterType(MdxStudyParameterBase.ParameterType.CONSTANT);

    MdxStudyParameter mdxStudyParameter_10 = 
      ((MdxStudyParameter) mdxDesignStudy_0.getStudyParameters().getObject("P_W_6"));

    mdxStudyParameter_10.setParameterType(MdxStudyParameterBase.ParameterType.CONSTANT);

    MdxStudyParameter mdxStudyParameter_11 = 
      ((MdxStudyParameter) mdxDesignStudy_0.getStudyParameters().getObject("P_W_7"));

    mdxStudyParameter_11.setParameterType(MdxStudyParameterBase.ParameterType.CONSTANT);

    MdxStudyParameter mdxStudyParameter_12 = 
      ((MdxStudyParameter) mdxDesignStudy_0.getStudyParameters().getObject("P_W_8"));

    mdxStudyParameter_12.setParameterType(MdxStudyParameterBase.ParameterType.CONSTANT);

    MdxStudyParameter mdxStudyParameter_13 = 
      ((MdxStudyParameter) mdxDesignStudy_0.getStudyParameters().getObject("P_W_9"));

    mdxStudyParameter_13.setParameterType(MdxStudyParameterBase.ParameterType.CONSTANT);

    MdxStudyParameter mdxStudyParameter_14 = 
      ((MdxStudyParameter) mdxDesignStudy_0.getStudyParameters().getObject("P_W_10"));

    mdxStudyParameter_14.setParameterType(MdxStudyParameterBase.ParameterType.CONSTANT);

    MdxStudyParameter mdxStudyParameter_15 = 
      ((MdxStudyParameter) mdxDesignStudy_0.getStudyParameters().getObject("P_W_11"));

    mdxStudyParameter_15.setParameterType(MdxStudyParameterBase.ParameterType.CONSTANT);

    MdxStudyParameter mdxStudyParameter_16 = 
      ((MdxStudyParameter) mdxDesignStudy_0.getStudyParameters().getObject("P_W_12"));

    mdxStudyParameter_16.setParameterType(MdxStudyParameterBase.ParameterType.CONSTANT);

    MdxStudyParameter mdxStudyParameter_17 = 
      ((MdxStudyParameter) mdxDesignStudy_0.getStudyParameters().getObject("P_W_13"));

    mdxStudyParameter_17.setParameterType(MdxStudyParameterBase.ParameterType.CONSTANT);

    MdxStudyParameter mdxStudyParameter_18 = 
      ((MdxStudyParameter) mdxDesignStudy_0.getStudyParameters().getObject("P_W_14"));

    mdxStudyParameter_18.setParameterType(MdxStudyParameterBase.ParameterType.CONSTANT);

    MdxStudyParameter mdxStudyParameter_19 = 
      ((MdxStudyParameter) mdxDesignStudy_0.getStudyParameters().getObject("P_W_15"));

    mdxStudyParameter_19.setParameterType(MdxStudyParameterBase.ParameterType.CONSTANT);

    MdxStudyParameter mdxStudyParameter_20 = 
      ((MdxStudyParameter) mdxDesignStudy_0.getStudyParameters().getObject("P_W_16"));

    mdxStudyParameter_20.setParameterType(MdxStudyParameterBase.ParameterType.CONSTANT);

    MdxStudyParameter mdxStudyParameter_21 = 
      ((MdxStudyParameter) mdxDesignStudy_0.getStudyParameters().getObject("P_W_17"));

    mdxStudyParameter_21.setParameterType(MdxStudyParameterBase.ParameterType.CONSTANT);

    MdxStudyParameter mdxStudyParameter_22 = 
      ((MdxStudyParameter) mdxDesignStudy_0.getStudyParameters().getObject("P_W_18"));

    mdxStudyParameter_22.setParameterType(MdxStudyParameterBase.ParameterType.CONSTANT);

    MdxStudyParameter mdxStudyParameter_23 = 
      ((MdxStudyParameter) mdxDesignStudy_0.getStudyParameters().getObject("P_W_19"));

    mdxStudyParameter_23.setParameterType(MdxStudyParameterBase.ParameterType.CONSTANT);

    MdxStudyParameter mdxStudyParameter_24 = 
      ((MdxStudyParameter) mdxDesignStudy_0.getStudyParameters().getObject("P_W_20"));

    mdxStudyParameter_24.setParameterType(MdxStudyParameterBase.ParameterType.CONSTANT);

    mdxStudyParameter_0.setParameterType(MdxStudyParameterBase.ParameterType.DISCRETE);

    mdxStudyParameter_0.setParameterType(MdxStudyParameterBase.ParameterType.CONTINUOUS);

    mdxStudyParameter_0.setParameterType(MdxStudyParameterBase.ParameterType.CONSTANT);

    mdxStudyParameter_0.setParameterType(MdxStudyParameterBase.ParameterType.CONTINUOUS);

    MdxContinuousParameterValue mdxContinuousParameterValue_2 = 
      mdxStudyParameter_0.getContinuousParameterValue();

    Units units_1 = 
      ((Units) mdxProject_0.get(UnitsManager.class).getObject("deg"));

    mdxContinuousParameterValue_2.getMaximumQuantity().setUnits(units_1);

    mdxContinuousParameterValue_2.getMaximumQuantity().setValue(7.5);

    mdxContinuousParameterValue_2.getMinimumQuantity().setUnits(units_1);

    mdxContinuousParameterValue_2.getMinimumQuantity().setValue(4.5);

    mdxContinuousParameterValue_2.getMaximumQuantity().setUnits(units_1);

    mdxContinuousParameterValue_2.getMaximumQuantity().setValue(7.0);

    mdxContinuousParameterValue_2.getMinimumQuantity().setUnits(units_1);

    mdxContinuousParameterValue_2.getMinimumQuantity().setValue(6.0);

    mdxStudyParameter_0.setParameterType(MdxStudyParameterBase.ParameterType.CONSTANT);
  }
}
