/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dieselICE;

import star.base.report.Report;
import star.common.Simulation;
import star.common.StarMacro;
import star.starcad2.StarCadDesignParameterDouble;
import star.starcad2.StarCadDocumentManager;

/**
 *
 * @author cd8unu
 */
public class WriteCompressionRatios extends StarMacro {

    Simulation _sim;
    double target_cr = 14.5;

    @Override
    public void execute() {
        _sim = getActiveSimulation();
        StarCadDesignParameterDouble scaleFactor = (StarCadDesignParameterDouble) _sim.get(StarCadDocumentManager.class).getDocument("ICE_Demo_Model.prt").
                getStarCadDesignParameters().getParameter("ICE_Demo_Model.prt\\SCALE_FACTOR");
        Report report1 = _sim.getReportManager().getReport("Compression Ratio NX");
        Report report2 = _sim.getReportManager().getReport("Compression Ratio 1");
        double cr1 = report1.getReportMonitorValue();
        double cr2 = report2.getReportMonitorValue();
        double sc_fact = scaleFactor.getQuantity().getRawValue();

        double cr_diff = cr1 - target_cr;

        _sim.println("Extract: " + _sim.getPresentationName() + "," + Double.toString(sc_fact) + "," + Double.toString(cr2) + "," + Double.toString(cr1) + "," + Double.toString(cr_diff));
                
    }

}
