
import java.util.Iterator;
import star.mdx.MdxAllDesignSet;
import star.mdx.MdxDesign;
import star.mdx.MdxDesignStudy;
import star.mdx.MdxMacro;
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
public class PostProcessDesignSet extends MdxMacro {

    @Override
    public void execute() {
        MdxProject proj = getActiveMdxProject();
        MdxDesignStudy study = proj.getDesignStudyManager().getDesignStudy("Sweep");
        MdxAllDesignSet allDesigns = ((MdxAllDesignSet) study.getDesignSets().getDesignSet("All"));
        Iterator<MdxDesign> iter = allDesigns.getDesigns().iterator();
        
        while (iter.hasNext()) {
            MdxDesign d = iter.next();
            d.getDesignParameters().getDesignParameter("").getQuantity().getRawValue();
            d.getDesignReports().getDesignReport("").getQuantity().getRawValue();
        }
    }
    
}
