// Simcenter STAR-CCM+ macro: Test5.java
// Written by Simcenter STAR-CCM+ 15.04.005
package macro;

import java.util.*;

import star.common.*;
import star.base.neo.*;
import star.base.report.*;

public class Test5 extends StarMacro {

  public void execute() {
    execute0();
  }

  private void execute0() {

    Simulation simulation_0 = 
      getActiveSimulation();

    ExpressionReport expressionReport_0 = 
      ((ExpressionReport) simulation_0.getReportManager().getReport("Expression 1"));

    simulation_0.get(CommentManager.class).setCommentsFor(Arrays.<ClientServerObject>asList(expressionReport_0), "Drag1");
    
    Comment c = simulation_0.get(CommentManager.class).getCommentFor(expressionReport_0);
    c.getText();
  }
}
