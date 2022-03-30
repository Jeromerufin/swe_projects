package enigma;

import java.util.HashMap;

import static enigma.EnigmaException.*;

/** Represents a permutation of a range of integers starting at 0 corresponding
 *  to the characters of an alphabet.
 *  @author Jerome Rufin
 */
class Permutation {

    /** Set this Permutation to that specified by CYCLES, a string in the
     *  form "(cccc) (cc) ..." where the c's are characters in ALPHABET, which
     *  is interpreted as a permutation in cycle notation.  Characters in the
     *  alphabet that are not included in any cycle map to themselves.
     *  Whitespace is ignored. */
    Permutation(String cycles, Alphabet alphabet) {
        _alphabet = alphabet;
        _cycles = cycles;
        _alphabetChars = _alphabet.getChar();
        _permutationMapping = new HashMap<>();
        _permutationMappingInverted = new HashMap<>();
        checkCycle();
        addSingleCycles(cycles);
        if (cycles != "") {
            _splitCycles = splitCycle(_cycles);
            for (String cycle: _splitCycles) {
                addCycle(cycle);
            }
        }
    }

    private void checkCycle() {
        if (_cycles != "") {
            String noWhiteSpace = _cycles.replace(" ", "");
            if (noWhiteSpace.contains("()")) {
                throw error("cycles contain empty cycle");
            }
            for (int i = 0; i < noWhiteSpace.length() - 1; i += 1) {
                char charBack = noWhiteSpace.charAt(i);
                char charFront = noWhiteSpace.charAt(i + 1);
                if (charBack == ')' && charFront != '(') {
                    throw error("cycles contain incorrectly "
                            + "positioned parentheses");
                }
            }
            String cleanedCycle = _cycles.replaceAll("[() ]", "");
            for (int i = 0; i < cleanedCycle.length(); i += 1) {
                if (_alphabetChars.indexOf(cleanedCycle.charAt(i)) == -1) {
                    throw error("cycles contain invalid characters");
                }
            }
        }
    }

    /** Add the cycle c0->c1->...->cm->c0 to the permutation, where CYCLE is
     *  c0c1...cm. */
    private void addCycle(String cycle) {
        int cycleLength = cycle.length();
        for (int i = 0; i < cycleLength - 1; i += 1) {
            _permutationMapping.put(cycle.charAt(i),
                    cycle.charAt(i + 1));
        }
        for (int i = cycleLength - 1; i > 0; i -= 1) {
            _permutationMappingInverted.put(cycle.charAt(i),
                    cycle.charAt(i - 1));
        }
    }

    private void addSingleCycles(String cycles) {
        for (int i = 0; i < _alphabet.size(); i += 1) {
            char currentChar = _alphabetChars.charAt(i);
            if (cycles.indexOf(currentChar) == -1) {
                _permutationMapping.put(currentChar, currentChar);
                _permutationMappingInverted.put(currentChar, currentChar);
            }
        }
    }

    private String[] splitCycle(String cycles) {
        String cleanedCycle = cycles.replaceAll("[()]", " ").trim();
        String[] newCyclesSplit = cleanedCycle.split("\\s+", 0);
        int splitCount = newCyclesSplit.length;
        String[] newCyclesCleaned = new String[splitCount];
        for (int i = 0; i < splitCount; i += 1) {
            newCyclesCleaned[i] = newCyclesSplit[i]
                    + newCyclesSplit[i].charAt(0);
        }
        return newCyclesCleaned;
    }

    /** Return the value of P modulo the size of this permutation. */
    final int wrap(int p) {
        int r = p % size();
        if (r < 0) {
            r += size();
        }
        return r;
    }

    /** Returns the size of the alphabet I permute. */
    int size() {
        return _alphabet.size();
    }

    /** Return the result of applying this permutation to P modulo the
     *  alphabet size. */
    int permute(int p) {
        char pVal = _alphabetChars.charAt(wrap(p));
        char pMapped = _permutationMapping.get(pVal);
        return _alphabetChars.indexOf(pMapped);
    }

    /** Return the result of applying the inverse of this permutation
     *  to  C modulo the alphabet size. */
    int invert(int c) {
        char cVal = _alphabetChars.charAt(wrap(c));
        char cMapped = _permutationMappingInverted.get(cVal);
        return _alphabetChars.indexOf(cMapped);
    }

    /** Return the result of applying this permutation to the index of P
     *  in ALPHABET, and converting the result to a character of ALPHABET. */
    char permute(char p) {
        return _permutationMapping.get(p);
    }

    /** Return the result of applying the inverse of this permutation to C. */
    char invert(char c) {
        return _permutationMappingInverted.get(c);
    }

    /** Return the alphabet used to initialize this Permutation. */
    Alphabet alphabet() {
        return _alphabet;
    }

    /** Return true iff this permutation is a derangement (i.e., a
     *  permutation for which no value maps to itself). */
    boolean derangement() {
        for (Character key: _permutationMapping.keySet()) {
            if (_permutationMapping.get(key) == key) {
                return false;
            }
        }
        return true;
    }

    /** Alphabet of this permutation. */
    private Alphabet _alphabet;

    /** cycles for rotor. */
    private String _cycles;

    /** forward mapping for permutations. */
    private HashMap<Character, Character> _permutationMapping;

    /** backwards mapping for permutations. */
    private HashMap<Character, Character> _permutationMappingInverted;

    /** array to split cycles and store. */
    private String[] _splitCycles;

    /** chars in alphabet. */
    private String _alphabetChars;
}
