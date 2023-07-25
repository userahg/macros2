package industrialTestCases;


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
public class RunStudies extends MdxMacro {
    
    String p_StudyName = "Design Study 2";

    @Override
    public void execute() {
        MdxProject proj = getActiveMdxProject();
	MdxDesignStudy study = proj.getDesignStudyManager().getDesignStudy(p_StudyName);
	study.runDesignStudy();
    }
    
}
