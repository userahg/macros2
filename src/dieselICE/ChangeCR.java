// Simcenter STAR-CCM+ macro: ChangeCR.java
// Written by Simcenter STAR-CCM+ 17.04.007
package macro;

import java.util.*;

import star.starice.*;
import star.common.*;
import star.base.neo.*;
import star.vis.*;

public class ChangeCR extends StarMacro {

  public void execute() {
    execute0();
  }

  private void execute0() {

    Simulation simulation_0 = 
      getActiveSimulation();

    StarIceEngine starIceEngine_0 = 
      simulation_0.get(StarIceEngine.class);

    starIceEngine_0.setCompressionRatio(15.6);

    starIceEngine_0.updateStarIce();

    Scene scene_0 = 
      simulation_0.getSceneManager().getScene("In-Cylinder 1");

    SceneUpdate sceneUpdate_0 = 
      scene_0.getSceneUpdate();

    HardcopyProperties hardcopyProperties_0 = 
      sceneUpdate_0.getHardcopyProperties();

    hardcopyProperties_0.setCurrentResolutionWidth(1498);

    hardcopyProperties_0.setCurrentResolutionHeight(603);
  }
}
