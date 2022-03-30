package enigma;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import java.util.List;
import java.util.Scanner;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.NoSuchElementException;


import ucb.util.CommandArgs;

import static enigma.EnigmaException.*;

/** Enigma simulator.
 *  @author Jerome Rufin
 */
public final class Main {

    /** Process a sequence of encryptions and decryptions, as
     *  specified by ARGS, where 1 <= ARGS.length <= 3.
     *  ARGS[0] is the name of a configuration file.
     *  ARGS[1] is optional; when present, it names an input file
     *  containing messages.  Otherwise, input comes from the standard
     *  input.  ARGS[2] is optional; when present, it names an output
     *  file for processed messages.  Otherwise, output goes to the
     *  standard output. Exits normally if there are no errors in the input;
     *  otherwise with code 1. */
    public static void main(String... args) {
        try {
            CommandArgs options =
                new CommandArgs("--verbose --=(.*){1,3}", args);
            if (!options.ok()) {
                throw error("Usage: java enigma.Main [--verbose] "
                            + "[INPUT [OUTPUT]]");
            }

            _verbose = options.contains("--verbose");
            new Main(options.get("--")).process();
            return;
        } catch (EnigmaException excp) {
            System.err.printf("Error: %s%n", excp.getMessage());
        }
        System.exit(1);
    }

    /** Open the necessary files for non-option arguments ARGS (see comment
      *  on main). */
    Main(List<String> args) {
        _config = getInput(args.get(0));

        if (args.size() > 1) {
            _input = getInput(args.get(1));
        } else {
            _input = new Scanner(System.in);
        }

        if (args.size() > 2) {
            _output = getOutput(args.get(2));
        } else {
            _output = System.out;
        }
    }

    /** Return a Scanner reading from the file named NAME. */
    private Scanner getInput(String name) {
        try {
            return new Scanner(new File(name));
        } catch (IOException excp) {
            throw error("could not open %s", name);
        }
    }

    /** Return a PrintStream writing to the file named NAME. */
    private PrintStream getOutput(String name) {
        try {
            return new PrintStream(new File(name));
        } catch (IOException excp) {
            throw error("could not open %s", name);
        }
    }

    /** Configure an Enigma machine from the contents of configuration
     *  file _config and apply it to the messages in _input, sending the
     *  results to _output. */
    private void process() {
        _notchesMap = new HashMap<>();
        Machine m = readConfig();
        while (_input.hasNextLine()) {
            String next = _input.nextLine();
            if (next.isEmpty()) {
                _output.println();
            } else {
                Scanner scan = new Scanner(next);
                if (scan.hasNext()) {
                    if (!scan.next().equals("*")) {
                        throw error("settings need * to initiate");
                    }
                    String settings = scan.nextLine();
                    setUp(m, settings);
                    while (!_input.hasNext("[*]") && _input.hasNext()) {
                        String inputNext = _input.nextLine();
                        String cleanedMsg = inputNext.replace(" ", "");
                        printMessageLine(m.convert(cleanedMsg));
                    }
                }
            }
        }
    }

    /** Return an Enigma machine configured from the contents of configuration
     *  file _config. */
    private Machine readConfig() {
        try {
            _rotorPawls = new ArrayList<>();
            _allRotors = new ArrayList<>();
            _alphabet = new Alphabet(_config.next());
            _rotorPawls.add(_config.nextInt());
            _rotorPawls.add(_config.nextInt());
            while (_config.hasNext()) {
                _allRotors.add(readRotor());
            }
            return new Machine(_alphabet, _rotorPawls.get(0),
                    _rotorPawls.get(1), _allRotors);
        } catch (NoSuchElementException excp) {
            throw error("configuration file truncated");
        }
    }

    /** Return a rotor, reading its description from _config. */
    private Rotor readRotor() {
        try {
            ArrayList<String> nameType = new ArrayList<>();
            String next = null;
            String notches = "";
            nameType.add(_config.next());
            nameType.add(_config.next());
            String cycles = getCycles(_config);
            if (nameType.get(1).charAt(0) == 'M') {
                for (int i = 1; i < nameType.get(1).length(); i += 1) {
                    char notch = nameType.get(1).charAt(i);
                    if (!_alphabet.contains(notch)) {
                        throw error("notch isn't included in alphabet");
                    }
                    notches += notch;
                }
                _notchesMap.put(nameType.get(0), notches);
                return new MovingRotor(nameType.get(0),
                        new Permutation(cycles, _alphabet), notches);
            } else if (nameType.get(1).charAt(0) == 'N') {
                return new FixedRotor(nameType.get(0),
                        new Permutation(cycles, _alphabet));
            } else if (nameType.get(1).charAt(0) == 'R') {
                return new Reflector(nameType.get(0),
                        new Permutation(cycles, _alphabet));
            } else {
                throw error("name has to indicate moving, "
                        + "non-moving, or reflector type rotor");
            }
        } catch (NoSuchElementException excp) {
            throw error("bad rotor description");
        }
    }

    /** Set M according to the specification given on SETTINGS,
     *  which must have the format specified in the assignment. */
    private void setUp(Machine M, String settings) {
        Scanner scan = new Scanner(settings);
        String cycles;
        String [] rotorNames = new String[_rotorPawls.get(0)];
        for (int i = 0; i < _rotorPawls.get(0); i += 1) {
            rotorNames[i] = scan.next();
        }
        M.insertRotors(rotorNames);
        M.setRotors(scan.next());
        M.resetNotches(_notchesMap);
        if (scan.hasNext("[^(]+")) {
            String ringSetting = getRingSetting(scan);
            M.applyRingSetting(ringSetting);
        }
        if (scan.hasNext()) {
            cycles = getCycles(scan);
            if (cycles.equals("")) {
                throw error("incorrect plugboard cycle format");
            }
        } else {
            cycles = "";
        }
        if (cycles.matches("\\([^()* )]{3,}\\)")) {
            throw error("plugboard has cycles longer than 2");
        }
        M.setPlugboard(new Permutation(cycles, _alphabet));

    }

    private String getRingSetting(Scanner s) {
        String ringSetting = s.next();
        if (ringSetting.length() != _rotorPawls.get(0) - 1) {
            throw error("incorrect number of characters for ringSetting");
        }
        for (int i = 0; i < ringSetting.length(); i += 1) {
            if (!_alphabet.contains(ringSetting.charAt(i))) {
                throw error("char in ringSetting not in alphabet");
            }
        }
        return ringSetting;
    }

    private String getCycles(Scanner s) {
        String cycles = "";
        while (s.hasNext("\\([^*]+\\)")) {
            cycles += s.next();
        }
        return cycles;
    }

    /** Return true iff verbose option specified. */
    static boolean verbose() {
        return _verbose;
    }

    /** Print MSG in groups of five (except that the last group may
     *  have fewer letters). */
    private void printMessageLine(String msg) {
        for (int i = 0; i < msg.length(); i += 1) {
            if (i != 0 && i % 5 == 0) {
                _output.print(" ");
            }
            _output.print(msg.charAt(i));
        }
        _output.println();
    }

    /** Alphabet used in this machine. */
    private Alphabet _alphabet;

    /** Source of input messages. */
    private Scanner _input;

    /** Source of machine configuration. */
    private Scanner _config;

    /** File for encoded/decoded messages. */
    private PrintStream _output;

    /** True if --verbose specified. */
    private static boolean _verbose;

    /** Array to store num rotors and pawls. */
    private ArrayList<Integer> _rotorPawls;

    /** Collection of all rotors. */
    private Collection<Rotor> _allRotors;

    /** notches Map used to reset notches. */
    private HashMap<String, String> _notchesMap;
}
