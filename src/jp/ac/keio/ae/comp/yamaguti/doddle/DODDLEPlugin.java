package jp.ac.keio.ae.comp.yamaguti.doddle;

/*
 * Created on 2005/03/01
 */

import java.util.*;

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
        DODDLE.doddlePlugin = this;
        new Thread() {
            public void run() {
                SplashWindow splashWindow = new SplashWindow(null);
                try {
                    DODDLE.initOptions(new String[] {});
                    new DODDLE();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    splashWindow.setVisible(false);
                }
            }
        }.start();
    }

    public void selectClasses(Set nodes) {
        this.selectClassNodes(nodes);
    }

    public void selectProperties(Set nodes) {
        this.selectPropertyNodes(nodes);
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