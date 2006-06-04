package jp.ac.keio.ae.comp.yamaguti.doddle;

/*
 * Created on 2005/03/01
 */

import java.io.*;
import java.util.*;

import javax.swing.tree.*;

import jp.ac.keio.ae.comp.yamaguti.doddle.data.*;
import jp.ac.keio.ae.comp.yamaguti.doddle.ui.*;

import org.semanticweb.mmm.mr3.plugin.*;

import com.hp.hpl.jena.rdf.model.*;

/**
 * @author takeshi morita
 * 
 */
public class DODDLEPlugin extends MR3Plugin {

    public void replaceRDFSModel(Model model) {
        mergeRDFSModel(model);
    }

    /**
     * DODDLE-JをMR3のプラグインとして起動
     */
    public void exec() {
        SplashWindow splashWindow = new SplashWindow();
        try {
            String jarPath = DODDLE.class.getClassLoader().getResource("").getFile();
            File file = new File(jarPath);
            String doddleHome = file.getParentFile().getPath() + "/";
            EDRDic.ID_DEFINITION_MAP = doddleHome + EDRDic.ID_DEFINITION_MAP;
            EDRDic.WORD_IDSET_MAP = doddleHome + EDRDic.WORD_IDSET_MAP;
            DODDLE.doddlePlugin = this;
            DODDLE.initOptions(new String[] { "lang=ja"});
            new DODDLE();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            splashWindow.setVisible(false);
        }
    }

    public void selectNodes(Set nodes) {
        this.selectClassNodes(nodes);
    }

    public void groupMR3Nodes(Set group) {
        this.groupClassNodes(group);
    }

    public void unGroupMR3Nodes(Set group) {
        this.unGroupClassNodes(group);
    }

    public Model getModel() {
        return getRDFSModel();
    }
}