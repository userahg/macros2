
import java.util.ArrayList;
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
    boolean test_only = false;

    @Override
    public void execute() {
        MdxProject proj = getActiveMdxProject();
        ArrayList<MdxDesignStudy> studies = new ArrayList<>();
        
        for (MdxDesignStudy mdsi : proj.getDesignStudyManager().getDesignStudies()) {
            if (runStudy(mdsi)) {
                proj.println(mdsi.getPresentationName() + " does match.");
                studies.add(mdsi);
            } else {
                proj.println(mdsi.getPresentationName() + " doesn't match.");
            }
        }
        
        if (!test_only) {
            proj.getDesignStudyManager().runStudies(studies);
        }
        
    }
    
    private boolean runStudy(MdxDesignStudy study) {
        Pattern p = Pattern.compile(regularExpression);
        return p.matcher(study.getPresentationName()).find();
    }    
}
