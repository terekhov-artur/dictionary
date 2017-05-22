package ua.karazin.dto;

import javafx.util.Pair;
import ua.karazin.model.PartOfSpeech;
import ua.karazin.model.Translation;
import ua.karazin.model.Word;

import java.util.ArrayList;
import java.util.List;

public class TranslationDTO {
    private long wordId;
    private String word;
    private String transcription;
    private List<Pair<String, PartOfSpeech>> translations;

    public TranslationDTO(Word word) {
        translations = new ArrayList<>();

        wordId = word.getId();
        this.word = word.getValue();
        transcription = word.getTranscription();

        for (Translation translation : word.getTranslations()) {
            Word translationWord =
                    translation.getLeft().getValue().equals(word.getValue()) ? translation.getRight() : translation.getLeft();
            translations.add(new Pair<>(translationWord.getValue(), translation.getPartOfSpeech()));
        }
    }

    public long getWordId()
    {
        return wordId;
    }
    public void setWordId(long wordId)
    {
        this.wordId = wordId;
    }

    public String getWord()
    {
        return word;
    }
    public void setWord(String word)
    {
        this.word = word;
    }

    public List<Pair<String, PartOfSpeech>> getTranslations()
    {
        return translations;
    }
    public void setTranslations(List<Pair<String, PartOfSpeech>> translations)
    {
        this.translations = translations;
    }

    public String getTranscription()
    {
        return transcription;
    }

    public void setTranscription(String transcription)
    {
        this.transcription = transcription;
    }
}