// Simcenter STAR-CCM+ macro: test.java
// Written by Simcenter STAR-CCM+ 18.03.038
package macro;

import java.util.*;

import star.common.*;
import star.base.neo.*;
import star.cadmodeler.*;

public class test extends StarMacro {

  public void execute() {
    execute0();
  }

  private void execute0() {

    Simulation simulation_0 = 
      getActiveSimulation();

    CadModel cadModel_0 = 
      ((CadModel) simulation_0.get(SolidModelManager.class).getObject("3D-CAD Model 1"));

    VectorQuantityDesignParameter vectorQuantityDesignParameter_0 = 
      ((VectorQuantityDesignParameter) cadModel_0.getDesignParameterManager().getObject("CAD_BRANCH1_PLANE2_OFFSET"));

    Units units_3 = 
      ((Units) simulation_0.getUnitsManager().getObject("mm"));

    vectorQuantityDesignParameter_0.getQuantity().setComponentsAndUnits(1.0E-5, 7.0, 15.0, units_3);

    vectorQuantityDesignParameter_0.getQuantity().setComponentsAndUnits(0.0, 7.0, 15.0, units_3);

    VectorQuantityDesignParameter vectorQuantityDesignParameter_1 = 
      ((VectorQuantityDesignParameter) cadModel_0.getDesignParameterManager().getObject("CAD_BRANCH1_PLANE2_ROTATION"));

    Units units_4 = 
      ((Units) simulation_0.getUnitsManager().getObject("deg"));

    vectorQuantityDesignParameter_1.getQuantity().setComponentsAndUnits(-30.01, 0.0, 0.0, units_4);

    vectorQuantityDesignParameter_1.getQuantity().setComponentsAndUnits(-30.0, 0.0, 0.0, units_4);

    ScalarQuantityDesignParameter scalarQuantityDesignParameter_0 = 
      ((ScalarQuantityDesignParameter) cadModel_0.getDesignParameterManager().getObject("CAD_BRANCH1_RADIUS_2"));

    scalarQuantityDesignParameter_0.getQuantity().setValueAndUnits(10.1, units_3);

    scalarQuantityDesignParameter_0.getQuantity().setValueAndUnits(10.0, units_3);
  }
}
