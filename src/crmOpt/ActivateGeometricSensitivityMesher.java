/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package crmOpt;

import star.common.PartSurface;
import star.common.Simulation;
import star.common.SimulationPartManager;
import star.common.StarMacro;
import star.meshing.AutoMeshOperation;
import star.meshing.MeshOperationManager;
import star.meshing.MeshOperationPart;
import star.meshing.SurfaceCustomMeshControl;

/**
 *
 * @author cd8unu
 */
public class ActivateGeometricSensitivityMesher extends StarMacro {

    @Override
    public void execute() {
        Simulation sim = getActiveSimulation();
        AutoMeshOperation geomSensMeshOp = (AutoMeshOperation) sim.get(MeshOperationManager.class).getObject("Geometric Sensitivity");
        MeshOperationPart subtractPart = (MeshOperationPart) sim.get(SimulationPartManager.class).getPart("Subtract");
        geomSensMeshOp.getInputGeometryObjects().addPart(subtractPart);
        PartSurface freestreamSurf = (PartSurface) subtractPart.getPartSurfaceManager().getPartSurface("Domain.00_FREESTREAM");
        PartSurface rootSurf = (PartSurface) subtractPart.getPartSurfaceManager().getPartSurface("Domain.01_ROOT");
        PartSurface tipSurf = (PartSurface) subtractPart.getPartSurfaceManager().getPartSurface("Domain.01_TIP");
        PartSurface teSurf = (PartSurface) subtractPart.getPartSurfaceManager().getPartSurface("CSTAirfoil.01_TRAILING_EDGE");
        
        SurfaceCustomMeshControl freestreamControl = (SurfaceCustomMeshControl) geomSensMeshOp.getCustomMeshControls().getObject("FREESTREAM");
        freestreamControl.getGeometryObjects().setObjects(freestreamSurf, rootSurf, tipSurf);
        SurfaceCustomMeshControl trailingEdgeControl = (SurfaceCustomMeshControl) geomSensMeshOp.getCustomMeshControls().getObject("TRAILING_EDGE");
        trailingEdgeControl.getGeometryObjects().setObjects(teSurf);
    }    
}
