/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import star.base.neo.ClientServerObject;
import star.base.neo.ClientServerObjectManager;
import star.base.neo.ServerConnection;
import star.base.query.Query;
import star.base.query.QueryPredicate;
import star.base.query.QueryResult;
import star.base.query.TagOperator;
import star.base.query.TagPredicate;
import star.common.PartManager;
import star.common.Simulation;
import star.common.StarMacro;
import star.common.Tag;
import star.vis.AnnotationManager;
import star.vis.BackgroundColorMode;
import star.vis.LookupTableManager;
import star.vis.Scene;
import star.vis.ResampledVolumePart;
import star.vis.SceneImage;
import star.vis.SceneManager;
import star.vis.UserLookupTable;

/**
 *
 * @author aarong
 */
public class CopyObjectsAndSaveNewScenes extends StarMacro {

    Simulation _srceSim;
    Simulation _destSim;
    String _imgPath;
    ArrayList<SceneCopier> _sceneCopiers;
    int x = 1639;
    int y = 916;
    int mag = 1;

    @Override
    public void execute() {
        try {
            _destSim = getActiveSimulation();
            _srceSim = new Simulation("seahawkvis02", 47828);
            _imgPath = "/data/agency/aarong/catheter/img";
            _sceneCopiers = new ArrayList<>();
            copy();
            saveScenes();
//            _destSim.saveState(_destSim.getSessionDir() + File.separator + _destSim.getPresentationName() + ".sim");
            _srceSim.close(ServerConnection.CloseOption.Disconnect);
        } catch (Exception ex) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            ex.printStackTrace(pw);
            _destSim.println(sw.toString());
        }
    }

    private void saveScenes() {
        for (SceneCopier sc : _sceneCopiers) {
            sc.hardcopy(mag, x, y, _imgPath);
        }
    }

    private void copy() {
        copyObjectProperties();
        copyObjects();
    }

    private void copyObjects() {
        Collection<ClientServerObject> toCopy = getTaggedObjects("Copy");
        Iterator<ClientServerObject> iterator;

        for (int i = 1; i <= 4; i++) {
            iterator = toCopy.iterator();
            copyObjects(iterator, i);
        }
    }

    private void copyObjects(Iterator<ClientServerObject> iterator, int level) {

        while (iterator.hasNext()) {
            ClientServerObject src = iterator.next();
            if (getLevel(src) == level) {
                if (src instanceof ResampledVolumePart) {
                    ResampledVolumeCopier copier = new ResampledVolumeCopier((ResampledVolumePart) src);
                    copier.copy(true);
                } else if (src instanceof SceneImage) {
                    SceneImageCopier copier = new SceneImageCopier((SceneImage) src);
                    copier.copy(true);
                } else if (src instanceof Scene) {
                    SceneCopier copier = new SceneCopier((Scene) src);
                    copier.copy(true);
                    _sceneCopiers.add(copier);
                } else if (src instanceof UserLookupTable) {
                    LookupTableCopier copier = new LookupTableCopier((UserLookupTable) src);
                    copier.copy(true);
                }
            }
        }
    }

    private void copyObjectProperties() {
        Collection<ClientServerObject> toCopyProperties = getTaggedObjects("Copy Properties");
        Iterator<ClientServerObject> iterator = toCopyProperties.iterator();

        while (iterator.hasNext()) {
            ClientServerObject src = iterator.next();
            if (src instanceof ResampledVolumePart) {
                ResampledVolumeCopier copier = new ResampledVolumeCopier((ResampledVolumePart) src);
                copier.override();
            } else if (src instanceof SceneImage) {
                SceneImageCopier copier = new SceneImageCopier((SceneImage) src);
                copier.override();
            } else if (src instanceof Scene) {
                SceneCopier copier = new SceneCopier((Scene) src);
                copier.override();
                _sceneCopiers.add(copier);
            } else if (src instanceof UserLookupTable) {
                LookupTableCopier copier = new LookupTableCopier((UserLookupTable) src);
                copier.override();
            }
        }
    }

    private String getDesignNum() {
        String s = _destSim.getPresentationName();
        String s2 = s.replace("Design_", "");
        int i = Integer.parseInt(s2);
        String number = String.format("%03d", i);
        return number;
    }

    private int getLevel(ClientServerObject cso) {
        int level = 1;

        if (cso instanceof ResampledVolumePart) {
            level = 1;
        }

        if (cso instanceof Scene) {
            Scene s = (Scene) cso;
            if (s.getAnnotationPropManager().getObjects().size() < 2) {
                level = 2;
            }
        }

        if (cso instanceof SceneImage) {
            level = 3;
        }

        if (cso instanceof Scene) {
            Scene s = (Scene) cso;
            if (s.getAnnotationPropManager().getObjects().size() > 1) {
                level = 4;
            }
        }

        return level;
    }

    private Collection<ClientServerObject> getTaggedObjects(String tagName) {
        Tag copyTag = _srceSim.getTagManager().getObject(tagName);
        QueryPredicate qp = new TagPredicate(TagOperator.Contains, copyTag);
        Query taggedForCopyQuery = new Query(qp, Query.STANDARD_MODIFIERS);
        QueryResult results = taggedForCopyQuery.createResults(_srceSim);
        return results.getObjects();
    }

    private abstract class CSOCopier<T extends ClientServerObject> {

        final T _src;
        T _dest;

        public CSOCopier(T _src) {
            this._src = _src;
        }

        T getCopy() {
            return this._dest;
        }

        public abstract <K extends ClientServerObjectManager> K getManager();

        public T copy(boolean replace) {
            ClientServerObjectManager manager = getManager();
            boolean exists = manager.has(_src.getPresentationName());
            if (exists && !replace) {
                return null;
            }
            if (exists && replace) {
                return override();
            }
            _dest = copy();
            return _dest;
        }

        public T override() {
            ClientServerObjectManager manager = getManager();
            _dest = (T) manager.getObject(_src.getPresentationName());
            if (_dest == null) {
                return _dest;
            }
            _dest.copyProperties(_src);
            return _dest;
        }

        abstract T copy();

    }

    private class SceneCopier extends CSOCopier<Scene> {

        public SceneCopier(Scene s) {
            super(s);
        }

        @Override
        public <K extends ClientServerObjectManager> K getManager() {
            return (K) _destSim.getSceneManager();
        }

        public void hardcopy(int mag, int x, int y, String path) {
            String fs = File.separator;
            String dir = _dest.getPresentationName().replace(" ", "");
            _dest.setBackgroundColorMode(BackgroundColorMode.SOLID);
            _dest.setAxesVisible(false);
            _dest.printAndWait(path + fs + dir + fs + "img-" + getDesignNum() + ".png", mag, x, y, true, true);
        }

        @Override
        Scene copy() {
            Scene s = ((SceneManager) getManager()).createScene();
            s.setPresentationName(_src.getPresentationName());
            s.copyProperties(_src);
            return s;
        }
    }

    private class SceneImageCopier extends CSOCopier<SceneImage> {

        public SceneImageCopier(SceneImage si) {
            super(si);
        }

        @Override
        public <K extends ClientServerObjectManager> K getManager() {
            return (K) _destSim.getAnnotationManager();
        }

        @Override
        SceneImage copy() {
            SceneImage si = ((AnnotationManager) getManager()).createSceneImage();
            si.setPresentationName(_src.getPresentationName());
            si.copyProperties(_src);
            return si;
        }

    }

    private class ResampledVolumeCopier extends CSOCopier<ResampledVolumePart> {

        public ResampledVolumeCopier(ResampledVolumePart rvp) {
            super(rvp);
        }

        @Override
        public <K extends ClientServerObjectManager> K getManager() {
            return (K) _destSim.getPartManager();
        }

        @Override
        ResampledVolumePart copy() {
            ResampledVolumePart part = (ResampledVolumePart) ((PartManager) getManager()).createResampledVolumePart();
            part.setPresentationName(_src.getPresentationName());
            part.copyProperties(_src);
            return part;
        }

    }

    private class LookupTableCopier extends CSOCopier<UserLookupTable> {

        public LookupTableCopier(UserLookupTable ult) {
            super(ult);
        }

        @Override
        public <K extends ClientServerObjectManager> K getManager() {
            return (K) _destSim.get(LookupTableManager.class);
        }

        @Override
        UserLookupTable copy() {
            UserLookupTable table = (UserLookupTable) ((LookupTableManager) getManager()).createLookupTable();
            table.setPresentationName(_src.getPresentationName());
            table.copyProperties(_src);
            return table;
        }

    }
}
