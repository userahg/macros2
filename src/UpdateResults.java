// Simcenter STAR-CCM+ macro: UpdateResults.java
// Written by Simcenter STAR-CCM+ 15.05.023-10-g4ed05e1


import java.util.*;

import star.mdx.*;

public class UpdateResults extends MdxMacro {

  @Override
  public void execute() {
    execute0();
  }

  private void execute0() {
    MdxProject project = getActiveMdxProject();
    MdxDesignStudy study = project.getDesignStudyManager().getDesignStudy("Opt 3");
    MdxAllDesignSet designSet = ((MdxAllDesignSet) study.getDesignSets().getDesignSet("All"));
    study.updateDesignSetResults(Arrays.<MdxDesignSet>asList(designSet));
  }
}
