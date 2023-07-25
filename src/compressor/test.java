// Simcenter STAR-CCM+ macro: test.java
// Written by Simcenter STAR-CCM+ 18.02.008
package macro;

import java.util.*;

import star.base.neo.*;
import star.mdx.*;

public class test extends MdxMacro {

  public void execute() {
    execute0();
  }

  private void execute0() {

    MdxProject mdxProject_0 = 
      getActiveMdxProject();

    MdxDesignStudy mdxDesignStudy_0 = 
      mdxProject_0.getDesignStudyManager().getDesignStudy("CADR");

    MdxStudyParameter mdxStudyParameter_0 = 
      ((MdxStudyParameter) mdxDesignStudy_0.getStudyParameters().getObject("ParametricBlade.prt\\p1P_W_L0"));

    mdxStudyParameter_0.setPresentationName("p1P_W_L0");
  }
}
