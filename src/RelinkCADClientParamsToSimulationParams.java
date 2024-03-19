import java.io.PrintWriter;
import java.io.StringWriter;
import star.mdx.MdxDesignStudy;
import star.mdx.MdxMacro;
import star.mdx.MdxProject;
import star.mdx.MdxReferenceScalarParameter;
import star.mdx.MdxReferenceSimulation;
import star.mdx.MdxStudyParameterBase;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author cd8unu
 */
public class RelinkCADClientParamsToSimulationParams extends MdxMacro {

    MdxProject _proj;
    String studySuffix = "_C";
    String cadClientTestString = "CompressorParametric";
    
    @Override
    public void execute() {
        _proj = getActiveMdxProject();
        for (MdxDesignStudy study : _proj.getDesignStudyManager().getDesignStudies()) {
            MdxReferenceSimulation refSim = study.getReferenceSimulationGroup().getObjects().toArray(new MdxReferenceSimulation[1])[0];
            if (study.getPresentationName().endsWith(studySuffix)) {
                for (MdxStudyParameterBase param : study.getStudyParameters().getObjects()) {
                    boolean isCadClient = param.getPresentationName().contains(cadClientTestString);
                    if (isCadClient) {
                        String[] paramNameSplit = param.getPresentationName().split("\\\\");
                        String simParamName = paramNameSplit[paramNameSplit.length - 1];
                        String folder = "Simulation Parameters";
                        _proj.println("CAD Client Parameter Found: Searching for " + simParamName + " Simulation Parameter.");
                        try {
                            MdxReferenceScalarParameter referenceObj = (MdxReferenceScalarParameter) refSim.getReferenceParameterManager().getReferenceParameter(folder, simParamName);
                            study.getStudyParameters().relinkStudyParameter(param, referenceObj);
                        } catch (Exception ex) {
                            print(ex);
                        }
                    }
                }                    
            }
        }        
    }
    
    private void print(Exception ex) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        ex.printStackTrace(pw);
        _proj.println(sw);
    }
    
}
