/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eclipse;

import java.io.PrintWriter;
import java.io.StringWriter;
import star.common.Simulation;
import star.common.StarMacro;
import star.starcad2.StarCadDesignParameterDouble;
import star.starcad2.StarCadDocument;
import star.starcad2.StarCadDocumentManager;
import star.starcad2.StarCadScalarFunctionProvider;

/**
 *
 * @author cd8unu
 */
public class ChangeDesignAndUpdateCAD extends StarMacro {

    Simulation _sim;
    String _docName = "Nacelle2.CATAnalysis";
    String[] _params = {"95-205511-2001\\Exhaust Angle (+ Exit Outward)", "95-205511-2001\\Inclination Angle (+ Nose Upward)", 
                       "95-205511-2001\\Inlet Diameter", "95-205511-2001\\Inlet Face Angle", "95-205511-2001\\Inlet Length",
                       "95-205511-2001\\Inlet Lip Depth", "95-205511-2001\\Inlet Lip Width", "95-205511-2001\\NCS Location (BL)",
                       "95-205511-2001\\NCS Location (FS)", "95-205511-2001\\NCS Location (WL)", "95-205511-2001\\Toe Angle (+ Nose Outward)"};
    double[] _vals = {5.0, 1.5, 380.853, 0.0, 225.552, 72.7, 28.765, -937.644, 7224.23, 3245.02, 4.0};

    @Override
    public void execute() {
        _sim = getActiveSimulation();
        StarCadDocument starCADDocument = _sim.get(StarCadDocumentManager.class).getDocument(_docName);
        
        try {
            validate();
            for (int i = 0; i < _params.length; i++) {
                changeParam(starCADDocument, _params[i], _vals[i]);
            }
        } catch (Exception ex) {
            print(ex);
        }
        
//        starCADDocument.updateModel();
    }
    
    private void changeParam(StarCadDocument doc, String param, double value) {
        StarCadDesignParameterDouble starCadParam = (StarCadDesignParameterDouble) doc.getStarCadDesignParameters().getParameter(param);
        StarCadScalarFunctionProvider functionProvider = starCadParam.getFunctionProvider();
        functionProvider.getQuantity().setValue(value);
    }
    
    private boolean validate() throws Exception {
        int nParams = _params.length;
        int nVals = _vals.length;
        
        if (nParams != nVals) {
            throw new Exception("Number of parameters and values must be equal. " + nParams + " parameters does not match " + nVals + " values.");
        }
        return true;
    }
    
    private void print(Exception ex) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        ex.printStackTrace(pw);
        _sim.println(sw.toString());        
    }

}
