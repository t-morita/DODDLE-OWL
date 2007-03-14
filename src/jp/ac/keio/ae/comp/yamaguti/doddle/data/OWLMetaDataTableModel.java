/*
 * @(#)  2007/03/14
 */

package jp.ac.keio.ae.comp.yamaguti.doddle.data;

import javax.swing.table.*;

import jp.ac.keio.ae.comp.yamaguti.doddle.ui.*;
import jp.ac.keio.ae.comp.yamaguti.doddle.utils.*;

import com.hp.hpl.jena.rdf.model.*;

/**
 * @author takeshi morita
 */
public class OWLMetaDataTableModel extends DefaultTableModel {

    private NameSpaceTable nsTable;
    
    public OWLMetaDataTableModel(NameSpaceTable nstbl, Object[] columnNames, int rowCount) {
        super(columnNames, rowCount);
        nsTable = nstbl;
    }

    public void addRow(Object[] rowData) {
        super.addRow(rowData);

    }

    public void refreshTableModel() {
        for (int i = 0; i < getRowCount(); i++) {
            setValueAt(getValueAt(i, 0), i, 0);
            setValueAt(getValueAt(i, 1), i, 1);
        }
    }

    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        Object newValue = null;
        String resourceStr = "";
        Resource resource = null;
        if (aValue instanceof Resource) {
            resource = (Resource) aValue;
            resourceStr = resource.getURI();
        } else {
            resourceStr = aValue.toString();
        }
        if (getColumnName(columnIndex).equals("Property") && resourceStr.indexOf("http:") != -1) {
            if (resource == null) {
                resource = ResourceFactory.createResource(resourceStr);
            }
            String prefix = nsTable.getPrefix(Utils.getNameSpace(resource));
            if (prefix != null) {
                String localName = Utils.getLocalName(resource);
                newValue = prefix + ":" + localName;
            } else {
                newValue = resource.getURI();
            }
        } else {
            newValue = aValue;
        }
        super.setValueAt(newValue, rowIndex, columnIndex);
    }
}
