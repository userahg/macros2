// Simcenter STAR-CCM+ macro: Test.java
// Written by Simcenter STAR-CCM+ 16.03.083
package surfLoch;

import star.common.*;
import star.meshing.*;

public class UpdateInterfaces extends StarMacro {

    @Override
    public void execute() {
        Simulation _sim = getActiveSimulation();

        WeakContactOperation weakContactOperation1 = ((WeakContactOperation) _sim.get(MeshOperationManager.class).getObject("Create Sammich/Ambiant Contact"));
        weakContactOperation1.execute();
        WeakContactOperation weakContactOperation2 = ((WeakContactOperation) _sim.get(MeshOperationManager.class).getObject("Create Sammich/Caisson Contact"));
        weakContactOperation2.execute();

        CompositePart fullModel = (CompositePart) _sim.get(SimulationPartManager.class).getPart("Full Model Assembly_stp");
        CadPart sammich = (CadPart) fullModel.getChildParts().getPart("CFD Air Big Sammich");
        PartSurface upstream = (PartSurface) sammich.getPartSurfaceManager().getPartSurface("02_UPSTREAM");
        PartSurface downstream = (PartSurface) sammich.getPartSurfaceManager().getPartSurface("02_DOWNSTREAM");
        _sim.get(PartContactManager.class).createPeriodic(upstream, downstream);
        BoundaryInterface periodicInterface = (BoundaryInterface) _sim.getInterfaceManager().getInterface("Full Model Assembly_stp.CFD Air Big Sammich/Full Model Assembly_stp.CFD Air Big Sammich");
        PartContact sammichContact = _sim.get(PartContactManager.class).getPartContact(sammich, sammich);
        PeriodicPartSurfaceContact sammichPeriodic = (PeriodicPartSurfaceContact) sammichContact.get(PartSurfaceContactManager.class).getPartSurfaceContact(upstream, downstream);
        periodicInterface.getContacts().addObjects(sammichPeriodic);

        CadPart forwardSide = (CadPart) fullModel.getChildParts().getPart("CFD Air Foward side");
        PartContact forwardPartContact = _sim.get(PartContactManager.class).getPartContact(sammich, forwardSide);
        PartSurface atmosphereSurface = (PartSurface) sammich.getPartSurfaceManager().getPartSurface("02_ATMOSPHERE");
        PartSurface sammichSurface = (PartSurface) forwardSide.getPartSurfaceManager().getPartSurface("02_SAMMICH");
        InPlacePartSurfaceContact forwardSammichPartSurfaceContact = (InPlacePartSurfaceContact) forwardPartContact.get(PartSurfaceContactManager.class).getPartSurfaceContact(atmosphereSurface, sammichSurface);
        BoundaryInterface forwardSammichBoundaryInterface = (BoundaryInterface) _sim.getInterfaceManager().getInterface("Full Model Assembly_stp.CFD Air Big Sammich/Full Model Assembly_stp.CFD Air Foward side");
        forwardSammichBoundaryInterface.getContacts().addObjects(forwardSammichPartSurfaceContact);

        CadPart motorSide = (CadPart) fullModel.getChildParts().getPart("CFD Air Motor Side");
        PartContact motorPartContact = _sim.get(PartContactManager.class).getPartContact(sammich, motorSide);
        PartSurface caissonSurface = (PartSurface) sammich.getPartSurfaceManager().getPartSurface("02_CAISSON");
        PartSurface sammichSurface2 = (PartSurface) motorSide.getPartSurfaceManager().getPartSurface("02_SAMMICH");
        InPlacePartSurfaceContact motorSammichPartSurfaceContact = (InPlacePartSurfaceContact) motorPartContact.get(PartSurfaceContactManager.class).getPartSurfaceContact(caissonSurface, sammichSurface2);
        BoundaryInterface motorSammichBoundaryInterface = (BoundaryInterface) _sim.getInterfaceManager().getInterface("Full Model Assembly_stp.CFD Air Big Sammich/Full Model Assembly_stp.CFD Air Motor Side");
        motorSammichBoundaryInterface.getContacts().addObjects(motorSammichPartSurfaceContact);
    }
}
