package jp.ac.keio.ae.comp.yamaguti.doddle.data;

import jp.ac.keio.ae.comp.yamaguti.doddle.utils.*;

/*
 * @(#)  2005/05/20
 *
 */

/**
 * @author takeshi morita
 */
public class EvalConcept implements Comparable {

    private Concept concept;
    private double evalValue;

    public EvalConcept(Concept c, double v) {
        concept = c;
        evalValue = v;
    }

    public void setEvalValue(double ev) {
        evalValue = ev;
    }

    public double getEvalValue() {
        return evalValue;
    }

    public Concept getConcept() {
        return concept;
    }

    public int compareTo(Object o) {
        double ev = ((EvalConcept) o).getEvalValue();
        EvalConcept c = (EvalConcept) o;
        if (evalValue < ev) {
            return 1;
        } else if (evalValue > ev) {
            return -1;
        } else {
            if (concept == null) {
                return 1;
            } else if (c == null) { return -1; }
            return concept.getURI().compareTo(c.getConcept().getURI());
        }
    }

    public String toString() {
        if (concept == null) { return Translator.getString("DisambiguationPanel.NotAvailable"); }
        return "[" + String.format("%.3f", evalValue) + "]" + "[" + concept.getQName() + "]" + "[" + concept.getWord() + "]";
    }
}
