// Simcenter STAR-CCM+ macro: DeleteSimhRemnants.java
// Written by Simcenter STAR-CCM+ 16.01.040
package eclipse;

import java.io.File;
import java.util.Collection;
import star.common.*;
import star.post.*;

public class DeleteSimhRemnants extends StarMacro {

    @Override
    public void execute() {
        Simulation _sim = getActiveSimulation();
        SolutionHistoryManager simhManager = _sim.get(SolutionHistoryManager.class);
        SolutionViewManager viewManager = _sim.get(SolutionViewManager.class);
        
        Collection<SolutionHistory> histories = simhManager.getSolutionHistories();
        Collection<SolutionView> views = viewManager.getSolutionViews();
        
        if (!histories.isEmpty()) {
            for (SolutionHistory sh : histories) {
                simhManager.remove(sh);
            }
        }

        if (views.size() >= 1) {
            for (SolutionView sv : views) {
                if (sv instanceof RecordedSolutionView) {
                    viewManager.remove(sv);
                }
            }
        }

        _sim.saveState(_sim.getSessionDir() + File.separator + _sim.getPresentationName() + ".sim");
    }
}
