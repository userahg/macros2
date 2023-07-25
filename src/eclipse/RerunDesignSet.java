// Simcenter STAR-CCM+ macro: Test.java
// Written by Simcenter STAR-CCM+ 15.04.008
package eclipse;

import java.util.*;

import star.mdx.*;

public class RerunDesignSet extends MdxMacro {
    String _studyName = "Design Optmization";
    String _dsName = "Rerun";
    
  @Override
  public void execute() {
    MdxProject _project = getActiveMdxProject();
    MdxDesignStudy _study = _project.getDesignStudyManager().getDesignStudy(_studyName);
    MdxUserDesignSet rerunSet = (MdxUserDesignSet) _study.getDesignSets().getDesignSet(_dsName);
    _study.rerunDesignSets(Arrays.<MdxDesignSet>asList(rerunSet));
  }
}
