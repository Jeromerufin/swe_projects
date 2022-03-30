package enigma;

import static enigma.EnigmaException.*;

/** Class that represents a rotating rotor in the enigma machine.
 *  @author Jerome Rufin
 */
class MovingRotor extends Rotor {

    /** A rotor named NAME whose permutation in its default setting is
     *  PERM, and whose notches are at the positions indicated in NOTCHES.
     *  The Rotor is initally in its 0 setting (first character of its
     *  alphabet).
     */
    MovingRotor(String name, Permutation perm, String notches) {
        super(name, perm);
        _notches = notches;
        for (int i = 0; i < _notches.length(); i += 1) {
            if (perm.alphabet().getChar().indexOf(_notches.charAt(i)) == -1) {
                throw error("notch not in alphabet");
            }
        }

    }

    @Override
    boolean rotates() {
        return true;
    }

    @Override
    void advance() {
        if (rotates()) {
            set(setting() + 1);
        }
    }

    @Override
    String notches() {
        return _notches;
    }

    @Override
    void setNotches(String newNotches) {
        _notches = newNotches;
    }

    /** notches for rotor. */
    private String _notches;


}
