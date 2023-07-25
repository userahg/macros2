
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
public class RunStudiesByRegularExpression extends MdxMacro {
    
    String regularExpression = "^SmartSweep.*_Latest$";

    @Override
    public void execute() {
        MdxProject proj = getActiveMdxProject();
        
        for (MdxDesignStudy mdsi : proj.getDesignStudyManager().getDesignStudies()) {
            if (runStudy(mdsi)) {
                proj.println(mdsi.getPresentationName() + " does match.");
//                mdsi.runDesignStudy();
            } else {
                proj.println(mdsi.getPresentationName() + " doesn't match.");
            }
        }
    }
    
    private boolean runStudy(MdxDesignStudy study) {
        Pattern p = Pattern.compile(regularExpression);
        return p.matcher(study.getPresentationName()).find();
    }    
}
