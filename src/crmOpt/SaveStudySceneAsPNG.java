package crmOpt;


import java.io.File;
import star.mdx.MdxDesign;
import star.mdx.MdxDesignSceneView;
import star.mdx.MdxDesignSet;
import star.mdx.MdxDesignStudy;
import star.mdx.MdxDesignViewManager;
import star.mdx.MdxMacro;
import star.mdx.MdxProject;
import star.mdx.MdxSuccessfulDesignSet;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author cd8unu
 */
public class SaveStudySceneAsPNG extends MdxMacro {

    MdxProject _proj;

    @Override
    public void execute() {
        _proj = getActiveMdxProject();
        MdxDesignStudy study = _proj.getDesignStudyManager().getDesignStudy("CADR9");
        MdxDesignSceneView snapshot = (MdxDesignSceneView) _proj.get(MdxDesignViewManager.class).getDesignView("GEOM");
        MdxSuccessfulDesignSet success = study.getDesignSets().getSuccessfulDesignSet();
        snapshot.setDesignSet(success);
        for (MdxDesign design : success.getDesigns()) {
            String path = design.getDirPath() + File.separator + "GEOM.png";
            snapshot.setDesign(design);
            _proj.println("Writing " + design.getPresentationName() + " to:\n\t" + path);
            snapshot.printAndWait(resolvePath(path), 1, 1200, 900, true, false);
        }
    }
}
