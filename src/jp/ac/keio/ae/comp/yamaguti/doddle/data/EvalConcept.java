package jp.ac.keio.ae.comp.yamaguti.doddle.data;

/*
 * @(#)  2005/05/20
 *
 */

/**
 * @author takeshi morita
 */
public class EvalConcept implements Comparable {

    private Concept concept;
    private int evalValue;

    public EvalConcept(Concept c, int v) {
        concept = c;
        evalValue = v;
    }

    public int getEvalValue() {
        return evalValue;
    }

    public Concept getConcept() {
        return concept;
    }

    public int compareTo(Object o) {
        int ev = ((EvalConcept) o).getEvalValue();
        EvalConcept c = (EvalConcept) o;
        if (evalValue < ev) {
            return 1;
        } else if (evalValue > ev) {
            return -1;
        } else {
            return concept.getIdentity().compareTo(c.getConcept().getIdentity());
        }
    }

    public String toString() {
        if (concept == null) { return "該当なし"; }
        return "[" + evalValue + "]" + "[" + concept.getIdentity() + "]" + "[" + concept.getWord() + "]";
    }
}
