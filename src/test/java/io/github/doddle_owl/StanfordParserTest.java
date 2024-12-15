package io.github.doddle_owl;

import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.SentenceUtils;
import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class StanfordParserTest {
    public static void main(String[] args) {
        var taggerModel = DODDLE_OWL.class.getClassLoader().getResourceAsStream("pos_tagger_model/english-left3words-distsim.tagger");
        var tagger = new MaxentTagger(taggerModel);
        try (var reader = Files.newBufferedReader(Paths.get("./LICENSE"))) {
            List<List<HasWord>> sentenceList = MaxentTagger.tokenizeText(reader);
            for (List<HasWord> sentence : sentenceList) {
                List<TaggedWord> taggedWordList = tagger.tagSentence(sentence);
                for (TaggedWord tw : taggedWordList) {
                    System.out.println(tw.tag() + ", " + tw.word());
                }
                System.out.println(SentenceUtils.listToString(taggedWordList, false));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
