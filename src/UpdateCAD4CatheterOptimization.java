
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import star.base.neo.NeoObjectVector;
import star.cadmodeler.Body;
import star.cadmodeler.BodyCircularPatternMerge;
import star.cadmodeler.BodyLinearPatternMerge;
import star.cadmodeler.BodyManager;
import star.cadmodeler.CadModel;
import star.cadmodeler.Feature;
import star.cadmodeler.FeatureManager;
import star.cadmodeler.ReferenceAxisByTwoPoints;
import star.cadmodeler.RotateBodyFeature;
import star.cadmodeler.SolidModelManager;
import star.cadmodeler.SolidModelPart;
import star.cadmodeler.UniteBodiesFeature;
import star.common.GlobalParameterManager;
import star.common.ScalarGlobalParameter;
import star.common.Simulation;
import star.common.SimulationPartManager;
import star.common.StarMacro;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author cd8unu
 */
public class UpdateCAD4CatheterOptimization extends StarMacro {

    private CadModel _cad;
    private FeatureManager _features;
    private Collection<ScalarGlobalParameter> _parameters;
    private Simulation _sim;
    private final String _host = "localhost";
    private final int _port = 0;

    @Override
    public void execute() {
        init();

        try {
            modifyCAD();
            reset();
            patternFins();
            rotateFins();
            unite();
            updateParts();
            printFeatures();
        } catch (Exception ex) {
            _sim.println(print(ex));
        }
    }

    private void init() {
        _sim = getActiveSimulation();
        _parameters = getParameters();
        _cad = (CadModel) _sim.get(SolidModelManager.class).getObject("3D-CAD Model 1");
        _features = _cad.getFeatureManager();
    }

    private Collection<ScalarGlobalParameter> getParameters() {
        GlobalParameterManager man = _sim.get(GlobalParameterManager.class);
        return man.getObjectsOf(ScalarGlobalParameter.class);
    }

    private ScalarGlobalParameter getParameter(String name) {
        Iterator<ScalarGlobalParameter> iter = _parameters.iterator();
        while (iter.hasNext()) {
            ScalarGlobalParameter sgp = iter.next();
            if (sgp.getPresentationName().equals(name)) {
                return sgp;
            }
        }
        return null;
    }

    private void reset() {
        ArrayList<Feature> toDelete = new ArrayList<>();
        Iterator<Feature> iter = _features.getFeatures().iterator();

        while (iter.hasNext()) {
            Feature f = iter.next();
            if (f.isVisibleOnTree() &&f.getIndex() > 62) { 
                toDelete.add(f);
            }
        }

        _features.delete(toDelete);
    }

    private void patternFins() {
        int nRadial = (int) getParameter("P_NRadial").getQuantity().getSIValue();
        BodyCircularPatternMerge radialPattern = (BodyCircularPatternMerge) _cad.getFeature("Circular Pattern 1");
        BodyLinearPatternMerge linearPattern = _cad.getFeatureManager().createBodyLinearPatternMerge();
        ReferenceAxisByTwoPoints x = (ReferenceAxisByTwoPoints) _cad.getFeature("X");

        Collection<Body> bodies = new ArrayList<>();

        bodies.add(_cad.getBody("Fin"));

        for (int i = 2; i <= nRadial; i++) {
            bodies.add(radialPattern.getBodyByIndex(0, i));
        }

        linearPattern.setBodies(bodies);
        linearPattern.setDirectionOption1(1);
        linearPattern.setDirectionOption2(0);
        linearPattern.setCoordinateSystemSourceOption1(1);
        linearPattern.setCoordinateSystemSourceOption2(1);
        linearPattern.setReferenceAxis1(x);
        linearPattern.getDistance1().setDefinition("${Q_FinDelta}");
        linearPattern.getNumberCopy1().setDefinition("${P_NRows}");
        linearPattern.markFeatureForEdit();
        linearPattern.setInteractingSelectedBodies(false);
        linearPattern.setPresentationName("Make Rows");
        linearPattern.setPostOption(0);
        _cad.getFeatureManager().execute(linearPattern);
    }

    private void rotateFins() {
        int nRows = (int) getParameter("P_NRows").getQuantity().getSIValue();
        ReferenceAxisByTwoPoints axis = (ReferenceAxisByTwoPoints) _features.getObject("X");

        for (int i = 2; i <= nRows; i++) {
            String coeff = Integer.toString(i - 1) + "*";
            RotateBodyFeature rotate = _features.createRotateBodyFeature();
            rotate.setRotationAxisOption(1);
            rotate.setCoordinateSystemSourceOption(1);
            rotate.setReferenceAxis(axis);
            rotate.getAngle().setDefinition(coeff + "${Q_FinRowRotateInc}");
            rotate.setCopyOption(0);
            rotate.setBodies(getBodiesToRotate(i));
            rotate.markFeatureForEdit();
            _features.execute(rotate);
        }
    }

    private NeoObjectVector getBodiesToRotate(int row) {
        int nFinsPerRow = (int) getParameter("P_NRadial").getQuantity().getSIValue();
        BodyLinearPatternMerge makeRows = ((BodyLinearPatternMerge) _features.getObject("Make Rows"));
        ArrayList<Body> finBodies = new ArrayList<>();

        for (int i = 0; i < nFinsPerRow; i++) {
            Body b = makeRows.getBodyByIndex(i, row, 1);
            finBodies.add(b);
        }

        NeoObjectVector objVec = new NeoObjectVector(finBodies.toArray());
        return objVec;
    }

    private void unite() {
        UniteBodiesFeature unite = _features.createUniteBodies();
        unite.setBodies(_cad.getBodyManager().getBodies());
        unite.setImprintOption(0);
        unite.setTransferFaceNames(true);
        unite.setTransferBodyNames(false);
        unite.markFeatureForEdit();
        _cad.getFeatureManager().execute(unite);
    }

    private void updateParts() {
        SimulationPartManager spm = _sim.get(SimulationPartManager.class);
        SolidModelPart part = (SolidModelPart) spm.getPart("Body");
        spm.updateParts(new NeoObjectVector(new Object[]{part}));
    }

    private void printFeatures() {
        HashMap<Integer, Feature> orderedFeatures = new HashMap<>();
        Iterator<Feature> iter2 = _features.getFeatures().iterator();

        while (iter2.hasNext()) {
            Feature f = iter2.next();
            orderedFeatures.put(f.getIndex(), f);
        }

        for (Map.Entry<Integer, Feature> entry : orderedFeatures.entrySet()) {
            int featureNamespace = 35;
            String featureName = entry.getValue().getPresentationName();
            int extraSpaces = featureNamespace - entry.getValue().getPresentationName().length();
            for (int i = 1; i <= extraSpaces; i++) {
                featureName += " ";
            }
            _sim.println("Feature: " + featureName + "    Index: " + entry.getKey());
        }
    }

    private void modifyCAD() {
        if (_port != 0) {
            modifyCAD(_host, _port);
        }

    }
    
    private void modifyCAD(String host, int port) {
        Simulation source = new Simulation(host, port);
        
        Collection<ScalarGlobalParameter> params = source.get(GlobalParameterManager.class).getObjectsOf(ScalarGlobalParameter.class);
        Iterator<ScalarGlobalParameter> iter = params.iterator();
        
        while (iter.hasNext()) {
            ScalarGlobalParameter sourcep = iter.next();
            if (sourcep.getPresentationName().startsWith("P_")) {
                ScalarGlobalParameter destp = (ScalarGlobalParameter) _sim.get(GlobalParameterManager.class).getObject(sourcep.getPresentationName());
                destp.getQuantity().setValue(sourcep.getQuantity().getRawValue());
            }
        }
        source.disconnect();
        updateParts();        
    }

    private String print(Exception ex) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        ex.printStackTrace(pw);
        return sw.toString();
    }

}
