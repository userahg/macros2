// Simcenter STAR-CCM+ macro: dumpSensitivities.java
package ga;

import java.util.*;
import java.io.*;
import java.nio.charset.*;
import java.util.List;
import star.common.*;
import star.base.neo.*;
import star.base.report.*;

public class DumpSensitivities extends StarMacro {

    @Override
    public void execute() {
        Simulation simulation_0 = getActiveSimulation();

        // Collect list of parameters
        GlobalParameterManager paramManager = simulation_0.get(GlobalParameterManager.class);
        List<String> sParameterList = Arrays.asList("D1x", "D1y", "D2x", "D2y");
        LongVector paramIds = new LongVector();
        for (String p : sParameterList) {
            paramIds.add(paramManager.getObject(p).getObjectId());
        }

        // write sensitivities to file
        Report pd = simulation_0.getReportManager().getObject("Pressure Drop 1");
        writeSensitivitiesToFile(pd, paramIds, pd.getPresentationName() + ".sens");

    }

    // To dump sensitivities of a given report into a file (reportOp.sens)
    // report : actual report where sim will calculate sensitivities
    // paramIds : parameters Ids that participate in Geometric Sensitivity / SQP
    // fileName : file name of the sensitivities file.
    private void writeSensitivitiesToFile(Report report, LongVector paramIds, String fileName) {
        final String SENSITIVITIES_TAG = "Sensitivities"; // NOI18N
        final String PARAMETER_ID_TAG = "ParameterId"; // NOI18N
        final String SENSITIVITY_TAG = "Sensitivity"; // NOI18N

        // Get sensitivities from adjoint solver.
        Simulation simulation_0 = getActiveSimulation();
        AdjointSolver adjointSolver = simulation_0.getSolverManager().getSolver(AdjointSolver.class);
        GlobalParameterManager paramManager = simulation_0.get(GlobalParameterManager.class);
        ReportCostFunction reportCostFunc = hasReportCostFunction(report.getObjectId());

        if (adjointSolver != null && paramManager != null && reportCostFunc != null) {
            Vector<NeoProperty> gProps = new Vector<>();
            // Check if sensitivity is available for Simulation Parameters
            for (NamedObject parameter : paramManager.getObjects()) {
                if (parameter instanceof ScalarGlobalParameter
                        && paramIds.contains(parameter.getObjectId())) {
                    ScalarGlobalParameter scalarParameter = (ScalarGlobalParameter) parameter;
                    if (adjointSolver.isGeometricParameterSensitivityAvailable(reportCostFunc, scalarParameter)) {

                        // NOTE: get Sensitivity value in reference Units of Report/Parameter dimensions.
                        double sensitivityInUnits = adjointSolver.getGeometricParameterSensitivityValueInUnits(reportCostFunc, scalarParameter);

                        NeoProperty gProp = new NeoProperty();
                        gProp.put(PARAMETER_ID_TAG, parameter.getObjectId());
                        gProp.put(SENSITIVITY_TAG, sensitivityInUnits);
                        gProps.add(gProp);
                        simulation_0.println("\tSensitivity for " + reportCostFunc.getPresentationName()
                                + " w.r.t to " + parameter.getPresentationName()
                                + " : " + sensitivityInUnits);
                    }
                }
            }
            NeoProperty out = new NeoProperty();
            out.put(SENSITIVITIES_TAG, gProps);
            String sensFilePath = NeoFileUtils.getAbsolutePath(NeoFileUtils.getPwd(), new File(fileName)); // NOI18N
            writeToFile(sensFilePath, out.toString());
        }
    }

    // Helper method to get report cost function for a given report object id.
    private ReportCostFunction hasReportCostFunction(long reportId) {
        AdjointCostFunctionManager costFuncManager = getActiveSimulation().get(AdjointCostFunctionManager.class);
        ReportCostFunction reportCostFunc = null;
        if (costFuncManager != null) {
            // Get the report cost function.
            for (AdjointCostFunction cost : costFuncManager.getObjects()) {
                // Get cost function of the report
                if (cost instanceof ReportCostFunction && ((ReportCostFunction) cost).getReport().getObjectId() == reportId) {
                    reportCostFunc = (ReportCostFunction) cost;
                }
            }
        }
        return reportCostFunc;
    }

    private boolean writeToFile(String path, String str) {
        try {
            File file = new File(path);
            BufferedWriter fileWriter = new BufferedWriter(
                    new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8));
            if (str != null) { // Allow to write empty file.
                fileWriter.write(str);
            }
            fileWriter.flush();
            fileWriter.close();
        } catch (Exception e) {
            getActiveSimulation().println("Error : Writing file has failed " + path);
            return false;
        }
        return true;
    }

}
