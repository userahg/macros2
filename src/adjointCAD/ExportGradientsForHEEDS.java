/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package adjointCAD;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import star.common.AdjointCostFunction;
import star.common.AdjointCostFunctionManager;
import star.common.AdjointSolver;
import star.common.GlobalParameterManager;
import star.common.ScalarGlobalParameter;
import star.common.Simulation;
import star.common.StarMacro;

/**
 *
 * @author cd8unu
 */
public class ExportGradientsForHEEDS extends StarMacro {

    Simulation _sim;
    AdjointSolver _adjointSolver;
    GlobalParameterManager _paramManager;
    AdjointCostFunctionManager _cfManager;
    String[] _cadParams = {"D1x", "D1y", "D2x", "D2y"};
    String[] _unfilteredSenReports = {"gradFwrtD1xUnfiltered", "gradFwrtD1yUnfiltered", "gradFwrtD2xUnfiltered", "gradFwrtD2yUnfiltered"};
    String[] _filteredSenReports = {"gradFwrtD1xFiltered", "gradFwrtD1yFiltered", "gradFwrtD2xFiltered", "gradFwrtD2yFiltered"};
    String _pressureDrop1 = "PressureDropAdj";
    String _pressureDrop2 = "PressureDropFiltered";
    String _pressureDrop3 = "PressureDropUnfiltered";

    @Override
    public void execute() {
        _sim = getActiveSimulation();
        _adjointSolver = ((AdjointSolver) _sim.getSolverManager().getSolver(AdjointSolver.class));
        _paramManager = _sim.get(GlobalParameterManager.class);
        _cfManager = _sim.get(AdjointCostFunctionManager.class);

        writeHEEDSInfo();

    }

    private double getSensitivityAdj(String response, String param) {
        ScalarGlobalParameter sgp = (ScalarGlobalParameter) _paramManager.getObject(param);
        AdjointCostFunction acf = _cfManager.getObject(response);
        return _adjointSolver.getGeometricParameterSensitivityValue(acf, sgp);
    }

    private double getSensitivityMan(String er) {
        return _sim.getReportManager().getReport(er).getReportMonitorValue();
    }

    private void writeHEEDSInfo() {

        String directory = _sim.getSessionDirFile().getParent();

        try (FileWriter fw = new FileWriter(directory + File.separator + "HEEDSGrads.info");
                BufferedWriter bw = new BufferedWriter(fw)) {
            bw.write(writeHEEDSInfo(_pressureDrop1));
            bw.newLine();
            bw.write(writeHEEDSInfo(_pressureDrop2));
            bw.newLine();
            bw.write(writeHEEDSInfo(_pressureDrop3));
            bw.flush();
            bw.close();
            fw.close();
        } catch (IOException ioex) {
        }

    }

    private String writeHEEDSInfo(String response) {
        boolean fromAdjoint = response.contains("Adj");
        boolean fromUnfiltered = response.endsWith("Unfiltered");
        
        String infomsg = "";
        infomsg += "$Data for response " + response + "\n";
        infomsg += "*RESPONSE_GRADIENT_INFO\n";
        infomsg += "responseName: " + response + "\n";
        for (String s : _cadParams) {
            if (fromAdjoint) {
                infomsg += s + ": " + getSensitivityAdj(response, s) + "\n";
            } else if (fromUnfiltered) {
                String gradReport = "";
                for (String ss : _unfilteredSenReports) {
                    if (ss.contains(s)) {
                        gradReport = ss;
                        break;
                    }
                }
                infomsg += s + ": " + getSensitivityMan(gradReport) + "\n";
            } else {
                String gradReport = "";
                for (String ss : _filteredSenReports) {
                    if (ss.contains(s)) {
                        gradReport = ss;
                        break;
                    }
                }
                infomsg += s + ": " + getSensitivityMan(gradReport) + "\n";
            }
        }
        return infomsg;
    }

}
