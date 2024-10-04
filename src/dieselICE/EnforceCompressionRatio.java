/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dieselICE;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;
import star.common.Simulation;
import star.common.StarMacro;
import star.starcad2.StarCadDesignParameterDouble;
import star.starcad2.StarCadDocument;
import star.starcad2.StarCadDocumentManager;

/**
 *
 * @author cd8unu
 */
public class EnforceCompressionRatio extends StarMacro {

    static final String RUN_JOURNAL_SCRIPT = "/u/cd8unu/projects/ICE/CAD/python/run_journal.sh";
    String cadFileName = "ICE_Demo_Model.prt";
    String floatingParamName = "SCALE_FACTOR";
    String crMacro = "CR_opt.py";
    Simulation _sim;

    @Override
    public void execute() {
        _sim = getActiveSimulation();
        String oldPath = _sim.getSessionPath();
        StarCadDocument doc = getActiveSimulation().get(StarCadDocumentManager.class).getDocument(cadFileName);
        doc.updateModelAndSave();
        _sim.saveState(oldPath);
        runCompressionRatioMacro(doc);
        readScaleFactor(doc);
        doc.updateModel();
    }

    private void runCompressionRatioMacro(StarCadDocument doc) {
        String py_mac = doc.getCadFileDir() + File.separator + "python" + File.separator + crMacro;
        ProcessBuilder pb = new ProcessBuilder(RUN_JOURNAL_SCRIPT, py_mac);
        try {
            Process proc = pb.inheritIO().start();
            proc.waitFor(2l, TimeUnit.MINUTES);
        } catch (IOException | InterruptedException ex) {
            print(ex);
        }
    }

    private void readScaleFactor(StarCadDocument doc) {
        try (BufferedReader br = new BufferedReader(new FileReader(doc.getCadFileDir() + File.separator + "scale_factor"))) {
            String scale_factor = br.readLine();
            StarCadDesignParameterDouble scale_factor_param = (StarCadDesignParameterDouble) doc.getStarCadDesignParameters().getObject(getParameterPresentationName());
            scale_factor_param.getQuantity().setDefinition(scale_factor);
        } catch (Exception ex) {
            print(ex);
        } finally {
            try {
                Files.delete(Paths.get(doc.getCadFileDir() + File.separator + "scale_factor"));
            } catch (IOException ex) {
                print(ex);
            }
        }
    }
    
    private String getParameterPresentationName() {
        return cadFileName + "\\" + floatingParamName;
    }

    private void print(Exception ex) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        ex.printStackTrace(pw);
        _sim.println(sw);
    }
}
