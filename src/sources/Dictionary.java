package sources;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Dictionary {

    public static CopyOnWriteArrayList<String> readDictionaryWords(String file) {
        CopyOnWriteArrayList<String> dictionaryWords = new CopyOnWriteArrayList<>();
        try {
            List<String> words = Files.readAllLines(Path.of(file));
            dictionaryWords.addAll(words);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return dictionaryWords;
    }
}
