/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package crmOpt;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import star.base.report.Report;
import star.common.FieldFunctionTypeOption;
import star.common.GlobalParameterManager;
import star.common.ScalarGlobalParameter;
import star.common.Simulation;
import star.common.StarMacro;
import star.common.StarPlot;
import star.common.Tag;
import star.common.TagManager;
import star.common.UserFieldFunction;

/**
 *
 * @author cd8unu
 */
public class MakeLocalSensFFCST extends StarMacro {

    boolean clean_up = false;
    String[] reportNames = new String[]{"Cl", "Cd", "Area_Constraint", "L_over_D"};
    String[] paramNames = new String[]{"P_TE_Thickness", "P_W_L0", "P_W_L1", "P_W_L2", "P_W_L3", "P_W_L4", "P_W_L5", "P_W_L6", "P_W_L7", "P_W_L8", "P_W_L9", "P_W_L10", "P_W_U0", "P_W_U1", "P_W_U2", "P_W_U3", "P_W_U4", "P_W_U5", "P_W_U6", "P_W_U7", "P_W_U8", "P_W_U9", "P_W_U10"};

    Simulation _sim;

    Report[] reports;
    ScalarGlobalParameter[] params;
    TagManager tagManager;
    private final String tag_name = "MLSFFCSTJAVA";

    @Override
    public void execute() {
        _sim = getActiveSimulation();
        try {
            initialize();
            deleteTaggedObjects();
            createGeomSensMappedFFs();
            createMappedLocalSensFFs();
            createManualLocalSenssFFs();
        } catch (Exception ex) {
            print(ex);
        }

    }

    private void initialize() throws Exception {
        tagManager = _sim.get(TagManager.class);
        ArrayList<Report> reportList = new ArrayList<>();
        ArrayList<ScalarGlobalParameter> paramList = new ArrayList<>();
        for (String s : reportNames) {
            try {
                Report r = _sim.getReportManager().getReport(s);
                if (r == null) {
                    throw new Exception();
                } else {
                    reportList.add(r);
                }
            } catch (Exception ex) {
                _sim.println("Report " + s + " not found!!! Cannot continue.");
                throw ex;
            }
        }
        reports = reportList.toArray(new Report[reportList.size()]);

        for (String s : paramNames) {
            try {
                ScalarGlobalParameter sgp = (ScalarGlobalParameter) _sim.get(GlobalParameterManager.class).getObject(s);
                if (sgp == null) {
                    throw new Exception();
                } else {
                    paramList.add(sgp);
                }
            } catch (Exception ex) {
                _sim.println("Parameter " + s + " not found!!! Cannot continue.");

            }
        }
        params = paramList.toArray(new ScalarGlobalParameter[paramList.size()]);
    }

    private void createGeomSensMappedFFs() {
        for (ScalarGlobalParameter sgp : params) {
            UserFieldFunction uff = _sim.getFieldFunctionManager().createFieldFunction();
            uff.getTagGroup().add(getTagAsTag());
            uff.getTypeOption().setSelected(FieldFunctionTypeOption.Type.VECTOR);
            uff.setPresentationName(getMappedGSFFName(sgp));
            uff.setSyncName(true);
            uff.setDefinition(getGeomSensMappedFFDefinition(sgp));
        }
    }
    
    private void createMappedLocalSensFFs() {
        for (Report r : reports) {
            for (ScalarGlobalParameter sgp : params) {
                UserFieldFunction uff = _sim.getFieldFunctionManager().createFieldFunction();
                uff.getTagGroup().add(getTagAsTag());
                uff.getTypeOption().setSelected(FieldFunctionTypeOption.Type.SCALAR);
                uff.setPresentationName(getMappedLocalSensFFName(r, sgp));
                uff.setSyncName(true);
                uff.setDefinition(getMappedLocalSensFFDefinition(r, sgp));
            }            
        }
    }
    
    private void createManualLocalSenssFFs() {
        for (Report r : reports) {
            for (ScalarGlobalParameter sgp : params) {
                UserFieldFunction uff = _sim.getFieldFunctionManager().createFieldFunction();
                uff.getTagGroup().add(getTagAsTag());
                uff.getTypeOption().setSelected(FieldFunctionTypeOption.Type.SCALAR);
                uff.setPresentationName(getManualLocalSensFFName(r, sgp));
                uff.setSyncName(true);
                uff.setDefinition(getManualLocalSensFFDefinition(r, sgp));
            }
        }
    }

    private String getUnfilteredSensitivityFFName(Report r) {
        if (r.getPresentationName().contains("Area")) {
            return "$${Adjoint1::Unfiltered Surface Sensitivity}";
        } else if (r.getPresentationName().contains("over")) {
            return "$${Adjoint2::Unfiltered Surface Sensitivity}";
        } else if (r.getPresentationName().contains("Cd")) {
            return "$${Adjoint3::Unfiltered Surface Sensitivity}";
        } else {
            return "$${Adjoint4::Unfiltered Surface Sensitivity}";
        }
    }
    
    private String getMappedGSFFName(ScalarGlobalParameter sgp) {
        String pName = getParamNameForFFName(sgp);
        String ffName = "Mapped_GS_" + pName;
        return ffName;
    }
    
    private String getManualGSFFName(ScalarGlobalParameter sgp) {
        String pName = getParamNameForFFName(sgp);
        String ffName = "GS_" + pName + "_Man";
        return ffName;
    }
    
    private String getMappedLocalSensFFName(Report r, ScalarGlobalParameter sgp) {
        String pName = getParamNameForFFName(sgp);
        String rName = getReportNameForFFName(r);
        String name = "Mapped_Local_Sensitivity_" + rName + "_" + pName;
        return name;
    }
    
    private String getManualLocalSensFFName(Report r, ScalarGlobalParameter sgp) {
        String pName = getParamNameForFFName(sgp);
        String rName = getReportNameForFFName(r);
        String name = "Manual_Local_Sensitivity_" + rName + "_" + pName;
        return name;
    }
    
    private String getParamNameForFFName(ScalarGlobalParameter sgp) {
        return sgp.getPresentationName().contains("TE") ? "TE" : sgp.getPresentationName().replace("P_W_", "");
    }
    
    private String getReportNameForFFName(Report r) {
        return r.getPresentationName().contains("Area") ? "Area" : r.getPresentationName();
    }

    private String getGeomSensMappedFFDefinition(ScalarGlobalParameter sgp) {
        String[] components = new String[]{"i", "j", "k"};
        String pName = sgp.getPresentationName();
        String prefix = "${MappedVertexSensitivity of Position w.r.t ";
        String ff = "[" + prefix + pName + "[" + components[0] + "]}";
        for (int i = 1; i <= 2; i++) {
            ff += ",\n" + prefix + pName + "[" + components[i] + "]}";
        }
        ff += "]";
        return ff;
    }
    
    private String getMappedLocalSensFFDefinition(Report r, ScalarGlobalParameter sgp) {
        String def = "dot(";
        def += getUnfilteredSensitivityFFName(r);
        def += ", ";
        def += "$${" + getMappedGSFFName(sgp) + "})";
        return def;
    }
    
    private String getManualLocalSensFFDefinition(Report r, ScalarGlobalParameter sgp) {
        String def = "dot(";
        def += getUnfilteredSensitivityFFName(r);
        def += ", ";
        def += "$${" + getManualGSFFName(sgp) + "})";
        return def;
    }

    private Collection<Tag> getTag() {
        Collection<Tag> tagCollection = new ArrayList<>();
        for (Tag ti : _sim.get(TagManager.class).getObjects()) {
            if (ti.getPresentationName().equals(tag_name)) {
                tagCollection = new ArrayList<>();
                tagCollection.add(ti);
                return tagCollection;
            }
        }

        Tag createdBytag = _sim.get(TagManager.class).createNewUserTag(tag_name);
        tagCollection.add(createdBytag);
        return tagCollection;
    }

    private Tag getTagAsTag() {
        return getTag().toArray(new Tag[1])[0];
    }

    private void deleteTaggedObjects() {
        int maxTries = 10;
        int count = 0;
        while (count < maxTries) {
            for (Report r : _sim.getReportManager().getObjects()) {
                if (r.getTagGroup().getObjects().contains(getTagAsTag())) {
                    _sim.deleteObject(r);
                }
            }
            count++;
        }

        count = 0;
        while (count < maxTries) {
            for (StarPlot sp : _sim.getPlotManager().getObjects()) {
                if (sp.getTagGroup().getObjects().contains(getTagAsTag())) {
                    _sim.deleteObject(sp);
                }
            }
            count++;
        }

        count = 0;
        while (count < maxTries) {
            for (UserFieldFunction uff : _sim.getFieldFunctionManager().getObjectsOf(UserFieldFunction.class)) {
                if (uff.getTagGroup().getObjects().contains(getTagAsTag())) {
                    _sim.deleteObject(uff);
                }
            }
            count++;
        }
    }

    private void print(Exception ex) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        ex.printStackTrace(pw);
        _sim.println(sw);
    }
}
