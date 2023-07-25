// Simcenter STAR-CCM+ macro: Test.java
// Written by Simcenter STAR-CCM+ 16.03.083
package surfLoch;

import star.common.*;
import star.starcad2.StarCadDesignParameterDouble;
import star.starcad2.StarCadDocument;
import star.starcad2.StarCadDocumentManager;
import star.starcad2.StarCadScalarFunctionProvider;
import star.vis.GraphicsPeriodicRepeat;
import star.vis.PeriodicRepeat;

public class UpdateGraphicsRepeats extends StarMacro {

    @Override
    public void execute() {
        Simulation _sim = getActiveSimulation();

        StarCadDocument starCADDoc = getActiveSimulation().get(StarCadDocumentManager.class).getDocument("Full Model Assembly_stp.prt");
        StarCadDesignParameterDouble nBladesParam = (StarCadDesignParameterDouble) starCADDoc.getStarCadDesignParameters().getParameter("Blade and Hub subassembly.prt\\P_Number_Of_Blades");
        StarCadScalarFunctionProvider nBladesFunctionProvider = nBladesParam.getFunctionProvider();
        double nBlades = nBladesFunctionProvider.getQuantity().evaluate();

        PeriodicRepeat periodicRepeat1 = (PeriodicRepeat) _sim.getTransformManager().getObject("Periodic");
        periodicRepeat1.setNumberOfRepeats(((int) nBlades) - 1);
        
        double rotationAngle = periodicRepeat1.getRotationAngle();
        _sim.println("Rotation Angle: " + rotationAngle);

        Units deg = (Units) _sim.getUnitsManager().getObject("degrees");

        GraphicsPeriodicRepeat periodicRepeat3 = (GraphicsPeriodicRepeat) _sim.getTransformManager().getObject("Periodic 3");
        periodicRepeat3.getRotationAngleQuantity().setValue(-rotationAngle);
        periodicRepeat3.getRotationAngleQuantity().setUnits(deg);

        GraphicsPeriodicRepeat periodicRepeat4 = (GraphicsPeriodicRepeat) _sim.getTransformManager().getObject("Periodic 4");
        periodicRepeat4.getRotationAngleQuantity().setValue(-rotationAngle);
        periodicRepeat4.getRotationAngleQuantity().setUnits(deg);
    }
}
