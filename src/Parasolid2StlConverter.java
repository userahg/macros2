
import java.io.File;
import java.io.FileFilter;
import star.base.neo.NeoObjectVector;
import star.common.GeometryPart;
import star.common.Simulation;
import star.common.SimulationPartManager;
import star.common.StarMacro;
import star.meshing.PartImportManager;
import star.meshing.RootDescriptionSource;
import star.meshing.SimulationMeshPartDescriptionSourceManager;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author cd8unu
 */
public class Parasolid2StlConverter extends StarMacro {

    @Override
    public void execute() {
        Simulation _sim = getActiveSimulation();
        PartImportManager pim = _sim.get(PartImportManager.class);
        SimulationPartManager spm = _sim.get(SimulationPartManager.class);
        RootDescriptionSource rds = _sim.get(SimulationMeshPartDescriptionSourceManager.class).getRootDescriptionSource();

        for (File fi : _sim.getSessionDirFile().listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.toString().endsWith(".x_b");
            }
        })) {
            pim.importCadPart(fi.getAbsolutePath(), "SharpEdges", 30.0, 2);
        }

        for (GeometryPart parti : spm.getPartManager().getParts()) {
            int i = 0;
            rds.exportStlPartDescriptions(new NeoObjectVector(new Object[]{parti}), resolvePath(_sim.getSessionDir() + File.separator + "export" + i + ".stl"), true);
        }
    }

}
