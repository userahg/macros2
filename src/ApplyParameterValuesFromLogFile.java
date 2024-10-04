
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Set;
import java.util.Map.Entry;
import java.util.regex.Pattern;
import star.cadmodeler.CadModel;
import star.cadmodeler.DesignParameter;
import star.cadmodeler.DesignParameterManager;
import star.cadmodeler.SolidModelManager;
import star.common.GlobalParameterManager;
import star.common.ScalarGlobalParameter;
import star.common.ScalarPhysicalQuantity;
import star.common.Simulation;
import star.common.StarMacro;
import star.starcad2.StarCadDesignParameterDouble;
import star.starcad2.StarCadDesignParameterManager;
import star.starcad2.StarCadDocument;
import star.starcad2.StarCadDocumentManager;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author cd8unu
 */
public class ApplyParameterValuesFromLogFile extends StarMacro {

    Simulation _sim;
    GlobalParameterManager paramManager;
    StarCadDocumentManager docManager;
    SolidModelManager cadManager;
    HashMap<String, Double> paramVals;
    String logFile = "D:\\Workdir\\projects\\2023\\compressor\\TAWCompliant\\dx\\proj_taw2\\Opt1\\Design_1\\Design_1.log";
    boolean printValues = true;
    boolean printIfNotMatching = true;
    boolean assignValues = true;

    @Override
    public void execute() {
        initialize();
        try {
            readValuesFromLog();
            assignValues(printValues, printIfNotMatching, assignValues);
        } catch (Exception ex) {
            print(ex);
        }

    }

    private void readValuesFromLog() {
        String regex = "^\\t\\t.* : [\\+\\-\\d]+[.\\d+]?.*$";
        String hasUnitsRegex = "\\(.*\\)";

        Pattern p1 = Pattern.compile(regex);
        Pattern p2 = Pattern.compile(hasUnitsRegex);

        try (BufferedReader br = new BufferedReader(new FileReader(logFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (p1.matcher(line).find()) {
                    String[] split = line.split(":");
                    String name = split[0].trim();
                    String value = split[1];
                    if (p2.matcher(value).find()) {
                        value = value.replaceAll(hasUnitsRegex, "");
                    }
                    Double val = Double.parseDouble(value);
                    paramVals.put(name, val);
                }
            }
        } catch (IOException ioex) {
            print(ioex);
        }
    }

    private void assignValues(boolean print, boolean printIfNotMatching, boolean assign) {
        Set<Entry<String, Double>> entries = paramVals.entrySet();
        int total = entries.size();
        int match = 0;
        int noMatch = 0;
        int valsDontMatch = 0;
        for (Entry<String, Double> entry : entries) {
            String name = entry.getKey();
            Double newVal = entry.getValue();
            if (print) {
                _sim.println(name + " = " + Double.toString(newVal));
            }
            boolean hasSGP = hasScalarGlobalParameter(name);
            boolean hasSCDPD = hasStarCADDesignParameterDouble(name);
            if (hasSGP && hasSCDPD) {
                noMatch++;
                _sim.println("Multiple matches found for " + name);
            } else if (hasSGP) {
                match++;
                ScalarGlobalParameter p = getScalarGlobalParameter(name);
                Double oldVal = p.getQuantity().getRawValue();
                if (!oldVal.equals(newVal)) {
                    valsDontMatch++;
                    if (printIfNotMatching) {
                        _sim.println(Double.toString(oldVal) + " doesn't match " + Double.toString(newVal) + " for " + name);
                    }
                }
                if (assign) {
                    p.getQuantity().setValue(newVal);
                }
            } else if (hasSCDPD) {
                match++;
                StarCadDesignParameterDouble p = getStarCadDesignParameterDouble(name);
                Double oldVal = p.getQuantity().getRawValue();
                if (!oldVal.equals(newVal)) {
                    valsDontMatch++;
                    if (printIfNotMatching) {
                        _sim.println(Double.toString(oldVal) + " doesn't match " + Double.toString(newVal) + " for " + name);
                    }
                }
                if (assign) {
                    p.getQuantity().setValue(newVal);
                }
            } else {
                noMatch++;
            }
        }
        _sim.println(Integer.toString(total) + " parameters extracted from log file:");
        _sim.println("\t" + Integer.toString(match) + " were matched");
        _sim.println("\t" + Integer.toString(noMatch) + " were not matched");
        if (printIfNotMatching) {
            _sim.println("\t" + Integer.toString(valsDontMatch) + " didn't match the new value");
        }
    }

    private boolean isLiteral(ScalarPhysicalQuantity spq) {
        return !spq.getIsExpression();
    }

    private boolean hasScalarGlobalParameter(String name) {
        boolean hasParam = false;
        for (ScalarGlobalParameter p : paramManager.getObjectsOf(ScalarGlobalParameter.class)) {
            if (p.getPresentationName().equals(name)) {
                hasParam = true;
                break;
            }
        }
        return hasParam;
    }

    private boolean hasStarCADDesignParameterDouble(String name) {
        boolean hasParam = false;
        for (StarCadDocument doc : docManager.getObjects()) {
            StarCadDesignParameterManager man = doc.getStarCadDesignParameters();
            for (StarCadDesignParameterDouble p : man.getObjectsOf(StarCadDesignParameterDouble.class)) {
                if (p.getPresentationName().equals(name)) {
                    hasParam = true;
                    break;
                }
            }
        }
        return hasParam;
    }

    private boolean has3DCADParameter(String name) {
        boolean hasParam = false;
        for (CadModel model : cadManager.getObjectsOf(CadModel.class)) {
            DesignParameterManager man = model.getDesignParameterManager();
            for (DesignParameter p : man.getDesignParameters()) {
                if (p.getPresentationName().equals(name)) {
                    hasParam = true;
                    break;
                }
            }
        }
        return hasParam;
    }

    private ScalarGlobalParameter getScalarGlobalParameter(String name) {
        for (ScalarGlobalParameter p : paramManager.getObjectsOf(ScalarGlobalParameter.class)) {
            if (p.getPresentationName().equals(name)) {
                if (isLiteral(p.getQuantity())) {
                    return p;
                }
            }
        }
        return null;
    }

    private StarCadDesignParameterDouble getStarCadDesignParameterDouble(String name) {
        for (StarCadDocument doc : docManager.getObjects()) {
            StarCadDesignParameterManager man = doc.getStarCadDesignParameters();
            for (StarCadDesignParameterDouble p : man.getObjectsOf(StarCadDesignParameterDouble.class)) {
                if (p.getPresentationName().equals(name)) {
                    if (isLiteral(p.getQuantity())) {
                        return p;
                    }
                }
            }
        }
        return null;
    }

    private DesignParameter get3DCadParameter(String name) {
        for (CadModel model : cadManager.getObjectsOf(CadModel.class)) {
            DesignParameterManager man = model.getDesignParameterManager();
            for (DesignParameter p : man.getDesignParameters()) {
                if (p.getPresentationName().equals(name)) {
                    return p;
                }
            }
        }
        return null;
    }

    private void initialize() {
        _sim = getActiveSimulation();
        paramManager = _sim.get(GlobalParameterManager.class);
        docManager = _sim.get(StarCadDocumentManager.class);
        cadManager = _sim.get(SolidModelManager.class);
        paramVals = new HashMap<>();
    }

    private void print(Exception ex) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        ex.printStackTrace(pw);
        _sim.println(sw.toString());
    }

}
