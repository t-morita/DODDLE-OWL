package jp.ac.keio.ae.comp.yamaguti.doddle.data;

import java.io.*;
import java.util.*;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.rdf.model.impl.*;

import jp.ac.keio.ae.comp.yamaguti.doddle.*;
import jp.ac.keio.ae.comp.yamaguti.doddle.ui.*;
import jp.ac.keio.ae.comp.yamaguti.doddle.utils.*;

/** 
 *  このクラスを編集した場合，DBを作成しなおす必要あり
 */

/**
 * @author takeshi morita
 */
public class Concept implements Serializable {

    private String uriStr;
    transient private Resource uri; 
    private String inputWord;
    private String jaWord;
    private String enWord;
    private String jaExplanation;
    private String enExplanation;

    public Concept() {
    }

    public Concept(Concept c) {
        uri = c.getResource();
        uriStr = c.getURI();
        jaWord = c.getJaWord();
        enWord = c.getEnWord();
        jaExplanation = c.getJaExplanation();
        enExplanation = c.getEnExplanation();
        if (c.getInputWord() != null && !c.getInputWord().equals("")) {
            inputWord = c.getInputWord();
        }
    }

    public Concept(String uri, String word) {
        setURI(uri);
        enWord = "";
        jaWord = word;
        enExplanation = "";
        jaExplanation = "";
    }   

    public Concept(String uri, String[] items) {
        setURI(uri);
        jaWord = removeNullWords(items[0]);
        enWord = removeNullWords(items[1]);
        jaExplanation = removeNullWords(items[2]);
        enExplanation = removeNullWords(items[3]);
        setInputWord();
    }

    public void setURI(String uri) {
        this.uri = ResourceFactory.createResource(uri);
    }

    private String removeNullWords(String str) {
        return str.replaceAll("\\*\\*\\*", "");
    }

    public void setInputWord() {
        String[] jaWords = jaWord.split("\t");
        String[] enWords = enWord.split("\t");
        if (DODDLE.LANG.equals("en")) {
            if (0 < enWords.length) {
                inputWord = enWords[0];
            } else if (0 < jaWords.length) {
                inputWord = jaWords[0];
            } else {
                inputWord = null;
            }
        } else {
            if (0 < jaWords.length) {
                inputWord = jaWords[0];
            } else if (0 < enWords.length) {
                inputWord = enWords[0];
            } else {
                inputWord = null;
            }
        }
    }

    public void setInputWord(String iw) {
        inputWord = iw;
    }

    public String getInputWord() {
        return inputWord;
    }

    public String getEnExplanation() {
        return enExplanation;
    }

    public void setEnExplanation(String str) {
        enExplanation = str;
    }

    public void setEnWord(String word) {
        enWord = word;
    }

    public void addEnWord(String word) {
        if (!Arrays.asList(enWord.split("\t")).contains(word)) {
            enWord += word + "\t";
        }
    }

    public String getEnWord() {
        return enWord;
    }

    public String[] getEnWords() {
        return enWord.split("\t");
    }

    public Resource getResource() {
        return uri;
    }
    
    public String getURI() {
        return uri.getURI();
    }

    public void setJaExplanation(String str) {
        jaExplanation = str;
    }

    public String getJaExplanation() {
        return jaExplanation;
    }

    public String getJaWord() {
        return jaWord;
    }

    public String[] getJaWords() {
        return jaWord.split("\t");
    }

    public void setJaWord(String word) {
        jaWord = word;
    }

    public void addJaWord(String word) {
        if (!Arrays.asList(jaWord.split("\t")).contains(word)) {
            jaWord += word + "\t";
        }
    }

    public String getWord() {
        if (DODDLE.LANG.equals("ja")) {
            return getJaMainWord();
        } else if (DODDLE.LANG.equals("en")) { return getEnMainWord(); }
        return getJaMainWord();
    }

    private String getJaMainWord() {
        if (0 < jaWord.length() && jaWord.charAt(0) == '\t') {
            jaWord = jaWord.replaceFirst("\t", "");
        }
        if (0 < enWord.length() && enWord.charAt(0) == '\t') {
            enWord = enWord.replaceFirst("\t", "");
        }
        String[] jaWords = jaWord.split("\t");
        String[] enWords = enWord.split("\t");
        if (inputWord != null && 0 < inputWord.length()) {
            return inputWord;
        } else if (0 < jaWord.length() && 0 < jaWords.length) {
            return jaWord.split("\t")[0];
        } else if (0 < jaExplanation.replaceAll("\\s*", "").length()) {
            return jaExplanation;
        } else if (0 < enWord.length() && 0 < enWords.length) {
            return enWord.split("\t")[0];
        } else if (0 < enExplanation.replaceAll("\\s*", "").length()) {
            return enExplanation;
        } else {
            return uri.getURI();
        }
    }

    private String getEnMainWord() {
        if (0 < jaWord.length() && jaWord.charAt(0) == '\t') {
            jaWord = jaWord.replaceFirst("\t", "");
        }
        if (0 < enWord.length() && enWord.charAt(0) == '\t') {
            enWord = enWord.replaceFirst("\t", "");
        }
        String[] jaWords = jaWord.split("\t");
        String[] enWords = enWord.split("\t");
        if (inputWord != null && 0 < inputWord.length()) {
            return inputWord;
        } else if (0 < enWord.length() && 0 < enWords.length) {
            return enWord.split("\t")[0];
        } else if (0 < enExplanation.replaceAll("\\s*", "").length()) {
            return enExplanation;
        } else if (0 < jaWord.length() && 0 < jaWords.length) {
            return jaWord.split("\t")[0];
        } else if (0 < jaExplanation.replaceAll("\\s*", "").length()) {
            return jaExplanation;
        } else {
            return uri.getURI();
        }
    }

    public String getNameSpace() {
        return Utils.getNameSpace(uri);
    }
    
    public String getLocalName() {    
        return Utils.getLocalName(uri);
    }
    
    public String getQName() {
        OntologySelectionPanel ontSelectionPanel = DODDLE.getCurrentProject().getOntologySelectionPanel();
        String prefix = ontSelectionPanel.getPrefix(getNameSpace());
        return prefix+":"+getLocalName(); 
    }

    public String toString() {
        return getWord() + "[" + getQName() + "]";
    }

    public boolean equals(Object c) {
        return uri.getURI().equals(((Concept) c).getURI());
    }
}
