// Simcenter STAR-CCM+ macro: UpdatePatchMesh.java
// Written by Simcenter STAR-CCM+ 17.04.007
package eCoolFESS;

import java.util.ArrayList;
import java.util.Collection;
import star.common.*;
import star.base.neo.*;
import star.cadmodeler.SolidModelCompositePart;
import star.cadmodeler.SolidModelPart;
import star.meshing.*;
import star.sweptmesher.*;

public class UpdatePatchMesh extends StarMacro {

    Simulation _sim;

    @Override
    public void execute() {
        _sim = getActiveSimulation();

        ScalarGlobalParameter nCellsParam = (ScalarGlobalParameter) _sim.get(GlobalParameterManager.class).getObject("P_N_Cells");
        int nCells = (int) nCellsParam.getQuantity().getRawValue();
        int nLayers = 5 * nCells;

        DirectedMeshOperation directedMesh = (DirectedMeshOperation) _sim.get(MeshOperationManager.class).getObject("Directed Mesh");
        directedMesh.getInputGeometryObjects().setObjects(((SolidModelCompositePart) _sim.get(SimulationPartManager.class).getPart("BAR")).getChildParts().getParts());
        DirectedMeshPartCollection connectedPart = (DirectedMeshPartCollection) directedMesh.getGuidedMeshPartCollectionManager().getObject("Connected Part");
        DirectedPatchSourceMesh patchMeshSource = (DirectedPatchSourceMesh) directedMesh.getGuidedSurfaceMeshBaseManager().getObject("Patch Mesh");
        patchMeshSource.getPartCollections().setObjects(connectedPart);
        patchMeshSource.editDirectedPatchSourceMesh();
        patchMeshSource.autopopulateFeatureEdges();
        patchMeshSource.enablePatchMeshMode();

        PatchCurve topBottomCurve = getHorizontalPatchCurve(patchMeshSource);
        patchMeshSource.defineMeshMultiplePatchCurves(new NeoObjectVector(new Object[]{topBottomCurve}), nCells, false);

        PatchCurve leftRightCurve = getVerticalPatchCurve(patchMeshSource);
        patchMeshSource.defineMeshMultiplePatchCurves(new NeoObjectVector(new Object[]{leftRightCurve}), nCells, false);
        
        patchMeshSource.stopEditPatchOperation();

        DirectedMeshDistribution volumeDist = (DirectedMeshDistribution) directedMesh.getDirectedMeshDistributionManager().getObject("Volume Distribution");
        DirectedMeshNumLayers numLayers = volumeDist.getDefaultValues().get(DirectedMeshNumLayers.class);
        numLayers.setNumLayers(nLayers);
    }

    private PatchCurve getHorizontalPatchCurve(DirectedPatchSourceMesh patchMeshSource) {

        double tol = 0.000001;

        for (PatchCurve pc : patchMeshSource.getPatchCurveManager().getObjects()) {
            double y1 = pc.startVertex().getCoordinate().getComponent(1);
            double y2 = pc.endVertex().getCoordinate().getComponent(1);
            double diff = Math.abs(y1 - y2);
            if (diff < tol) {
                return pc;
            }
        }
        
        return null;
    }
    
    private PatchCurve getVerticalPatchCurve(DirectedPatchSourceMesh patchMeshSource) {

        double tol = 0.000001;

        for (PatchCurve pc : patchMeshSource.getPatchCurveManager().getObjects()) {
            double x1 = pc.startVertex().getCoordinate().getComponent(0);
            double x2 = pc.endVertex().getCoordinate().getComponent(0);
            double diff = Math.abs(x1 - x2);
            if (diff < tol) {
                return pc;
            }
        }
        
        return null;
    }
}
