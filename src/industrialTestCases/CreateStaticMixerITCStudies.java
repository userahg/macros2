/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package industrialTestCases;

import java.util.ArrayList;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import star.base.neo.ClientServerObject;
import star.base.neo.NeoObjectVector;
import star.common.Cartesian2DPlot;
import star.common.TagManager;
import star.common.UserTag;
import star.common.graph.DataSet;
import star.mdx.MdxDataSet;
import star.mdx.MdxDesignStudy;
import star.mdx.MdxMacro;
import star.mdx.MdxProject;

/**
 *
 * @author cd8unu
 */
public class CreateStaticMixerITCStudies extends MdxMacro {

    MdxProject _proj;
    String _currentVersion;

    @Override
    public void execute() {
        _proj = getActiveMdxProject();
        _currentVersion = getCurrentVersion();

        if (getStudiesToCopy().isEmpty()) {
            _proj.println("Project already contains studies for current version.");
        } else {
            copyExistingStudies();
            for (Cartesian2DPlot plot : _proj.getPlotManager().getObjectsOf(Cartesian2DPlot.class)) {
                udpate2DPlot(plot);
            }
//            _proj.saveState(_proj.getSessionDir() + File.separator + _proj.getPresentationName() + ".dmprj");
        }
    }

    private void copyExistingStudies() {
        _proj = getActiveMdxProject();

        for (MdxDesignStudy mds : getStudiesToCopy()) {
            MdxDesignStudy newStudy = _proj.getDesignStudyManager().createDesignStudy();
            newStudy.copyProperties(mds);
            newStudy.setPresentationName(mds.getPresentationName().replace(getPreviousVersion(_currentVersion), _currentVersion));
            removeTag(mds);
            addTag(newStudy);
        }
    }

    private Collection<MdxDesignStudy> getStudiesToCopy() {
        String previousVers = getPreviousVersion(_currentVersion);
        ArrayList<MdxDesignStudy> studies = new ArrayList<>();

        if (currentVersionStudiesExist()) {
            return studies;
        }

        for (MdxDesignStudy mds : _proj.getDesignStudyManager().getDesignStudies()) {
            if (mds.getPresentationName().contains(previousVers)) {
                studies.add(mds);
            }
        }
        return studies;
    }

    private String getCurrentVersion() {
        String presName = _proj.getPresentationName();
        String[] nameSplit = _proj.getPresentationName().split("\\d{4}\\.\\d{1}");
        String currentVers = presName.replace(nameSplit[0], "");
        return currentVers;
    }

    private String getPreviousVersion(String currentVersion) {
        String[] split = currentVersion.split("\\.");
        String year = split[0];
        String vers = split[1];

        int year_int = Integer.parseInt(year);
        int vers_int = Integer.parseInt(vers);
        int old_year = vers_int > 1 ? year_int : year_int - 1;
        int old_vers = vers_int > 1 ? vers_int - 1 : 3;

        return Integer.toString(old_year) + "." + Integer.toString(old_vers);
    }

    private String getNextVersion(String currentVersion) {
        String[] split = currentVersion.split("\\.");
        String year = split[0];
        String vers = split[1];

        int year_int = Integer.parseInt(year);
        int vers_int = Integer.parseInt(vers);
        int next_year = vers_int < 3 ? year_int : year_int + 1;
        int next_vers = vers_int < 3 ? vers_int + 1 : 1;

        return Integer.toString(next_year) + "." + Integer.toString(next_vers);
    }

    private boolean currentVersionStudiesExist() {
        boolean exists = false;
        for (MdxDesignStudy mds : _proj.getDesignStudyManager().getDesignStudies()) {
            if (mds.getPresentationName().contains(_currentVersion)) {
                exists = true;
            }
        }
        return exists;
    }

    private void removeTag(ClientServerObject cso) {
        _proj.get(TagManager.class).setTags(cso, new NeoObjectVector(new Object[]{}));
    }

    private void addTag(ClientServerObject cso) {
        UserTag currentTag = (UserTag) _proj.get(TagManager.class).getObject("Current");
        _proj.get(TagManager.class).setTags(cso, new NeoObjectVector(new Object[]{currentTag}));
    }

    private void udpate2DPlot(Cartesian2DPlot plot) {
        _proj.println("Plot: " + plot.getPresentationName());
        for (DataSet ds : plot.getDataSetManager().getDataSets()) {
            MdxDataSet mds = (MdxDataSet) ds;
            String currentStudyName = mds.getDesignStudy().getPresentationName();
            Pattern p = Pattern.compile("\\d{4}\\.\\d{1}");
            Matcher m = p.matcher(currentStudyName);
            if (m.find()) {
                String initVersionNum = m.group();
                String nextVersionNum = getNextVersion(initVersionNum);
                String newStudyName = currentStudyName.replaceFirst("\\d{4}\\.\\d{1}", nextVersionNum);
                MdxDesignStudy study = _proj.getDesignStudyManager().getDesignStudy(newStudyName);
                _proj.println("\tData Set: " + mds.getPresentationName() + " uses study " + currentStudyName);
                _proj.println("\tData Set: " + mds.getPresentationName() + " being updated to " + newStudyName);
                _proj.println("");
                mds.setDesignStudy(study);
            }
            m = p.matcher(mds.getSeriesName());
            if (m.find()) {
                String currentName = mds.getSeriesName();
                String newName = currentName.replaceFirst("\\d{4}\\.\\d{1}", getNextVersion(m.group()));
                mds.setSeriesName(newName);
            }
        }
    }
}
