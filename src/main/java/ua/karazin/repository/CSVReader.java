package ua.karazin.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ua.karazin.model.Language;
import ua.karazin.model.PartOfSpeech;
import ua.karazin.model.Translation;
import ua.karazin.model.Word;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class CSVReader {

    @Autowired
    private WordRepository repository;

    private static final Map<String, PartOfSpeech> posMap = new HashMap<>(8);

    public CSVReader() {
        posMap.put("[n]", PartOfSpeech.NOUN);
        posMap.put("[adj]", PartOfSpeech.ADJECTIVE);
        posMap.put("[adv]", PartOfSpeech.ADVERB);
        posMap.put("[v]", PartOfSpeech.VERB);
        posMap.put("[c]", PartOfSpeech.CONJUNCTION);
        posMap.put("[i]", PartOfSpeech.INTERJECTION);
        posMap.put("[pre]", PartOfSpeech.PREPOSITION);
        posMap.put("[pro]", PartOfSpeech.PRONOUN);
    }

    public void load() {
        File file = new File("src/main/resources/terms.txt");
        Scanner sc;
        try {
            sc = new Scanner(file);
        } catch (FileNotFoundException e) {
            System.out.println("ERROR: Unable to find the file '" + file + "'");
            return;
        }

        List<Word> terms = new ArrayList<>();

        while (sc.hasNextLine()) {
            String line = sc.nextLine();
            Word word = parseTerm(line);
            terms.add(word);
        }

        repository.saveAll(terms);

        sc.close();
    }

    private String[] parseRow(String row) {
        return row.split("\\t");
    }

    private Word parseTerm(String row) {
        Word term = new Word();
        term.setLanguage(Language.EN);
        term.setTranslations(new ArrayList<>());

        String[] items = parseRow(row);

        term.setValue(items[0].trim());

        if (items.length >= 3 && items[2].startsWith("[")) {
            term.setTranscription(items[2].trim());
        }

        String ukrDefinition = null;
        if (items.length == 5) {
            term.setDefinition(items[3].trim());
            ukrDefinition = items[4].trim();
        }

        Pattern translationPattern = Pattern.compile("(\\d\\)\\s*)?([а-яА-Яієї\\s-+*./()]+)(\\[\\w+])?\\s*");
        Matcher matcher = translationPattern.matcher(items[1]);

        Matcher videoMatcher = Pattern.compile("video: ([\\w\\d]*\\.mov)").matcher(row);
        String videoPath = null;
        if(videoMatcher.find()) {
            videoPath = videoMatcher.group(1).trim();
            term.setVideoPath(videoPath);
        }

        while (matcher.find()) {
            Word word = Word.translation(matcher.group(2).trim(), Language.UKR);
            word.setDefinition(ukrDefinition);
            word.setVideoPath(videoPath);

            Translation translation = new Translation();

            translation.setLeft(term);
            translation.setRight(word);

            String partOfSpeech = matcher.group(3);
            if (partOfSpeech != null && !partOfSpeech.isEmpty()) {
                translation.setPartOfSpeech(posMap.get(partOfSpeech.trim()));
            }

            word.setTranslations(Collections.singletonList(translation));
            term.getTranslations().add(translation);
        }

//        Matcher synonymsMatcher = Pattern.compile("syn:(.*)").matcher(row);
////        if(synonymsMatcher.find()) {
////            term.setSynonyms(Arrays.asList(synonymsMatcher.group(1).trim().split(",")));
////        }


        return term;
    }
}
