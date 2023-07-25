// Simcenter STAR-CCM+ macro: Test.java
// Written by Simcenter STAR-CCM+ 16.06.007
package macro;

import java.util.*;

import star.common.*;
import star.base.neo.*;

public class Test extends StarMacro {

  public void execute() {
    execute0();
  }

  private void execute0() {

    Simulation simulation_0 = 
      getActiveSimulation();

    MonitorPlot monitorPlot_1 = 
      ((MonitorPlot) simulation_0.getPlotManager().getPlot("Cd Convergence"));

    Cartesian2DAxisManager cartesian2DAxisManager_1 = 
      ((Cartesian2DAxisManager) monitorPlot_1.getAxisManager());

    Cartesian2DAxis cartesian2DAxis_5 = 
      ((Cartesian2DAxis) cartesian2DAxisManager_1.getAxis("Right Axis"));

    AxisLabels axisLabels_5 = 
      cartesian2DAxis_5.getLabels();

    axisLabels_5.setGridVisible(false);
  }
}
