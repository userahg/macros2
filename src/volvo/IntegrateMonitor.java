package volvo;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.io.PrintWriter;
import java.io.StringWriter;
import star.base.report.ExpressionReport;
import star.common.*;
import star.common.graph.DataSet;

/**
 *
 * @author cd8unu
 */
public class IntegrateMonitor extends StarMacro {

    Simulation _sim;
    String _plot = "Thickness";
    String _monitor = "Avg Thickness Monitor";
    String _result = "TimeIntegralThickness";

    @Override
    public void execute() {
        _sim = getActiveSimulation();
        
        MonitorPlot mp = (MonitorPlot) _sim.getPlotManager().getPlot(_plot);
        
        try {
            double val =  integrate(mp, _monitor);
            ExpressionReport er = (ExpressionReport) _sim.getReportManager().getReport(_result);
            er.setDefinition(Double.toString(val));
            _sim.println("Integration val = " + val);
        } catch (Exception ex) {
            print(ex);
        }
    }

    public double integrate(MonitorPlot monitorPlot, String monitorName) throws Exception {
        DataSet dataSet = null;
        for(DataSet ds : monitorPlot.getDataSetCollection()) {
            if (ds.getDataSourceName().equals(monitorName)) {
                dataSet = ds;
            }
        }
        
        if (dataSet == null) {
            throw new NullPointerException();
        }
        
        double xVals[] = dataSet.getXDataArray();
        double yVals[] = dataSet.getYDataArray();

        double integration = 0;
        for (int i = 1; i < xVals.length; i++) {
            double y2 = yVals[i];
            double y1 = yVals[i - 1];
            double dx = xVals[i] - xVals[i - 1];
            double tmp = dx * (y2 - 0.5 * (y2 - y1));
            integration += tmp;
        }
        return integration;
    }
    
    private void print(Exception ex) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        ex.printStackTrace(pw);
        _sim.print(sw.toString());
    }
}