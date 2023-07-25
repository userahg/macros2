// Simcenter STAR-CCM+ macro: test.java
// Written by Simcenter STAR-CCM+ 17.04.007
package dieselICE;

import star.combustion.ppdftable.SootEcfmTable;
import star.combustion.ppdftable.TkiTable;
import star.combustion.tablegenerators.EcfmEquilTableCollection;
import star.combustion.tablegenerators.EcfmEquilTableGenerator;
import star.combustion.tablegenerators.EcfmTableGeneratorManager;
import star.combustion.tablegenerators.SootEcfmTableGenerator;
import star.combustion.tablegenerators.TkiTableGenerator;
import star.common.*;
import star.emissions.*;

public class SetTablePaths extends StarMacro {
    
    String tableDir = "/u/cd8unu/projects/ICE/tables/";
    String equiTable = "Diesel_ECFM_EQCLEH_Table_STAR-CCM+.tbl";
    String sootTable = "ECFM_Soot_Library_Diesel_TRF15mech_STARCCM.tbl";
    String noraTable = "NORA_Table_STAR-CCM+.tbl";
    String tkiTable = "Diesel_ECFM_TKI_Table_v2.1_STAR-CCM+.tbl";
    Simulation _sim;

    @Override
    public void execute() {
        _sim = getActiveSimulation();
        PhysicsContinuum physics = (PhysicsContinuum) _sim.getContinuumManager().getContinuum("Default Gases");
        
        EcfmEquilTableGenerator ecfmEquilTableGenerator = (EcfmEquilTableGenerator) physics.get(EcfmTableGeneratorManager.class).getObject("ECFM-CLEH Equilibrium Table Generator");
        EcfmEquilTableCollection ecfmEquilTable = (EcfmEquilTableCollection) ecfmEquilTableGenerator.getEcfmEquilTableCollection();
        ecfmEquilTable.setTablesPathAndName(tableDir + equiTable);
        
        SootEcfmTableGenerator sootEcfmTableGenerator = (SootEcfmTableGenerator) physics.get(EcfmTableGeneratorManager.class).getObject("ECFM Soot Table Generator");
        SootEcfmTable sootEcfmTable = (SootEcfmTable) sootEcfmTableGenerator.getSootEcfmTable();
        sootEcfmTable.setTablePathAndName(tableDir + sootTable);

        NoraModel noraModel = physics.getModelManager().getModel(NoraModel.class);
        NoraTable noraTbl = noraModel.getNoraTable();
        noraTbl.setTablePathAndName(tableDir + noraTable);
        
        TkiTableGenerator tkiTableGenerator = (TkiTableGenerator) physics.get(EcfmTableGeneratorManager.class).getObject("TKI Table Generator");
        TkiTable tkiTbl = (TkiTable) tkiTableGenerator.getTkiTable();
        tkiTbl.setTablePathAndName(tableDir + tkiTable); 
    }
}
