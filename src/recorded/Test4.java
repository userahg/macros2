// Simcenter STAR-CCM+ macro: Test4.java
// Written by Simcenter STAR-CCM+ 15.04.005
package recorded;

import java.io.PrintWriter;
import java.io.StringWriter;
import star.common.*;
import star.post.*;

public class Test4 extends StarMacro {

    @Override
    public void execute() {
        execute0();
    }

    private void execute0() {

        Simulation simulation_0
                = getActiveSimulation();

        try {
            RecordedSolutionView recordedSolutionView_1 = ((RecordedSolutionView) simulation_0.get(SolutionViewManager.class).getObject("Test1"));
            if (recordedSolutionView_1 != null) {
                simulation_0.println(recordedSolutionView_1.toString());
            } else {
                simulation_0.println("Object not found.");
            }
        } catch (Exception ex) {
            if (ex.getMessage().contains("does not exist in manager")) {
                
            }
            
        }

    }
}
