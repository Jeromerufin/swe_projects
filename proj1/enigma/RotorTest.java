package enigma;

import org.junit.Test;
import static org.junit.Assert.*;

import static enigma.TestUtils.UPPER;

public class RotorTest {
    String testCycles = "(AELTPHQXRU) (BKNW) (CMOY) (DFG) (IV) (JZ) (S)";
    String fromAlpha = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    String toAlpha = "EKMFLGDQVZNTOWYHXUSPAIBRCJ";
    Permutation perm = new Permutation(testCycles, UPPER);
    Rotor testMainRotor = new Rotor("mainRotor", perm);

    /* ***** TESTS ***** */
    @Test
    public void convertForwardTest() {
        int input = 11;
        int expectedOutput = 19;
        assertEquals(expectedOutput, testMainRotor.convertForward(input));

    }

    @Test
    public void convertBackwardTest() {
        int input = 11;
        int expectedOutput = 4;
        assertEquals(expectedOutput, testMainRotor.convertBackward(input));

    }

    @Test
    public void setCharTest() {
        char input = 'E';
        int expectedOutput = 4;
        testMainRotor.set(input);
        assertEquals(expectedOutput, testMainRotor.setting());


    }

    @Test
    public void atNotchTest() {
        char input = 'E';
        int expectedOutput = 4;
        testMainRotor.set(input);
        assertFalse(testMainRotor.atNotch());

    }

    @Test(expected = EnigmaException.class)
    public void checkDerangedReflector() {
        String cycles = "(A) (BD) (CO) (EJ) (FN) (GT) "
                + "(HK) (IV) (LM) (PW) (QZ) (SX) (UY)";
        Permutation reflectorPerm = new Permutation(cycles, UPPER);
        Rotor rotor = new Reflector("B", reflectorPerm);
    }
}

