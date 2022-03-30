package enigma;

import org.junit.Test;
import static org.junit.Assert.*;


public class AlphabetTest {

    Alphabet char1 = new Alphabet("ABCD");
    Alphabet char2 = new Alphabet("abcdefg012345");

    @Test
    public void sizeTest() {
        assertEquals("expecting a size of 4", 4, char1.size());
        assertEquals("expecting a size of 13", 13, char2.size());
    }

    @Test
    public void containsTest() {
        assertTrue(char1.contains('A'));
        assertFalse(char1.contains('Z'));
        assertTrue(char2.contains('a'));
    }

    @Test
    public void toCharTest() {
        assertEquals("expecting character A", 'A', char1.toChar(0));
        assertEquals("expecting character Z", 'B', char1.toChar(1));
        assertEquals("expecting character a", 'a', char2.toChar(0));
        assertEquals("expecting character 1", '1', char2.toChar(8));

    }

    @Test
    public void toIntTest() {
        assertEquals("expecting index 0", 0, char1.toInt('A'));
        assertEquals("expecting index 3", 3, char1.toInt('D'));
        assertEquals("expecting index 7", 7, char2.toInt('0'));
        assertEquals("expecting index 0", 0, char2.toInt('a'));

    }

    @Test
    public void allCharTest() {
        String testString = "1204abcD";
        Alphabet test = new Alphabet(testString);
        for (int i = 0; i < testString.length(); i += 1) {
            char curr = testString.charAt(i);
            assertEquals(curr, test.toChar(i));
            assertEquals(i, test.toInt(curr));
            assertTrue(test.contains(curr));
        }
    }

    @Test(expected = EnigmaException.class)
    public void duplicateAlphabetTest() {
        Alphabet duplicateChar = new Alphabet("Aabbwerg");
    }

    @Test(expected = EnigmaException.class)
    public void invalidWhiteSpaceAlphabetTest() {
        Alphabet invalidChar = new Alphabet(" ");
    }

    @Test(expected = EnigmaException.class)
    public void invalidParenthesesAlphabetTest() {
        Alphabet invalidChar = new Alphabet(")");
    }

    @Test(expected = EnigmaException.class)
    public void invalidStarAlphabetTest() {
        Alphabet invalidChar = new Alphabet("*324");
    }



}
