// Simcenter STAR-CCM+ macro: test.java
// Written by Simcenter STAR-CCM+ 17.04.007
package macro;

import java.util.*;

import star.base.neo.*;
import star.common.*;
import star.starcad2.*;

public class test extends StarMacro {

  public void execute() {
    execute0();
  }

  private void execute0() {

    StarCadDocument starCadDocument_0 = 
      getActiveSimulation().get(StarCadDocumentManager.class).getDocument("ICE_Demo_Model.prt");

    StarCadScalarDesignReport starCadScalarDesignReport_0 = 
      ((StarCadScalarDesignReport) starCadDocument_0.hasStarCadDesignReports().getDesignReport("ICE_Demo_Model.prt\\OUT_CR_NOW"));

    StarCadScalarFunctionProvider starCadScalarFunctionProvider_0 = 
      starCadScalarDesignReport_0.getFunctionProvider();

    starCadScalarFunctionProvider_0.setPresentationName("ICE_Demo_Model.prt\\OUT_CR_NOW1");
  }
}
