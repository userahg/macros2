// Simcenter STAR-CCM+ macro: Test3.java
// Written by Simcenter STAR-CCM+ 15.04.005
package macro;

import java.util.*;

import star.common.*;
import star.base.neo.*;
import star.post.*;

public class Test3 extends StarMacro {

  public void execute() {
    execute0();
  }

  private void execute0() {

    Simulation simulation_0 = 
      getActiveSimulation();

    SolutionHistory solutionHistory_1 = 
      simulation_0.get(SolutionHistoryManager.class).createForFile(resolvePath("C:\\Users\\cd8unu\\Desktop\\Test.simh"), false, false);

    solutionHistory_1.setAutoRecord(false);

    solutionHistory_1.setAddReportFieldFunctions(true);

    solutionHistory_1.getInputs().setQuery(null);

    Region region_0 = 
      simulation_0.getRegionManager().getRegion("Region");

    Boundary boundary_1 = 
      region_0.getBoundaryManager().getBoundary("Body 1.Lid");

    solutionHistory_1.getInputs().setObjects(boundary_1);

    PrimitiveFieldFunction primitiveFieldFunction_3 = 
      ((PrimitiveFieldFunction) simulation_0.getFieldFunctionManager().getFunction("AbsolutePressure"));

    PrimitiveFieldFunction primitiveFieldFunction_4 = 
      ((PrimitiveFieldFunction) simulation_0.getFieldFunctionManager().getFunction("Density"));

    PrimitiveFieldFunction primitiveFieldFunction_5 = 
      ((PrimitiveFieldFunction) simulation_0.getFieldFunctionManager().getFunction("Velocity"));

    VectorMagnitudeFieldFunction vectorMagnitudeFieldFunction_0 = 
      ((VectorMagnitudeFieldFunction) primitiveFieldFunction_5.getMagnitudeFunction());

    VectorComponentFieldFunction vectorComponentFieldFunction_0 = 
      ((VectorComponentFieldFunction) primitiveFieldFunction_5.getComponentFunction(0));

    VectorComponentFieldFunction vectorComponentFieldFunction_1 = 
      ((VectorComponentFieldFunction) primitiveFieldFunction_5.getComponentFunction(1));

    VectorComponentFieldFunction vectorComponentFieldFunction_2 = 
      ((VectorComponentFieldFunction) primitiveFieldFunction_5.getComponentFunction(2));

    PrimitiveFieldFunction primitiveFieldFunction_6 = 
      ((PrimitiveFieldFunction) simulation_0.getFieldFunctionManager().getFunction("WallShearStress"));

    VectorMagnitudeFieldFunction vectorMagnitudeFieldFunction_1 = 
      ((VectorMagnitudeFieldFunction) primitiveFieldFunction_6.getMagnitudeFunction());

    VectorComponentFieldFunction vectorComponentFieldFunction_3 = 
      ((VectorComponentFieldFunction) primitiveFieldFunction_6.getComponentFunction(0));

    VectorComponentFieldFunction vectorComponentFieldFunction_4 = 
      ((VectorComponentFieldFunction) primitiveFieldFunction_6.getComponentFunction(1));

    VectorComponentFieldFunction vectorComponentFieldFunction_5 = 
      ((VectorComponentFieldFunction) primitiveFieldFunction_6.getComponentFunction(2));

    solutionHistory_1.setFunctions(new NeoObjectVector(new Object[] {primitiveFieldFunction_3, primitiveFieldFunction_4, primitiveFieldFunction_5, vectorMagnitudeFieldFunction_0, vectorComponentFieldFunction_0, vectorComponentFieldFunction_1, vectorComponentFieldFunction_2, primitiveFieldFunction_6, vectorMagnitudeFieldFunction_1, vectorComponentFieldFunction_3, vectorComponentFieldFunction_4, vectorComponentFieldFunction_5}));

    solutionHistory_1.createSnapshot("Test1");

    RecordedSolutionView recordedSolutionView_1 = 
      solutionHistory_1.createRecordedSolutionView(false);

    solutionHistory_1.createSnapshot("Test3");

    solutionHistory_1.rescanFile();

    recordedSolutionView_1.setStateName("Test3");
    
    recordedSolutionView_1.getSo
  }
}
