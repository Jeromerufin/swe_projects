package enigma;

import org.junit.Test;
import static org.junit.Assert.*;

import static enigma.TestUtils.*;

/** The suite of all JUnit tests for the Permutation class.
 *  @author Jerome Rufin
 */
public class PermutationTest {

    /** Testing time limit. */

    /* ***** TESTING UTILITIES ***** */

    private Permutation perm;
    private String alpha = UPPER_STRING;

    /** Check that perm has an alphabet whose size is that of
     *  FROMALPHA and TOALPHA and that maps each character of
     *  FROMALPHA to the corresponding character of TOALPHA, and
     *  vice-versa. TESTID is used in error messages. */
    private void checkPerm(String testId,
                           String fromAlpha, String toAlpha) {
        int N = fromAlpha.length();
        assertEquals(testId + " (wrong length)", N, perm.size());
        for (int i = 0; i < N; i += 1) {
            char c = fromAlpha.charAt(i), e = toAlpha.charAt(i);
            assertEquals(msg(testId, "wrong translation of '%c'", c),
                         e, perm.permute(c));
            assertEquals(msg(testId, "wrong inverse of '%c'", e),
                         c, perm.invert(e));
            int ci = alpha.indexOf(c), ei = alpha.indexOf(e);
            assertEquals(msg(testId, "wrong translation of %d", ci),
                         ei, perm.permute(ci));
            assertEquals(msg(testId, "wrong inverse of %d", ei),
                         ci, perm.invert(ei));
        }
    }

    /* ***** TESTS ***** */

    @Test
    public void checkIdTransform() {
        perm = new Permutation("", UPPER);
        checkPerm("identity", UPPER_STRING, UPPER_STRING);
        assertFalse(perm.derangement());
    }

    @Test
    public void testInvertChar() {
        String testCycles = "(AELTPHQXRU) (BKNW) (CMOY) (DFG) (IV) (JZ) (S)";
        perm = new Permutation(testCycles, UPPER);
        assertEquals('A', perm.invert('E'));
    }

    @Test
    public void checkComplicatedTransform() {
        String testCycles = "(AELTPHQXRU) (BKNW) (CMOY) (DFG) (IV) (JZ) (S)";
        String fromAlpha = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String toAlpha =   "EKMFLGDQVZNTOWYHXUSPAIBRCJ";
        perm = new Permutation(testCycles, UPPER);
        checkPerm("complicated", fromAlpha, toAlpha);
    }

    @Test
    public void checkDerangedTransform() {
        String testCycles = "(AELTPHQXRU) (BKNW) (CMOY) (DFG) (IV) (JZS)";
        String fromAlpha = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String toAlpha =   "EKMFLGDQVZNTOWYHXUJPAIBRCS";
        perm = new Permutation(testCycles, UPPER);
        checkPerm("deranged", fromAlpha, toAlpha);
        assertTrue(perm.derangement());
    }

    @Test
    public void missingCharCyclesTest() {
        String testCycles = "(AELTPHQXRU) (BKNW) (CMOY) (DFG) (IV)";
        String fromAlpha = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String toAlpha =   "EKMFLGDQVJNTOWYHXUSPAIBRCZ";
        perm = new Permutation(testCycles, UPPER);
        checkPerm("missingChars", fromAlpha, toAlpha);
        assertFalse(perm.derangement());
    }

    @Test(expected = EnigmaException.class)
    public void invalidEmptyPermutation() {
        String testCycles = "(AELTPHQXRU) (BKNW) (CMOY) (DFG) (IV) (JZ) ()";
        Permutation emptyCycle = new Permutation(testCycles, UPPER);

    }

    @Test(expected = EnigmaException.class)
    public void invalidParenthesesPermutation() {
        String testCycles = "(AELT(PHQXRU) (BK)NW) (CMOY) (DFG) (IV) (JZ) ( )";
        Permutation parenthesesCycle = new Permutation(testCycles, UPPER);
    }

    @Test(expected = EnigmaException.class)
    public void invalidWhiteSpacePermutation() {
        String testCycles = "(AELTPHQXRU) (BKNW) (CMOY) (DFG) (IV) (JZ) ( )";
        Permutation whiteSpaceCycle = new Permutation(testCycles, UPPER);
    }

    @Test(expected = EnigmaException.class)
    public void invalidCharsPermutation() {
        String testCycles = "(AEL*TPHQXRU) (BKNW) (CMOY) (DFG) (IV) (JZ) (S)";
        Permutation asteriskCycle = new Permutation(testCycles, UPPER);
    }

    @Test(expected = EnigmaException.class)
    public void invalidCharPlacementPermutation() {
        String testCycles = "(AELTPHQXRU) (BKNW) (CMOY) (DFG) (IV) (J) (S) Z";
        Permutation outOfCycle = new Permutation(testCycles, UPPER);

    }

    @Test
    public void missingCharTest() {
        String testCycles = "(AELTPHQXRU) (BKNW) (CMOY) (DFG) (IV) (JZ)";
        perm = new Permutation(testCycles, UPPER);
        assertEquals('S', perm.permute('S'));
    }

    @Test
    public void testInvertInt() {
        Permutation pt = new Permutation("(BACD)", new Alphabet("ABCD"));
        assertEquals(1, pt.invert(0));
        assertEquals(1, pt.invert(40));
        assertEquals(3, pt.invert(-3));
    }

    @Test
    public void testPermuteInt() {
        Permutation pt = new Permutation("(BACD)", new Alphabet("ABCD"));
        assertEquals(0, pt.permute(1));
        assertEquals(1, pt.permute(-1));
        assertEquals(0, pt.permute(5));
    }

}
