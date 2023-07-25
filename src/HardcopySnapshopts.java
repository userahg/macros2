
import star.mdx.MdxDesignSceneView;
import star.mdx.MdxDesignViewManager;
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
public class HardcopySnapshopts extends MdxMacro {

    @Override
    public void execute() {

        MdxProject proj = getActiveMdxProject();
        MdxDesignSceneView snap = ((MdxDesignSceneView) proj.get(MdxDesignViewManager.class).getDesignView("Scene Snapshot"));

        for (int i = 0; i < 488; i++) {
            String name = "s" + String.format("%03d", i + 1);
            snap.printAndWait(resolvePath("C:\\Projects\\HondaCADR\\imgs\\" + name + ".png"), 1, 1588, 910, true, false);
            snap.openNextScene();
        }
    }

}
