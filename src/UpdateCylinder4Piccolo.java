// STAR-CCM+ macro: T.java
// Written by STAR-CCM+ 14.02.010


import star.common.*;
import star.base.neo.*;
import star.base.report.ExpressionReport;
import star.vis.*;

public class UpdateCylinder4Piccolo extends StarMacro {

    Simulation _sim;
    double _xPos;
    double _zPos;
    double _rad;

    @Override
    public void execute() {
        _sim = getActiveSimulation();

        getDoubles();

        CylinderSection cylSection = ((CylinderSection) _sim.getPartManager().getObject("Cylinder Section"));
        Units m = ((Units) _sim.getUnitsManager().getObject("m"));
        cylSection.getOriginCoordinate().setCoordinate(m, m, m, new DoubleVector(new double[]{_xPos, 0.0, _zPos}));
        cylSection.getRadius().setValue(_rad);
    }

    private void getDoubles() {
        ExpressionReport xPosReport = (ExpressionReport) _sim.getReportManager().getReport("PiccoloXPos");
        ExpressionReport zPosReport = (ExpressionReport) _sim.getReportManager().getReport("PiccoloZPos");
        ExpressionReport midPlaneR = (ExpressionReport) _sim.getReportManager().getReport("PiccoloMidPlaneR");
        _xPos = xPosReport.getReportMonitorValue();
        _zPos = zPosReport.getReportMonitorValue();
        _rad = midPlaneR.getReportMonitorValue();
    }
}
