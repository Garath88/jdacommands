package utils;

import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public final class WordBlacklistTest {
    private List<String> badInputs;
    private List<String> goodInputs;

    @Before
    public void init() {
        badInputs = Arrays.asList(
            "test lolicon", "loli", "ShoTaCon", "l o l i",
            "apa l o l i apa", "1 o l 1", "y o u ng", "y O u N G",
            "spic", "something spic");
        goodInputs = Arrays.asList(
            "nothing interesting", "p", "p e", "l o l", "youn",
            "teen", "ScA", "LoLa", "lola", "spice girls");
    }

    @Test
    public void testBadWords() {
        badInputs.forEach(badInput ->
            Assert.assertTrue(WordBlacklist.containsBadWord(badInput)));
    }

    @Test
    public void testValidWords() {
        goodInputs.forEach(goodInput ->
            Assert.assertFalse(WordBlacklist.containsBadWord(goodInput)));
    }

}
