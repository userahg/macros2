
import star.common.Cartesian2DPlot;
import star.common.PlotManager;
import star.mdx.MdxDataSet;
import star.mdx.MdxDesignStudy;
import star.mdx.MdxMacro;
import star.mdx.MdxParetoCycle;
import star.mdx.MdxProject;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author cd8unu
 */
public class SaveParetoPlots extends MdxMacro {

    @Override
    public void execute() {
        
        MdxProject proj = getActiveMdxProject();
        MdxDesignStudy study = proj.getDesignStudyManager().getDesignStudy("Pareto Opt1");
        Cartesian2DPlot plot = (Cartesian2DPlot) proj.get(PlotManager.class).getPlot("Pareto Plot");
        MdxDataSet dataSet = ((MdxDataSet) plot.getDataSetManager().getDataSet("Pareto Opt1"));
        int cycleCnt = study.getDesigns().getParetoCycleManager().getChildrenCount();
        
        proj.println("Cycle count: " + cycleCnt);
        
        for (int i = 1; i <= cycleCnt; i++) {
            MdxParetoCycle cycle = study.getDesigns().getParetoCycleManager().getParetoCycle("Cycle " + i);
            dataSet.getParetoRanks().getCycleGroup().setObjects(cycle);
            study.getDesigns().resetCurrentDesign();
            plot.encode(resolvePath("C:\\Users\\cd8unu\\Desktop\\img\\Cycle-" + String.format("%03d", i) + ".png"), "png", 1600, 910);
        }
                
    }    
}
