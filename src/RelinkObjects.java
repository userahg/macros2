
import java.io.PrintWriter;
import java.io.StringWriter;
import star.mdx.MdxDesignStudy;
import star.mdx.MdxMacro;
import star.mdx.MdxProject;
import star.mdx.MdxReferenceMonitor;
import star.mdx.MdxReferencePlot;
import star.mdx.MdxReferenceReport;
import star.mdx.MdxReferenceScalarParameter;
import star.mdx.MdxReferenceScene;
import star.mdx.MdxReferenceSimulation;
import star.mdx.MdxStudyMonitor;
import star.mdx.MdxStudyParameterBase;
import star.mdx.MdxStudyPlot;
import star.mdx.MdxStudyResponse;
import star.mdx.MdxStudyScene;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author cd8unu
 */
public class RelinkObjects extends MdxMacro {

    MdxProject _proj;

    @Override
    public void execute() {
        _proj = getActiveMdxProject();

        for (MdxDesignStudy study : _proj.getDesignStudyManager().getDesignStudies()) {
            MdxReferenceSimulation refSim = study.getReferenceSimulationGroup().getObjects().toArray(new MdxReferenceSimulation[1])[0];
            for (MdxStudyParameterBase studyObj : study.getStudyParameters().getObjects()) {
                if (studyObj.getReferenceParameter() == null) {
                    String name = studyObj.getPresentationName();
                    _proj.println("Study " + study.getPresentationName() + " parameter " + name + " needs to be relinked");
                    boolean isCADClient = name.contains("\\");
                    String folder = isCADClient ? name.split("\\\\")[0] : "Simulation Parameters";
                    try {
                        MdxReferenceScalarParameter referenceObj = (MdxReferenceScalarParameter) refSim.getReferenceParameterManager().getReferenceParameter(folder, name);
                        study.getStudyParameters().relinkStudyParameter(studyObj, referenceObj);
                    } catch (Exception ex) {
                        print(ex);
                    }
                }
            }

            for (MdxStudyResponse studyObj : study.getStudyResponses().getObjects()) {
                if (studyObj.getReferenceReport() == null) {
                    _proj.println("Study " + study.getPresentationName() + " response " + studyObj.getPresentationName() + " needs to be relinked");
                    try {
                        MdxReferenceReport referenceObj = refSim.getReferenceReportManager().getReferenceReport(studyObj.getPresentationName());
                        study.getStudyResponses().relinkStudyResponse(studyObj, referenceObj);
                    } catch (Exception ex) {
                        print(ex);
                    }
                }
            }

            for (MdxStudyMonitor studyObj : study.getStudyMonitors().getObjects()) {
                if (studyObj.getReferenceMonitor() == null) {
                    _proj.println("Study " + study.getPresentationName() + " monitor " + studyObj.getPresentationName() + " needs to be relinked");
                    try {
                        MdxReferenceMonitor referenceObj = refSim.getReferenceMonitors().getReferenceMonitor(studyObj.getPresentationName());
                        study.getStudyMonitors().relinkStudyMonitor(studyObj, referenceObj);
                    } catch (Exception ex) {
                        print(ex);
                    }
                }
            }
            
            for (MdxStudyScene studyObj : study.getStudyScenes().getObjects()) {
                if (studyObj.getReferenceScene() == null) {
                    _proj.println("Study " + study.getPresentationName() + " scene " + studyObj.getPresentationName() + " needs to be relinked");
                    try {
                        MdxReferenceScene referenceObj = refSim.getReferenceSceneManager().getReferenceScene(studyObj.getPresentationName());
                        study.getStudyScenes().relinkStudyScene(studyObj, referenceObj);
                    } catch (Exception ex) {
                        print(ex);
                    }
                }
            }
            
            for (MdxStudyPlot studyObj : study.getStudyPlots().getObjects()) {
                if (studyObj.getReferencePlot()== null) {
                    _proj.println("Study " + study.getPresentationName() + " plot " + studyObj.getPresentationName() + " needs to be relinked");
                    try {
                        MdxReferencePlot referenceObj = refSim.getReferencePlotManager().getReferencePlot(studyObj.getPresentationName());
                        study.getStudyPlots().relinkStudyPlot(studyObj, referenceObj);
                    } catch (Exception ex) {
                        print(ex);
                    }
                }
            }
        }

    }

    private void print(Exception ex) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        ex.printStackTrace(pw);
        _proj.println(sw);
    }

}
