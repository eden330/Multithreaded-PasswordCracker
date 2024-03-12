package sources;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Scanner;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HashedPasswords {

    public static ConcurrentLinkedQueue<String> readHashedPasswords(String file) {
        ConcurrentLinkedQueue<String> hashedPasswordsList = new ConcurrentLinkedQueue<>();
        Pattern pattern = Pattern.compile("(?<password>[[a-z][0-9]]{32})");
        Matcher matcher;
        try {
            Scanner scanner = new Scanner(Path.of(file));
            while (scanner.hasNext()) {
                String hashedPassword = scanner.nextLine();
                matcher = pattern.matcher(hashedPassword);
                if (matcher.find()) {
                    hashedPasswordsList.add(matcher.group("password"));
                }
            }
            return hashedPasswordsList;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
