package utils;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.annotations.VisibleForTesting;

public final class WordBlacklist {
    private static final Logger LOGGER = LoggerFactory.getLogger(WordBlacklist.class);
    private static URL fileURL;
    static {
        try {
            File target = new File(WordBlacklist.class.getProtectionDomain()
                .getCodeSource()
                .getLocation()
                .toURI())
                .getParentFile();
            fileURL = new File(target, "blacklist.txt").toURI()
                .toURL();
        } catch (URISyntaxException | MalformedURLException e) {
            LOGGER.error("Failed to init blacklist", e);
        }
    }
    private static final TxtReader TXT_READER = new TxtReader(fileURL);
    private static Set<String> badWords;

    private WordBlacklist() {
        loadBadWordsList();
    }

    private static void loadBadWordsList() {
        Set<String> list = new HashSet<>();
        try {
            List<String> temp = TXT_READER.readTxtFile();
            for (String item : temp) {
                if (!list.add(item)) {
                    String warn = String.format("duplicated blacklist word \"%s\" found!",
                        item);
                    LOGGER.warn(warn);
                }
            }
        } catch (IOException e) {
            LOGGER.error("Failed to load blacklist file");
        }
        badWords = list;
    }

    @VisibleForTesting
    static boolean containsBadWord(String input) {
        if (StringUtils.isNotEmpty(searchBadWord(input))) {
            return true;
        }
        String debug = String.format("%s not found in blacklist", input);
        LOGGER.debug(debug);
        return false;
    }

    public static String searchBadWord(String input) {
        try {
            if (TXT_READER.isFileUpdated()) {
                loadBadWordsList();
                LOGGER.info("Loaded blacklist");
            }
            return findBadWordInList(input);
        } catch (IOException e) {
            LOGGER.error("Failed to check if blacklist was updated", e);
        }
        return "";
    }

    private static String findBadWordInList(String input) {
        String badWord = "";
        String haystack = input.toLowerCase()
            .replaceAll("\\s+", "");
        for (String needle : badWords) {
            if (needle.contains("|")) {
                String delimtedBadWord = needle.replaceAll("\\|", "");
                String pattern = "\\b" + delimtedBadWord + "\\b";
                Pattern p = Pattern.compile(pattern);
                Matcher m = p.matcher(input);
                if (m.find()) {
                    badWord = delimtedBadWord;
                    String debug = String.format("%s contains %s", input, delimtedBadWord);
                    LOGGER.debug(debug);
                }
            } else {
                if (haystack.contains(needle)) {
                    badWord = needle;
                    String debug = String.format("%s contains %s", input, needle);
                    LOGGER.debug(debug);
                }
            }
        }
        return badWord;
    }
}