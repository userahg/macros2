
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import star.base.report.ExpressionReport;
import star.base.report.Report;
import star.base.report.ReportManager;
import star.common.Simulation;
import star.common.StarMacro;
import star.common.StarPlot;
import star.vis.Scene;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author cd8unu
 */
public class RunMeshTest extends StarMacro {

    Simulation _sim;

    @Override
    public void execute() {
        _sim = getActiveSimulation();
        checkUseTetOctree();
        _sim.getMeshPipelineController().clearGeneratedMeshes();

        mesh();
        exportResults();
        writeImgs();
        writeDoneFile();
    }

    private void mesh() {
        long starttime = System.nanoTime();
        _sim.getMeshPipelineController().generateVolumeMesh();
        long endtime = System.nanoTime();
        long elapsedtime = endtime - starttime;

        ExpressionReport er = (ExpressionReport) _sim.getReportManager().getReport("Mesh Time");
        er.setDefinition(Long.toString(elapsedtime / 1000000000l));
    }

    private void exportResults() {
        try (BufferedWriter out = new BufferedWriter(new FileWriter(_sim.getSessionDir() + File.separator + "results.out"))) {
            HashMap<Integer, Report> reports = getOrderedReports();

            for (int i = 1; i <= 8; i++) {
                out.write(Double.toString(reports.get(i).getReportMonitorValue()));
                out.newLine();
            }

            out.flush();
        } catch (IOException ex) {
            print(ex);
        }
    }

    private void writeImgs() {
        for (StarPlot sp : _sim.getPlotManager().getPlots()) {
            sp.encode(_sim.getSessionDir() + File.separator + sp.getPresentationName() + ".png", "png", 1920, 1080);
        }

        for (Scene s : _sim.getSceneManager().getScenes()) {
            s.printAndWait(s.getPresentationName() + ".png");
        }
    }

    private HashMap<Integer, Report> getOrderedReports() {
        HashMap<Integer, Report> reports = new HashMap<>();
        ReportManager rm = _sim.getReportManager();
        reports.put(1, rm.getReport("Cell Quality Avg"));
        reports.put(2, rm.getReport("Cell Quality Min"));
        reports.put(3, rm.getReport("Cell Quality St Dev"));
        reports.put(4, rm.getReport("Volume Change Avg"));
        reports.put(5, rm.getReport("Volume Change Min"));
        reports.put(6, rm.getReport("Volume Change St Dev"));
        reports.put(7, rm.getReport("Cells"));
        reports.put(8, rm.getReport("Mesh Time"));

        return reports;
    }

    private void writeDoneFile() {
        try (BufferedWriter out = new BufferedWriter(new FileWriter(_sim.getSessionDir() + File.separator + "DONE"))) {
            out.write("DONE");
            out.flush();
        } catch (IOException ex) {
            print(ex);
        }
    }

    private void print(Exception ex) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        ex.printStackTrace(pw);
        _sim.println(sw.toString());
    }
    
    private void checkUseTetOctree() {
        String tetOctree = System.getenv("TET_OCTREE");
        if (tetOctree == null || tetOctree.isEmpty()) {
            _sim.println("TET_OCTREE environment variable is not set.");
        } else {
            _sim.println("TET_OCTREE environment variable is set to " + tetOctree);
        }
    }
}
