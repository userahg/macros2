/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package volvo;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Scanner;
import star.base.neo.NeoObjectVector;
import star.common.PartCurve;
import star.common.PartCurveManager;
import star.common.PartSurface;
import star.common.Simulation;
import star.common.SimulationPartManager;
import star.common.StarMacro;
import star.meshing.CadPart;
import star.meshing.PartRepresentation;
import star.meshing.PartSurfaceMeshWidget;
import star.meshing.RootDescriptionSource;
import star.meshing.SimulationMeshPartDescriptionSourceManager;
import star.meshing.SurfaceMeshWidgetDisplayController;
import star.meshing.SurfaceMeshWidgetRepairController;
import star.meshing.SurfaceMeshWidgetSelectController;
import star.meshing.SurfaceMeshWidgetSelectOptions;
import star.starcad2.StarCadDesignParameter;
import star.starcad2.StarCadDesignParameterDouble;
import star.starcad2.StarCadDocument;
import star.starcad2.StarCadDocumentManager;

/**
 *
 * @author cd8unu
 */
public class DeleteCADEdges extends StarMacro {

    Simulation _sim;
    CadPart _cad;
    PartCurveManager _curveManager;
    List<PartCurve> _toSplit;
    String _fileName = "C:\\Users\\cd8unu\\OneDrive - SPLM\\mdx\\Projects\\Volvo\\CAD\\Working\\All_Export.csv";
    int _designCounter = 0;
    int _designMax = 6;

    @Override
    public void execute() {
        _sim = getActiveSimulation();
        _cad = (CadPart) _sim.get(SimulationPartManager.class).getPart("NX 12 Mirror");
        _curveManager = _cad.getPartCurveManager();
        _toSplit = new ArrayList<>();

//        for (int i = 1; i <= _designMax; i++) {
//            updateCADParameters();
            surfaceRepair();
            
            splitNonContiguous("TEMP", "TEMP", "FF_IN", "FF_OUT");
            split("TEMP", 2.0);
            combine("FF_IN", "TEMP", "TEMP 2", "TEMP 3", "TEMP 5");
            combine("FF_OUT", "TEMP 4", "TEMP 6");
            _cad.deleteMeshPartCurves(Arrays.<PartCurve>asList(_curveManager.getObject("Edges")));
//        }
    }

    private void surfaceRepair() {
        PartRepresentation geomRep = (PartRepresentation) _sim.getRepresentationManager().getObject("Geometry");
        PartSurfaceMeshWidget surfaceRepairWidget = geomRep.startSurfaceMeshWidget();
        RootDescriptionSource rootDescription = _sim.get(SimulationMeshPartDescriptionSourceManager.class).getRootDescriptionSource();
        surfaceRepairWidget.setActiveParts(new NeoObjectVector(new Object[]{_cad}), rootDescription);
        surfaceRepairWidget.startSurfaceRepairControllers();
        SurfaceMeshWidgetDisplayController surfaceMeshDisplayController = surfaceRepairWidget.getControllers().getController(SurfaceMeshWidgetDisplayController.class);
        surfaceMeshDisplayController.showAllFaces();
        SurfaceMeshWidgetSelectController surfaceMeshSelectController = surfaceRepairWidget.getControllers().getController(SurfaceMeshWidgetSelectController.class);
        SurfaceMeshWidgetSelectOptions surfaceMeshSelect = surfaceMeshSelectController.getOptions();
        surfaceMeshSelectController.clearSelectedFaces();
        surfaceMeshSelectController.clearSelectedEdges();
        surfaceMeshSelect.setCanSelectFaces(true);
        surfaceMeshSelect.setCanSelectEdges(false);
        PartSurface underside = (PartSurface) _cad.getPartSurfaceManager().getPartSurface("Underside");
//        PartSurface undersideUpstream = (PartSurface) _cad.getPartSurfaceManager().getPartSurface("Underside Upstream");
        PartSurface cameraLens = (PartSurface) _cad.getPartSurfaceManager().getPartSurface("Camera Lens");
        PartSurface cameraLensRim = (PartSurface) _cad.getPartSurfaceManager().getPartSurface("Camera Lens Rim");
        PartSurface cameraMound = (PartSurface) _cad.getPartSurfaceManager().getPartSurface("Camera Mound");
        PartSurface cameraMoundEdges = (PartSurface) _cad.getPartSurfaceManager().getPartSurface("Camera Mound Edges");
        PartSurface faces = (PartSurface) _cad.getPartSurfaceManager().getPartSurface("Faces");
        surfaceMeshSelectController.selectPartSurfaces(new NeoObjectVector(new Object[]{cameraLens, cameraLensRim, cameraMound, cameraMoundEdges, faces, underside}));

        surfaceMeshDisplayController.hideAllFaces();
        surfaceMeshDisplayController.showSelectedFaces();

        surfaceMeshSelect.setSelectAllEdges(false);
        surfaceMeshSelect.setSelectPerimeterEdges(true);
        surfaceMeshSelect.setSelectFeatureEdges(false);
        surfaceMeshSelect.setSelectFreeEdges(false);
        surfaceMeshSelectController.selectAttachedEdges();
        surfaceMeshSelectController.clearSelectedFaces();

        SurfaceMeshWidgetRepairController surfaceMeshRepairController = surfaceRepairWidget.getControllers().getController(SurfaceMeshWidgetRepairController.class);
        surfaceMeshRepairController.modifyCurveToNewPartCurveInSamePart("TEMP");
        surfaceRepairWidget.stop();

    }

    private void splitDontCombine(String edgeToSplit, double angle, String... dontCombine) {
        List<PartCurve> toSplit = new ArrayList<>();
        Collection<PartCurve> curves;
        toSplit.add(_curveManager.getObject(edgeToSplit));
        _curveManager.splitPartCurvesByAngle(toSplit, angle);

        curves = _curveManager.getPartCurves();
        for (String s : dontCombine) {
            PartCurve pc = _curveManager.getObject(s);
            curves.remove(pc);
        }
        _cad.combinePartCurves(curves);
    }

    private void split(String edgeToSplit, double angle) {
        List<PartCurve> toSplit = new ArrayList<>();
        Collection<PartCurve> curves;
        toSplit.add(_curveManager.getObject(edgeToSplit));
        _curveManager.splitPartCurvesByAngle(toSplit, angle);
    }

    private void splitNonContiguous(String edgeToSplit, String... dontCombine) {
        _cad.getPartCurveManager().splitNonContiguousPartCurves(new NeoObjectVector(new Object[]{_curveManager.getObject(edgeToSplit)}));
        Collection<PartCurve> curves;
        curves = _curveManager.getPartCurves();
        for (String s : dontCombine) {
            PartCurve pc = _curveManager.getObject(s);
            curves.remove(pc);
        }
        _cad.combinePartCurves(curves);
    }

    private void combine(String... toCombine) {
        Collection<PartCurve> curves = new ArrayList<>();
        for (String s : toCombine) {
            PartCurve pc = _curveManager.getObject(s);
            curves.add(pc);
        }
        _cad.combinePartCurves(curves);
    }

    private void updateCADParameters() {

        try {
            String[] header = new String[1];
            File f = new File(_fileName);
            Scanner sc = new Scanner(f);
            while (sc.hasNextLine() && _designCounter < _designMax) {
                String line = sc.nextLine();
                if (line.startsWith("Design")) {
                    header = line.split(",");
                    continue;
                }
                for (int i = 1; i < header.length; i++) {
                    StarCadDesignParameterDouble param = getDesignParameter(header[i].replace("\"", "").trim());
                    param.getFunctionProvider().getQuantity().setValue(Double.parseDouble(line.split(",")[i]));
                }
                _designCounter++;
            }

            _sim.get(StarCadDocumentManager.class).getDocument("NX 12 Mirror.prt").updateModel();

        } catch (FileNotFoundException fnfe) {
            _sim.println("Unable to find file " + _fileName + ".");
            print(fnfe);
        } catch (NumberFormatException nfe) {
            _sim.println("Error parsing double from file " + _fileName + ".");
            print(nfe);
        } catch (NullPointerException npe) {
            print(npe);
        }

    }

    private StarCadDesignParameterDouble getDesignParameter(String name) throws NullPointerException {
        StarCadDocument doc = _sim.get(StarCadDocumentManager.class).getDocument("NX 12 Mirror.prt");
        StarCadDesignParameter param = doc.getStarCadDesignParameters().getParameter(name);
        if (param == null) {
            throw new NullPointerException();
        }
        return (StarCadDesignParameterDouble) param;
    }

    private void print(Exception ex) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        ex.printStackTrace(pw);
        _sim.println(sw.toString());
    }
}
