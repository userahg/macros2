
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Scanner;
import star.mdx.MdxDesign;
import star.mdx.MdxDesignSceneView;
import star.mdx.MdxDesignSet;
import star.mdx.MdxDesignStateEnum;
import star.mdx.MdxDesignStudy;
import star.mdx.MdxDesignViewManager;
import star.mdx.MdxMacro;
import star.mdx.MdxProject;
import star.mdx.MdxStudyScene;
import star.vis.AspectRatioEnum;
import star.vis.ViewManager;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author cd8unu
 */
public class CreateDesignImageKey extends MdxMacro {

    MdxProject _proj;
    MdxDesignStudy _study;

    @Override
    public void execute() {
        _proj = getActiveMdxProject();
        _study = _proj.getDesignStudyManager().getDesignStudy("Pos2");

        makeHardCopies();
        makeCSVKeyFile();
    }

    private void makeHardCopies() {
        MdxDesignSceneView snapshot = _proj.get(MdxDesignViewManager.class).createDesignSceneView("4Hardcopies");
        MdxDesignSet successful = _study.getDesignSets().getDesignSet("Successful");
        snapshot.setDesignStudy(_study);
        snapshot.setDesignSet(successful);
        snapshot.setIsViewLocked(true);
        snapshot.setAspectRatio(AspectRatioEnum.RATIO_4_3);

        try {
            for (MdxStudyScene mssi : getScenesToHardcopy()) {
                snapshot.setStudyScene(mssi);
                if (_proj.get(ViewManager.class).getObject("hardcopy") != null) {
                    snapshot.getCurrentView().setView(_proj.get(ViewManager.class).getObject("hardcopy"));
                }
                for (MdxDesign mdi : successful.getDesigns()) {
                    snapshot.setDesign(mdi);
                    _proj.println(mdi.getDirPath() + File.separator + mssi.getPresentationName() + ".png");
                    if (!(new File(mdi.getDirPath() + File.separator + mssi.getPresentationName() + ".png")).exists()) {
                        snapshot.printAndWait(mdi.getDirPath() + File.separator + mssi.getPresentationName() + ".png", 1, 1440, 1080, true, false);
                    }
                }
            }
        } catch (Exception ex) {
            print(ex);
        }
        _proj.get(MdxDesignViewManager.class).deleteDesignView(snapshot);
    }

    private void makeCSVKeyFile() {
        Collection<MdxDesign> designs = _study.getDesigns().getDesigns();

        try (FileWriter fileWriter = new FileWriter(_proj.getSessionDir() + File.separator + "key.csv")) {
            for (MdxDesign di : designs) {
                if (di.getDesignState().equals(MdxDesignStateEnum.Completed)) {
                    String entry[] = new String[2];
                    entry[0] = Integer.toString(di.getIndex());
                    File dir = new File(di.getDirPath());
                    entry[1] = dir.getName();
                    fileWriter.write(entry[0] + ", " + entry[1]);
                    fileWriter.write("\n");
                }
            }
            fileWriter.close();
        } catch (Exception ex) {
            print(ex);
        }
    }

    private Collection<MdxStudyScene> getScenesToHardcopy() {
        File sceneListFile = new File(_proj.getSessionDir() + File.separator + "scenes.list");
        if (sceneListFile.exists()) {
            Collection<MdxStudyScene> scenes = new ArrayList<>();
            try (Scanner sc = new Scanner(sceneListFile)) {
                while (sc.hasNextLine()) {
                    String sceneName = sc.nextLine();
                    MdxStudyScene mdxss = _study.getStudyScenes().getStudyScene(sceneName);
                    if (mdxss != null) {
                        scenes.add(mdxss);
                    } else {
                        _proj.println("Unable to locate scene " + sceneName + " in study " + _study.getPresentationName());
                    }
                }
                return scenes;
            } catch (FileNotFoundException ex) {
                return scenes;
            }
        } else {
            return _study.getStudyScenes().getStudyScenes();
        }
    }

    private void print(Exception ex) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        ex.printStackTrace(pw);
        _proj.print(sw.toString());
    }
}
