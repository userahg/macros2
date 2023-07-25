// STAR-CCM+ macro: Report_to_csv.java


import java.util.*;
import java.io.*;
import star.common.*;
import star.base.report.*;

public class ExportReportFixed4_19d1 extends StarMacro {

    BufferedWriter bwout = null;

    @Override
    public void execute() {

        try {

            Simulation simulation_0 = getActiveSimulation();

// Collecting the simualtion file name and session dir
            String simulationName = simulation_0.getPresentationName();
            simulation_0.println("Simulation Name:" + simulationName);

            String dir = simulation_0.getSessionDir();

            String sep = System.getProperty("file.separator");
            simulation_0.println("Report CSV File Name:" + dir + sep + simulationName + "report.csv");

// Open Buffered Input and Output Readers
// Creating file with name "<sim_file_name>+report.csv" 
            bwout = new BufferedWriter(new FileWriter(resolvePath(dir + sep + simulationName + "report.csv")));
            bwout.write("Report Name, Value, Unit, \n");

// Adding Global Parameters Routine (Jaime Edit 02/12/18)
            GlobalParameterManager globalPMN = simulation_0.get(GlobalParameterManager.class);

            Collection<ScalarGlobalParameter> all_globalP = globalPMN.getObjectsOf(ScalarGlobalParameter.class);

            String BC_id;
            double p_value;

            for (ScalarGlobalParameter i_BC : all_globalP) {

                BC_id = i_BC.getPresentationName();

                p_value = i_BC.getQuantity().getInternalValue();
                simulation_0.println("GlobalParam: " + BC_id + " = " + p_value);

// Write Output file as "sim file name"+report.csv
                bwout.write(BC_id + ", " + p_value + " " + "\n");

            }

// Extracting Report Values Routine
            Collection<Report> reportCollection = simulation_0.getReportManager().getObjects();

            for (Report thisReport : reportCollection) {

                String fieldLocationName = thisReport.getPresentationName();
                Double fieldValue = thisReport.getReportMonitorValue();
                String fieldUnits = thisReport.getUnits().toString();

// Printing to chek in output window
                simulation_0.println("Field Location :" + fieldLocationName);
                simulation_0.println(" Field Value :" + fieldValue);
                simulation_0.println(" Field Units :" + fieldUnits);
                simulation_0.println("");

// Write Output file as "sim file name"+report.csv
                bwout.write(fieldLocationName + ", " + fieldValue + ", " + fieldUnits + "\n");
            }
            bwout.close();
        } catch (IOException iOException) {
        }

    }
}
