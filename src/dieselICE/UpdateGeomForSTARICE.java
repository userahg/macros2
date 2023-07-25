package dieselICE;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import star.starice.*;
import star.common.*;
import star.base.neo.*;
import star.cadmodeler.*;
import star.meshing.CadPart;

public class UpdateGeomForSTARICE extends StarMacro {

    Simulation _sim;
    String _fileName = File.separator + "TEMP.x_b";
    String _cadFileName = "ICE_Demo_Model.prt";
    String _crReportName = "ICE_Demo_Model.prt\\OUT_CR_NOW";

    @Override
    public void execute() {
        _sim = getActiveSimulation();

        exportCADModel();
        replaceEngineGeom();
        //cleanUp();
    }

    private void exportCADModel() {
        CompositePart assembly = (CompositePart) _sim.get(SimulationPartManager.class).getPart("ICE_Demo_Model");
        SolidModelPart enginePart = (SolidModelPart) assembly.getChildParts().getPart("ENGINE-FLUID");
        SolidModelPart engineFluid1Part = (SolidModelPart) assembly.getChildParts().getPart("engine-fluid");
        SolidModelPart engineFluid2Part = (SolidModelPart) assembly.getChildParts().getPart("engine-fluid 2");
        SolidModelPart engineFluid3Part = (SolidModelPart) assembly.getChildParts().getPart("engine-fluid 3");
        SolidModelPart exhaust1Part = (SolidModelPart) assembly.getChildParts().getPart("EXHAUST-VALVE 1");
        SolidModelPart exhaust2Part = (SolidModelPart) assembly.getChildParts().getPart("EXHAUST-VALVE 2");
        SolidModelPart intake1Part = (SolidModelPart) assembly.getChildParts().getPart("INTAKE-VALVE 1");
        SolidModelPart intake2Part = (SolidModelPart) assembly.getChildParts().getPart("INTAKE-VALVE 2");
        Collection parts = new NeoObjectVector(new Object[] {enginePart, engineFluid1Part, engineFluid2Part, engineFluid3Part, exhaust1Part, exhaust2Part, intake1Part, intake2Part});
                
        CadModel tempCADModel = _sim.get(SolidModelManager.class).createSolidModelForCadParts(parts);
        tempCADModel.resetSystemOptions();

        star.cadmodeler.Body engineCADBody = (star.cadmodeler.Body) tempCADModel.getBody("ENGINE-FLUID");
        star.cadmodeler.Body engineFluid1 = (star.cadmodeler.Body) tempCADModel.getBody("engine-fluid");
        star.cadmodeler.Body engineFluid2 = (star.cadmodeler.Body) tempCADModel.getBody("engine-fluid 2");
        star.cadmodeler.Body engineFluid3 = (star.cadmodeler.Body) tempCADModel.getBody("engine-fluid 3");
        star.cadmodeler.Body exhaust1CADBody = (star.cadmodeler.Body) tempCADModel.getBody("EXHAUST-VALVE 1");
        star.cadmodeler.Body exhaust2CADBody = (star.cadmodeler.Body) tempCADModel.getBody("EXHAUST-VALVE 2");
        star.cadmodeler.Body intake1CADBody = (star.cadmodeler.Body) tempCADModel.getBody("INTAKE-VALVE 1");
        star.cadmodeler.Body intake2CADBody = (star.cadmodeler.Body) tempCADModel.getBody("INTAKE-VALVE 2");
        
        uniteFluidVolumes(tempCADModel, new NeoObjectVector(new Object[] {engineCADBody, engineFluid1, engineFluid2, engineFluid3}));
        
        tempCADModel.exportModel(new NeoObjectVector(new Object[]{engineCADBody, exhaust1CADBody, exhaust2CADBody, intake1CADBody, intake2CADBody}), resolvePath(_sim.getSessionDir() + _fileName), true, false, false, false);
        _sim.get(SolidModelManager.class).endEditCadModel(tempCADModel);
        _sim.get(SolidModelManager.class).removeObjects(tempCADModel);
    }
    
    private void uniteFluidVolumes(CadModel model, Collection<Body> bodies) {
        UniteBodiesFeature unite = model.getFeatureManager().createUniteBodies2();
        unite.setAutoPreview(true);
        model.allowMakingPartDirty(false);
        unite.setAutoPreview(true);
        model.allowMakingPartDirty(false);
        unite.setImprintOption(0);
        Units meters = (Units) _sim.getUnitsManager().getObject("m");
        unite.getTolerance().setValueAndUnits(1.0E-5, meters);
        unite.setUseAutoMatch(true);
        unite.setTransferFaceNames(true);
        unite.setTransferBodyNames(false);
        unite.setBodies(bodies);
        unite.setBodyGroups(new NeoObjectVector(new Object[] {}));
        unite.setCadFilters(new NeoObjectVector(new Object[] {}));
        unite.setIsBodyGroupCreation(false);
        model.getFeatureManager().markDependentNotUptodate(unite);
        model.allowMakingPartDirty(true);
        unite.markFeatureForEdit();
        model.getFeatureManager().execute(unite);        
    }

    private void replaceEngineGeom() {
        _sim.loadStarIce("StarIce");
        StarIceEngine starICEEngineModel = _sim.get(StarIceEngine.class);
        starICEEngineModel.startStarIce();
        starICEEngineModel.replaceEngineGeometry(_sim.getSessionDir() + _fileName, true, true);
        starICEEngineModel.updateStarIce();
        starICEEngineModel.stopStarIce();
    }

    private void cleanUp() {
        try {
            Files.deleteIfExists(Paths.get(_sim.getSessionDir() + _fileName));
        } catch (IOException ex) {
            print(ex);
        }
    }

    private void print(Exception ex) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        ex.printStackTrace(pw);
        _sim.println(sw);
    }
}
