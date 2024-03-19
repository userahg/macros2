// Simcenter STAR-CCM+ macro: UpdatePatchMeshSpacing.java
// Written by Simcenter STAR-CCM+ 18.06.006

import star.common.*;
import star.base.neo.*;
import star.meshing.*;
import star.sweptmesher.*;

public class UpdatePatchMeshSpacing extends StarMacro {

    Simulation _sim;
    final String paramName = "n_cells";

    @Override
    public void execute() {
        _sim = getActiveSimulation();
        ScalarGlobalParameter param = (ScalarGlobalParameter) _sim.get(GlobalParameterManager.class).getObject(paramName);
        Double doubleCells = param.getQuantity().getRawValue();
        int nCells = doubleCells.intValue();
        
        DirectedMeshOperation dm_meshOp = (DirectedMeshOperation) _sim.get(MeshOperationManager.class).getObject("Directed Mesh");
        dm_meshOp.editDirectedMeshOperation();
        DirectedPatchSourceMesh patchSourceMesh = (DirectedPatchSourceMesh) dm_meshOp.getGuidedSurfaceMeshBaseManager().getObject("Patch Mesh");
        patchSourceMesh.editDirectedPatchSourceMesh();
        patchSourceMesh.enablePatchMeshMode();
        PatchCurve patchCurve_0 = (PatchCurve) patchSourceMesh.getPatchCurveManager().getObject("PatchCurve 2");
        patchSourceMesh.defineMeshMultiplePatchCurves(new NeoObjectVector(new Object[]{patchCurve_0}), nCells, false);
        PatchCurve patchCurve_1 = (PatchCurve) patchSourceMesh.getPatchCurveManager().getObject("PatchCurve 3");
        patchSourceMesh.defineMeshMultiplePatchCurves(new NeoObjectVector(new Object[]{patchCurve_1}), nCells, false);
        patchSourceMesh.stopEditPatchOperation();
        dm_meshOp.stopEditingDirectedMeshOperation();
    }
}
