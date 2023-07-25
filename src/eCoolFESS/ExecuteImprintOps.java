// Simcenter STAR-CCM+ macro: Test2.java
// Written by Simcenter STAR-CCM+ 16.04.005
package eCoolFESS;

import java.io.PrintWriter;
import java.io.StringWriter;
import star.common.*;
import star.meshing.*;

public class ExecuteImprintOps extends StarMacro {

    Simulation _sim;

    @Override
    public void execute() {
        _sim = getActiveSimulation();
        try {
            MeshOperationManager mom = _sim.get(MeshOperationManager.class);
            for (MeshOperation moi : mom.getOrderedOperations()) { //getObjectsOf(ImprintPartsOperation.class)) {
                if (moi instanceof ImprintPartsOperation){
                    ImprintPartsOperation ipoi = (ImprintPartsOperation) moi;
                    if (ipoi.getPresentationName().contains("IGBT2") || ipoi.getPresentationName().contains("IGBT3")) {
                        _sim.println(ipoi.getPresentationName() + " WILL NOT execute");
                        continue;
                    }
                    _sim.println(ipoi.getPresentationName() + " WILL execute");
                    ipoi.execute();
                }
            }
        } catch (Exception ex) {
            print(ex);
        }
    }

    private void print(Exception ex) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        ex.printStackTrace(pw);
        _sim.println(sw.toString());
    }
}
