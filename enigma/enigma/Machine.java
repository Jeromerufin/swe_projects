package enigma;

import java.util.Collection;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.Arrays;

import static enigma.EnigmaException.*;

/** Class that represents a complete enigma machine.
 *  @author Jerome Rufin
 */
class Machine {

    /** A new Enigma machine with alphabet ALPHA, 1 < NUMROTORS rotor slots,
     *  and 0 <= PAWLS < NUMROTORS pawls.  ALLROTORS contains all the
     *  available rotors. */
    Machine(Alphabet alpha, int numRotors, int pawls,
            Collection<Rotor> allRotors) {
        _alphabet = alpha;
        _numRotors = numRotors;
        _pawls = pawls;
        _allRotors = allRotors;
        _rotorNameMap = new HashMap<>();
        _orderedRotors = new Rotor[numRotors];
        for (Rotor r: _allRotors) {
            _rotorNameMap.put(r.name(), r);
        }
        checkMachine();
    }

    private void checkMachine() {
        if (_numRotors <= 1) {
            throw error("Number of rotors is invalid. "
                    + "Should be greater than 1");
        }
        if (_pawls < 0 || _pawls >= _numRotors) {
            throw error("Number of pawls is invalid. "
                    + "Should be greater than or equal to zero and less "
                    + "than number of rotors");
        }
    }
    /** Return the number of rotor slots I have. */
    int numRotors() {
        return _numRotors;
    }

    /** Return the number pawls (and thus rotating rotors) I have. */
    int numPawls() {
        return _pawls;
    }

    /** Return Rotor #K, where Rotor #0 is the reflector, and Rotor
     *  #(numRotors()-1) is the fast Rotor.  Modifying this Rotor has
     *  undefined results. */
    Rotor getRotor(int k) {
        return _orderedRotors[k];

    }

    Alphabet alphabet() {
        return _alphabet;
    }

    /** Set my rotor slots to the rotors named ROTORS from my set of
     *  available rotors (ROTORS[0] names the reflector).
     *  Initially, all rotors are set at their 0 setting. */
    void insertRotors(String[] rotors) {
        if (rotors.length != _orderedRotors.length) {
            throw error("invalid length for rotor name array");
        }
        Set<String> targetSet = new HashSet<>(Arrays.asList(rotors));
        if (rotors.length != targetSet.size()) {
            throw error("duplicates included in rotor settings");
        }
        for (int i = 0; i < rotors.length; i += 1) {
            if (_rotorNameMap.containsKey(rotors[i])) {
                _orderedRotors[i] = _rotorNameMap.get(rotors[i]);
            } else {
                throw error("rotor not in map");
            }
        }
        checkOrderedRotors();
    }

    void resetNotches(HashMap<String, String> h) {
        for (Rotor r: _orderedRotors) {
            if (r.rotates()) {
                r.setNotches(h.get(r.name()));
            }
        }
    }

    private void checkOrderedRotors() {
        if (!_orderedRotors[0].reflecting()) {
            throw error("first rotor needs to be a reflector");
        }
        int movingRotorCount = 0;
        for (int i = 1; i < _orderedRotors.length; i += 1) {
            if (_orderedRotors[i].reflecting()) {
                throw error("can't have reflectors except in first position");
            }
            if (_orderedRotors[i].rotates()) {
                movingRotorCount += 1;
            }
            if (i > _numRotors - _pawls && !_orderedRotors[i].rotates()) {
                throw error("can't have fixed rotor "
                        + "in positions greater than S-P");
            }
        }
        if (movingRotorCount != _pawls) {
            throw error("incorrect number of pawls to moving rotors");
        }
        if (!_orderedRotors[_orderedRotors.length - 1].rotates()) {
            throw error("need > 0 moving rotors");
        }

    }

    /** Set my rotors according to SETTING, which must be a string of
     *  numRotors()-1 characters in my alphabet. The first letter refers
     *  to the leftmost rotor setting (not counting the reflector).  */
    void setRotors(String setting) {
        if (setting.length() != _orderedRotors.length - 1) {
            throw error("invalid length for rotor settings");
        }
        for (int i = 1; i < _orderedRotors.length; i += 1) {
            int settingInt = _alphabet.toInt(setting.charAt(i - 1));
            _orderedRotors[i].set(settingInt);
        }
    }

    /** Modify setting to take into account RINGSETTING. */
    void applyRingSetting(String ringSetting) {
        for (int i = 1; i < _orderedRotors.length; i += 1) {
            int ringSettingInt = _alphabet.toInt(ringSetting
                    .charAt(i - 1));
            _orderedRotors[i].set(_orderedRotors[i].setting()
                    - ringSettingInt);
            if (_orderedRotors[i].rotates()) {
                String modifiedNotches = "";
                for (int j = 0; j < _orderedRotors[i]
                        .notches().length(); j += 1) {
                    int prevNotchInt = _orderedRotors[i].alphabet()
                            .toInt(_orderedRotors[i].notches().charAt(j));
                    int newNotchInt = _orderedRotors[i].permutation()
                            .wrap(prevNotchInt - ringSettingInt);
                    modifiedNotches += _orderedRotors[i].alphabet()
                            .toChar(newNotchInt);
                    _orderedRotors[i].setNotches(modifiedNotches);
                }

            }
        }
    }

    /** Return the current plugboard's permutation. */
    Permutation plugboard() {
        return _plugBoard;
    }

    /** Set the plugboard to PLUGBOARD. */
    void setPlugboard(Permutation plugboard) {
        _plugBoard = plugboard;
    }

    /** Returns the result of converting the input character C (as an
     *  index in the range 0..alphabet size - 1), after first advancing
     *  the machine. */
    int convert(int c) {
        advanceRotors();
        if (Main.verbose()) {
            System.err.printf("[");
            for (int r = 1; r < numRotors(); r += 1) {
                System.err.printf("%c",
                        alphabet().toChar(getRotor(r).setting()));
            }
            System.err.printf("] %c -> ", alphabet().toChar(c));
        }
        c = plugboard().permute(c);
        if (Main.verbose()) {
            System.err.printf("%c -> ", alphabet().toChar(c));
        }
        c = applyRotors(c);
        c = plugboard().permute(c);
        if (Main.verbose()) {
            System.err.printf("%c%n", alphabet().toChar(c));
        }
        return c;
    }

    /** Advance all rotors to their next position. */
    private void advanceRotors() {
        int [] priorStateSettings = new int[_orderedRotors.length];
        for (int i = 0; i < _orderedRotors.length; i += 1) {
            priorStateSettings[i] = _orderedRotors[i].setting();
        }
        _orderedRotors[_orderedRotors.length - 1].
                set(_orderedRotors[_orderedRotors.length - 1].setting() + 1);
        for (int i = _orderedRotors.length - 2; i > 0; i -= 1) {
            if (_orderedRotors[i].rotates()) {
                char previousSettingChar = _alphabet.
                        toChar(priorStateSettings[i + 1]);
                if (_orderedRotors[i + 1].notches().
                        indexOf(previousSettingChar) != -1) {
                    _orderedRotors[i].set(_orderedRotors[i].
                            setting() + 1);
                    if (priorStateSettings[i + 1]
                            == _orderedRotors[i + 1].setting()) {
                        _orderedRotors[i + 1].set(_orderedRotors[i + 1].
                                setting() + 1);
                    }
                }
            }
        }
    }

    /** Return the result of applying the rotors to the character C (as an
     *  index in the range 0..alphabet size - 1). */
    private int applyRotors(int c) {
        int transformedInt = c;
        for (int i = _orderedRotors.length - 1; i > 0; i -= 1) {
            transformedInt = _orderedRotors[i].convertForward(transformedInt);
        }
        for (int i = 0; i < _orderedRotors.length; i += 1) {
            transformedInt = _orderedRotors[i].convertBackward(transformedInt);
        }
        return transformedInt;
    }

    /** Returns the encoding/decoding of MSG, updating the state of
     *  the rotors accordingly. */
    String convert(String msg) {
        String finalString = "";
        for (int i = 0; i < msg.length(); i += 1) {
            int charToConvert = _alphabet.toInt(msg.charAt(i));
            finalString += _alphabet.toChar(convert(charToConvert));
        }
        return finalString;
    }

    /** Common alphabet of my rotors. */
    private final Alphabet _alphabet;

    /** Number of rotors of machine. */
    private int _numRotors;

    /** Number of pawls of machine. */
    private int _pawls;

    /** Collection of all rotors available. */
    private Collection<Rotor> _allRotors;

    /** Array to order rotors. */
    private Rotor[] _orderedRotors;

    /** Map to access each rotor. */
    private HashMap<String, Rotor> _rotorNameMap;

    /** Plugboard permutation. */
    private Permutation _plugBoard;

}
