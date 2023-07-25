// Simcenter STAR-CCM+ macro: ClearHistory.java
// Written by Simcenter STAR-CCM+ 17.01.059-24-g1052f94

import star.common.*;

public class ClearHistoryOnly extends StarMacro {

  @Override
  public void execute() {
    Simulation _sim = getActiveSimulation();
    Solution solution = _sim.getSolution();
    solution.clearSolution(Solution.Clear.History);
  }
}