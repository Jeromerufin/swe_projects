package enigma;

import java.util.HashSet;
import java.util.Set;

import static enigma.EnigmaException.*;
/** An alphabet of encodable characters.  Provides a mapping from characters
 *  to and from indices into the alphabet.
 *  @author Jerome Rufin
 */
class Alphabet {

    /** A new alphabet containing CHARS. The K-th character has index
     *  K (numbering from 0). No character may be duplicated. */
    Alphabet(String chars) {
        _chars = chars;
        _charArray = _chars.toCharArray();
        _charSet = new HashSet<>();
        checkAlphabet();
    }

    /** A default alphabet of all upper-case characters. */
    Alphabet() {
        this("ABCDEFGHIJKLMNOPQRSTUVWXYZ");
    }

    /** Returns the size of the alphabet. */
    int size() {
        return _chars.length();
    }

    private void checkAlphabet() {
        for (int i = 0; i < _charArray.length; i += 1) {
            if ("*() ".indexOf(_charArray[i]) != -1) {
                throw error("Invalid characters in alphabet");
            }
            _charSet.add(_charArray[i]);
        }
        if (_charArray.length != _charSet.size()) {
            throw error("Alphabet contains duplicates");
        }
    }

    /** Returns true if CH is in this alphabet. */
    boolean contains(char ch) {
        for (char c: _charArray) {
            if (c == ch) {
                return true;
            }
        }
        return false;
    }

    /** Returns character number INDEX in the alphabet, where
     *  0 <= INDEX < size(). */
    char toChar(int index) {
        return _charArray[index];
    }

    /** Returns the index of character CH which must be in
     *  the alphabet. This is the inverse of toChar(). */
    int toInt(char ch) {
        for (int i = 0; i < size(); i += 1) {
            if (_charArray[i] == ch) {
                return i;
            }
        }
        throw error("index not in alphabet");
    }

    /** Returns chars. */
    String getChar() {
        return _chars;
    }

    /** chars instance variable. */
    private String _chars;

    /** chars instance variable converted to array. */
    private char[] _charArray;

    /** chars instance variable converted to set. */
    private Set<Character> _charSet;



}


