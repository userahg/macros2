// STAR-CCM+ macro: T.java
// Written by STAR-CCM+ 13.04.011


import star.mdx.*;

public class SetMacroPathAndRunStudy extends MdxMacro {

  @Override
  public void execute() {
    execute0();
  }

  private void execute0() {

    MdxProject proj = getActiveMdxProject();

    MdxDesignStudy study = proj.getDesignStudyManager().getDesignStudy("Optimization");
    MdxStudySettings settings = study.getStudySettings();
    MdxMacroFile mdxMacroFile_0 = ((MdxMacroFile) settings.getMacroFiles().getObject("Macro 1"));
    mdxMacroFile_0.setMacroFileName("/data/agency/analysis/aarong/hPressureMixer/UpdateCAD4CatheterOptimization.java");
    study.runDesignStudy();
  }
}
