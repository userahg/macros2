// Simcenter STAR-CCM+ macro: Test.java
// Written by Simcenter STAR-CCM+ 17.04.007
package eCoolFESS;

import java.util.ArrayList;
import java.util.Collection;
import star.common.*;
import star.base.neo.*;
import star.cadmodeler.*;
import star.meshing.InPlacePartSurfaceContact;

public class UpdateBarInterfaces extends StarMacro {

    Simulation _sim;

    @Override
    public void execute() {
        _sim = getActiveSimulation();

        int nRegions = (int) ((ScalarGlobalParameter) _sim.get(GlobalParameterManager.class).getObject("P_N_Regions")).getQuantity().getRawValue();
        CadModel cad = (CadModel) _sim.get(SolidModelManager.class).getObject("3D-CAD");

        updateCAD(nRegions, cad);
        
        updateInterfaces();
    }
    
    private void updateCAD(int nRegions, CadModel cad) {
        SimulationPartManager partManager = _sim.get(SimulationPartManager.class);
        SolidModelCompositePart barPart = (SolidModelCompositePart) partManager.getPart("BAR");
        partManager.removeParts(new NeoObjectVector(new Object[] {barPart}));
        
        cad.allowMakingPartDirty(false);
        SliceBodiesFeature slice = (SliceBodiesFeature) cad.getFeature("Slice Bar");
        cad.getFeatureManager().updateModelForEditingFeature(slice);
        Body barBody = (star.cadmodeler.Body) cad.getBody("BAR");
        NeoObjectVector empty = new NeoObjectVector(new Object[]{});
        NeoObjectVector bar = new NeoObjectVector(new Object[]{barBody});

        slice.setBodies(bar);
        slice.setBodyGroups(empty);
        slice.setCadFilters(empty);
        slice.setSliceOption(2);
        slice.setSliceBodies(populateSheetsForSlice(nRegions, cad));
        slice.setSliceBodyGroups(empty);
        slice.setSliceCadFilters(empty);
        slice.setExtendFace(false);
        slice.setSideOption(0);
        slice.setIsBodyGroupCreation(true);
        slice.markFeatureForEdit();
        cad.allowMakingPartDirty(true);
        cad.getFeatureManager().markDependentNotUptodate(slice);
        cad.getFeatureManager().rollForwardToEnd();
        cad.update();
        nameFaces(nRegions, cad);
        BodyGroup barGroup = (BodyGroup) cad.getBodyGroupManager().getBodyGroup("BAR");
        cad.createParts(new NeoObjectVector(new Object[] {}), new NeoObjectVector(new Object[] {barGroup}), true, false, 1, false, false, 3, "SharpEdges", 30.0, 2, true, 1.0E-5, false);
        partManager.updateParts(partManager.getParts());
    }

    private NeoObjectVector populateSheetsForSlice(int nRegions, CadModel cad) {

        ArrayList<Object> sheets = new ArrayList<>();

        for (int i = 1; i < nRegions; i++) {
            String index = Integer.toString(i);
            ConvertSketchToSheet convertSketchToSheet = (ConvertSketchToSheet) cad.getFeature("Sheet " + index);
            Sketch sketch = (Sketch) cad.getFeature("Sketch " + index);
            LineSketchPrimitive lineSketchPrimitive = (LineSketchPrimitive) sketch.getSketchPrimitive("Line 1");
            star.cadmodeler.Body body = (star.cadmodeler.Body) convertSketchToSheet.getBody(lineSketchPrimitive);
            sheets.add(body);
        }

        return new NeoObjectVector(sheets.toArray());
    }

    private void nameFaces(int nRegions, CadModel cad) {
        SliceBodiesFeature slice = ((SliceBodiesFeature) cad.getFeature("Slice Bar"));
        double current_z = 0.02;
        Face tipSide;
        Face wallSide;

        switch (nRegions) {
            case 2:
                star.cadmodeler.Body barBody = ((star.cadmodeler.Body) cad.getBody("BAR"));
                tipSide = (Face) slice.getFaceByLocation(barBody, new DoubleVector(new double[]{0, 0, 0.02}));
                cad.setFaceNameAttributes(new NeoObjectVector(new Object[]{tipSide}), "02_TIPSIDE", false);
                barBody = ((star.cadmodeler.Body) cad.getBody("BAR 2"));
                wallSide = (Face) slice.getFaceByLocation(barBody, new DoubleVector(new double[]{0, 0, 0.02}));
                cad.setFaceNameAttributes(new NeoObjectVector(new Object[]{wallSide}), "02_WALLSIDE", false);
                break;
                
            default:
                String wallIndex = Integer.toString(nRegions - 1);
                String tipIndex = Integer.toString(nRegions);
                barBody = ((star.cadmodeler.Body) cad.getBody("BAR " + wallIndex));
                tipSide = (Face) slice.getFaceByLocation(barBody, new DoubleVector(new double[]{0, 0, 0.02}));
                cad.setFaceNameAttributes(new NeoObjectVector(new Object[]{tipSide}), "02_TIPSIDE", false);
               
                if (nRegions >= 3) {
                    for (int i = nRegions - 2; i >= 1; i--) {
                        String index = i == 1 ? "" : " " + Integer.toString(i);
                        star.cadmodeler.Body bar_i = (star.cadmodeler.Body) cad.getBody("BAR" + index);
                        wallSide = (Face) slice.getFaceByLocation(bar_i, new DoubleVector(new double[]{0, 0, current_z}));
                        cad.setFaceNameAttributes(new NeoObjectVector(new Object[]{wallSide}), "02_WALLSIDE", false);
                        current_z += 0.02;
                        tipSide = (Face) slice.getFaceByLocation(bar_i, new DoubleVector(new double[]{0, 0, current_z}));
                        cad.setFaceNameAttributes(new NeoObjectVector(new Object[]{tipSide}), "02_TIPSIDE", false);
                    }
                }
                
                barBody = ((star.cadmodeler.Body) cad.getBody("BAR " + tipIndex));
                wallSide = (Face) slice.getFaceByLocation(barBody, new DoubleVector(new double[]{0, 0, current_z}));
                cad.setFaceNameAttributes(new NeoObjectVector(new Object[]{wallSide}), "02_WALLSIDE", false);
        }
    }
    
    private void updateInterfaces() {
        InterfaceManager interfaceManager = _sim.getInterfaceManager();
        interfaceManager.deleteChildren(interfaceManager.getObjects());
        MappedInterface mappedInterface = (MappedInterface) _sim.get(ConditionTypeManager.class).get(MappedInterface.class);
        
        for (PartContact contact : _sim.get(PartContactManager.class).getObjects()) {
            Collection<Object> objs = contact.getChildren();
            InPlacePartSurfaceContact[] partSurfaceContacts = objs.toArray(new InPlacePartSurfaceContact[objs.size()]);
            BoundaryInterface bndInterface = _sim.getInterfaceManager().createBoundaryInterface(new NeoObjectVector(partSurfaceContacts), contact.getPresentationName());
            bndInterface.setInterfaceType(mappedInterface);
        }
    }
}
