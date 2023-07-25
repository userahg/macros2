// STAR-CCM+ macro: MoveCircularPatternedHoles.java
// Written by STAR-CCM+ 13.02.011

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Vector;
import star.common.*;
import star.base.neo.*;
import star.cadmodeler.*;
import star.meshing.MeshOperationManager;
import star.meshing.SubtractPartsOperation;

public class MoveCircularPatternedHoles extends StarMacro {

    Simulation _sim;
    CadModel _cath;
    Units _units;
    CanonicalReferenceCoordinateSystem _canRefCoordSys;
    LabCoordinateSystem _labCoordSys;
    ScalarGlobalParameter _pNRadArtHoles;
    ScalarGlobalParameter _pNRowsArtHoles;
    ScalarGlobalParameter _qArtHoleSpiralDistInc;
    ScalarGlobalParameter _qArtHoleDistRemainder;
    ScalarGlobalParameter _pNRadVeinHoles;
    ScalarGlobalParameter _pNRowsVeinHoles;
    ScalarGlobalParameter _qVeinHoleSpiralDistInc;
    ScalarGlobalParameter _qVeinHoleDistRemainder;

    int _nRadArtHoles;
    int _nRowsArtHoles;
    double _artHoleSpiralDistInc;
    double _artHoleDistRemainder;

    int _nRadVeinHoles;
    int _nRowsVeinHoles;
    double _veinHoleSpiralDistInc;
    double _veinHoleDistRemainder;

    boolean debug = true;

    @Override
    public void execute() {

        try {
            init();
            _cath.update();
            for (int i = 2; i <= _nRadArtHoles; i++) {
                moveBody("Arterial Hole Circular Pattern", i, (i - 1) * _artHoleSpiralDistInc);
            }
            for (int i = 2; i <= _nRadVeinHoles; i++) {
                moveBody("Veinous Hole Circular Pattern", i, (i - 1) * _veinHoleSpiralDistInc);
            }
            linearPattern("Arterial Hole Circular Pattern", _nRowsArtHoles, getLinearPatternInc(_nRowsArtHoles, _artHoleDistRemainder));
            linearPattern("Veinous Hole Circular Pattern", _nRowsVeinHoles, getLinearPatternInc(_nRowsVeinHoles, _veinHoleDistRemainder));
            SubtractPartsOperation subtract = ((SubtractPartsOperation) _sim.get(MeshOperationManager.class).getObject("Create Holes"));
            subtract.execute();
        } catch (Exception ex) {
            print(ex);
        }
    }

    private void moveBody(String featureName, int index, double inc) {
        MoveBodyFeature moveBody = _cath.getFeatureManager().createMoveBodyFeature();
        moveBody.setDirectionOption(0);
        moveBody.setCoordinateSystemSourceOption(1);
        moveBody.setReferenceCoordinateSystem(_canRefCoordSys);
        moveBody.setImportedCoordinateSystem(_labCoordSys);
        CadModelCoordinate coord = moveBody.getTranslationVector();
        coord.setCoordinateSystem(_labCoordSys);
        coord.setCoordinate(_units, _units, _units, new DoubleVector(new double[]{-inc, 0.0, 0.0}));
        moveBody.setCopyOption(0);
        BodyCircularPatternMerge holeCircPatt = ((BodyCircularPatternMerge) _cath.getFeature(featureName));
        star.cadmodeler.Body holeBody = ((star.cadmodeler.Body) holeCircPatt.getBodyByIndex(0, index));
        moveBody.setBodies(new NeoObjectVector(new Object[]{holeBody}));
        moveBody.markFeatureForEdit();
        _cath.getFeatureManager().execute(moveBody);
    }

    private void linearPattern(String featureName, int nInst, double dist) {
        BodyLinearPatternMerge linearPattern = _cath.getFeatureManager().createBodyLinearPatternMerge();
        linearPattern.setBodies(new NeoObjectVector(getCircularPatternBodies(featureName)));
        linearPattern.setDirectionOption1(0);
        linearPattern.setDirectionOption2(0);
        linearPattern.setCoordinateSystemSourceOption1(1);
        linearPattern.setCoordinateSystemSourceOption2(1);
        linearPattern.setReferenceCoordinateSystem1(_canRefCoordSys);
        linearPattern.setCoordinateSystem1(_labCoordSys);

        CadModelCoordinate coordDir1 = linearPattern.getDirection1();
        coordDir1.setCoordinateSystem(_labCoordSys);
        coordDir1.setCoordinate(_units, _units, _units, new DoubleVector(new double[]{-1.0, 0.0, 0.0}));
        linearPattern.setReferenceCoordinateSystem2(_canRefCoordSys);
        linearPattern.setCoordinateSystem2(_labCoordSys);
        CadModelCoordinate coordDir2 = linearPattern.getDirection2();
        coordDir2.setCoordinateSystem(_labCoordSys);
        coordDir2.setCoordinate(_units, _units, _units, new DoubleVector(new double[]{0.0, 1.0, 0.0}));

        linearPattern.getDistance1().setValue(dist);
        linearPattern.getNumberCopy1().setValue(nInst);
        linearPattern.setSkipPositions(new IntVector(new int[]{}));
        linearPattern.setPostOption(0);

        BodyNameRefManager interactionManager = linearPattern.getInteractionBodies();
        interactionManager.setBodies(new NeoObjectVector(new Object[]{}));

        linearPattern.setInteractingSelectedBodies(false);
        linearPattern.markFeatureForEdit();
        _cath.getFeatureManager().execute(linearPattern);
    }

    private double getLinearPatternInc(int nInst, double dist) {
        if (nInst > 1) {
            return dist / (nInst - 1);
        } else {
            return dist;
        }
    }

    public Object[] getCircularPatternBodies(String featureName) {
        BodyCircularPatternMerge artHoleCircPatt = ((BodyCircularPatternMerge) _cath.getFeature(featureName));
        Vector<star.cadmodeler.Body> body = artHoleCircPatt.getBodies();
        Vector<star.cadmodeler.Body> bodies = artHoleCircPatt.getFeatureBodies();
        body.addAll(bodies);
        return body.toArray();
    }

    private void init() {
        _sim = getActiveSimulation();
        _cath = ((CadModel) _sim.get(SolidModelManager.class).getObject("Catheter"));
        _units = _sim.getUnitsManager().getPreferredUnits(new IntVector(new int[]{0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}));
        _canRefCoordSys = ((CanonicalReferenceCoordinateSystem) _cath.getFeature("Lab Coordinate System"));
        _labCoordSys = _sim.getCoordinateSystemManager().getLabCoordinateSystem();
        _pNRadArtHoles = ((ScalarGlobalParameter) _sim.get(GlobalParameterManager.class).getObject("P_NRadArtHoles"));
        _pNRowsArtHoles = ((ScalarGlobalParameter) _sim.get(GlobalParameterManager.class).getObject("P_NRowsArtHoles"));
        _qArtHoleSpiralDistInc = ((ScalarGlobalParameter) _sim.get(GlobalParameterManager.class).getObject("Q_ArtHoleSpiralDistInc"));
        _qArtHoleDistRemainder = ((ScalarGlobalParameter) _sim.get(GlobalParameterManager.class).getObject("Q_ArtHoleDistRemainder"));
        _pNRadVeinHoles = ((ScalarGlobalParameter) _sim.get(GlobalParameterManager.class).getObject("P_NRadVeinHoles"));
        _pNRowsVeinHoles = ((ScalarGlobalParameter) _sim.get(GlobalParameterManager.class).getObject("P_NRowsVeinHoles"));
        _qVeinHoleSpiralDistInc = ((ScalarGlobalParameter) _sim.get(GlobalParameterManager.class).getObject("Q_VeinHoleSpiralDistInc"));
        _qVeinHoleDistRemainder = ((ScalarGlobalParameter) _sim.get(GlobalParameterManager.class).getObject("Q_VeinHoleDistRemainder"));

        _artHoleSpiralDistInc = _qArtHoleSpiralDistInc.getQuantity().getRawValue();
        _nRadArtHoles = (int) _pNRadArtHoles.getQuantity().getRawValue();
        _nRowsArtHoles = (int) _pNRowsArtHoles.getQuantity().getRawValue();
        _artHoleDistRemainder = _qArtHoleDistRemainder.getQuantity().getRawValue();
        _veinHoleSpiralDistInc = _qVeinHoleSpiralDistInc.getQuantity().getRawValue();
        _nRadVeinHoles = (int) _pNRadVeinHoles.getQuantity().getRawValue();
        _nRowsVeinHoles = (int) _pNRowsVeinHoles.getQuantity().getRawValue();
        _veinHoleDistRemainder = _qVeinHoleDistRemainder.getQuantity().getRawValue();
    }

    private void print(Exception ex) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        ex.printStackTrace(pw);
        _sim.println(sw.toString());
    }
}
