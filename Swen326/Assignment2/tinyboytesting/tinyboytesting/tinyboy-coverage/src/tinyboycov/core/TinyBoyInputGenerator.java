package tinyboycov.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import tinyboy.core.ControlPad;
import tinyboy.core.ControlPad.Button;
import tinyboy.core.TinyBoyInputSequence;
import tinyboy.util.AutomatedTester;

/**
 * The TinyBoy Input Generator is responsible for generating and refining inputs
 * to try and ensure that sufficient branch coverage is obtained.
 *
 * @author David J. Pearce
 * @author Isaac Read
 */

public class TinyBoyInputGenerator
    implements
      AutomatedTester.InputGenerator<TinyBoyInputSequence> {

  /**
   * Current batch being processed.
   */
  private ArrayList<TinyBoyInputSequence> worklist = new ArrayList<>();

  /**
   * List of seeds to be used in generation of next sequences.
   */
  public ArrayList<InputRecord> seeds = new ArrayList<InputRecord>();
  
  /**
   * Max number of seeds.
   */
  private static final int MAXSEEDS = 10;

  /**
   * Random object used for generation of input sequences.
   */
  private static final Random rand = new Random();

  /**
   * Constructor - Generate all sequences of length 4 and add them to the
   * worklist.
   */
  public TinyBoyInputGenerator() {
    worklist.addAll(genAllInputSequences(4));
    randomSample(worklist, 75);
  }

  /**
   * Create all possible sequences of a given length.
   * 
   * @param sequenceLength
   *          Length of sequences.
   * @return ArrayList of sequences.
   */
  public ArrayList<TinyBoyInputSequence> genAllInputSequences(
      int sequenceLength) {
    ArrayList<TinyBoyInputSequence> sequenceList = new ArrayList<>();
    if (sequenceLength == 1) {
      sequenceList.add(new TinyBoyInputSequence(new Button[1]));
      for (ControlPad.Button button : ControlPad.Button.values()) {
        TinyBoyInputSequence seq = new TinyBoyInputSequence(button);
        sequenceList.add(seq);
      }
    } else {

      Button[] buttons = ControlPad.Button.values();
      for (int i = -1; i < buttons.length; i++) {
        ArrayList<TinyBoyInputSequence> smallerSequenceList = genAllInputSequences(
            sequenceLength - 1);
        for (int j = 0; j < smallerSequenceList.size(); j++) {
          if (i == -1) {
            sequenceList.add(smallerSequenceList.get(j).append(new Button[1]));
          } else {
            sequenceList.add(smallerSequenceList.get(j).append(buttons[i]));
          }
        }
      }
    }
    return sequenceList;
  }

  /**
   * Checks if worklist has anymore sequences.
   */
  @Override
  public boolean hasMore() {
    return worklist.size() > 0;
  }

  /**
   * Returns next input to use and removes it from worklist.
   */
  @Override
  public TinyBoyInputSequence generate() {
    if (!worklist.isEmpty()) {
      // remove last item from worklist
      return worklist.remove(0);
    } else {
      return null;
    }
  }

  /**
   * A record returned from the fuzzer indicating the coverage and final state
   * obtained for a given input sequence.
   */
  @Override
  public void record(TinyBoyInputSequence input, BitSet coverage,
      byte[] state) {

    insertIfImproveTotalCoverage(
        new InputRecord(input.toString(), coverage, state));
    String sequenceStr = generateNewSeqFromSeeds();
    worklist.add(inputFromString(sequenceStr));

  }

  /**
   * Generates a TinyBoyInputSequence from a string form.
   * 
   * @param sequenceStr
   *          Input string to use as source
   * @return
   */
  private TinyBoyInputSequence inputFromString(String sequenceStr) {
    Button[] buttons = new Button[sequenceStr.length()];
    for (int i = 0; i < buttons.length; i++) {
      switch (sequenceStr.charAt(i)) {
        case 'U' :
          buttons[i] = Button.UP;
          break;
        case 'D' :
          buttons[i] = Button.DOWN;
          break;
        case 'L' :
          buttons[i] = Button.LEFT;
          break;
        case 'R' :
          buttons[i] = Button.RIGHT;
          break;
        case '_' :
          buttons[i] = null;
          break;

        default :
          break;
      }
    }
    return new TinyBoyInputSequence(buttons);
  }

  /**
   * Creates an input sequence string using genetic algorithm techniques.
   * 
   * @return
   */
  private String generateNewSeqFromSeeds() {
    String baseSeed = seeds
        .get(rand.nextInt((seeds.size() / 2) + 1)).inputString;
    String mateSeed = seeds.get(rand.nextInt(seeds.size())).inputString;
    StringBuilder baseBuilder = new StringBuilder(baseSeed);
    if (rand.nextBoolean()) {
      baseBuilder = mate(baseBuilder, mateSeed, rand.nextInt(mateSeed.length()),
          rand.nextInt(4), rand.nextBoolean());
    }

    baseBuilder = appendChunk(baseBuilder, mateSeed,
        rand.nextInt(mateSeed.length() + 1));

    baseBuilder = mutate(baseBuilder, rand.nextInt(2), rand.nextBoolean());
    if (rand.nextBoolean()) {
      baseBuilder = addBlanks(baseBuilder, rand.nextInt(4), rand.nextBoolean());
    }
    return baseBuilder.toString();
  }

  /**
   * Adds of one chunk to end of another seed.
   * 
   * @param baseBuilder
   *          The base seed to be added to
   * @param mateSeed
   *          The seed which is added
   * @param chunkSize
   *          The length of the chunk added to the end
   * @return
   */
  private static StringBuilder appendChunk(StringBuilder baseBuilder,
      String mateSeed, int chunkSize) {
    if (chunkSize > mateSeed.length()) {
      chunkSize = mateSeed.length();
    }
    int mateStartIndex = rand.nextInt(mateSeed.length() - chunkSize + 1);
    String currChunk = mateSeed.substring(mateStartIndex,
        mateStartIndex + chunkSize);
    baseBuilder.append(currChunk);
    return baseBuilder;
  }

  /**
   * Add chunks blanks to button sequence.
   * 
   * @param baseSeed
   *          Seed to have blanks added.
   * @param numBlanks
   *          Number of blanks to add.
   * @param append
   *          If set true will add to end rather then in middle of seed.
   * @return
   */
  private StringBuilder addBlanks(StringBuilder baseSeed, int numBlanks,
      boolean append) {
    int index = rand.nextInt(baseSeed.length());
    for (int i = 0; i < numBlanks; i++) {
      if (append) {
        baseSeed.append('_');
      } else {
        baseSeed.insert(index, '_');
      }
    }
    return baseSeed;
  }

  /**
   * Randomly alter some of the seed.
   * 
   * @param baseSeed
   *          Seed to be altered.
   * @param amount
   *          Amount of pulses to be altered.
   * @param append
   *          If true will add to end rather then replacing presses
   * @return
   */
  private static StringBuilder mutate(StringBuilder baseSeed, int amount,
      boolean append) {

    for (int i = 0; i < amount; i++) {

      int mutationNumber = rand.nextInt(5);
      int mutationPosition = rand.nextInt(baseSeed.length());
      String mutationChar = null;
      switch (mutationNumber) {
        case 0 :
          mutationChar = "U";
          break;
        case 1 :
          mutationChar = "D";
          break;
        case 2 :
          mutationChar = "L";
          break;
        case 3 :
          mutationChar = "R";
          break;
        case 4 :
          mutationChar = "_";
          break;
        default :
          break;
      }

      if (append == true) {
        baseSeed.append(mutationChar);
      } else {
        baseSeed.delete(mutationPosition, mutationPosition + 1);
        baseSeed.insert(mutationPosition, mutationChar);
      }
    }
    return baseSeed;
  }

  /**
   * Combine 2 seeds to create a unique seed.
   * @param baseSeed
   *       Seed which is added to.
   * @param mateSeed
   *       Seed which is added from.
   * @param chunkSize
   *        Length of chunks added from mateSeed.
   * @param numChunk
   *        Number of chunks added to base seed.
   * @param deleteFromEnd
   *        If true will delete pulses from end to maintain size
   *        rather then replacing pulses where they are added.
   * @return
   */
  private static StringBuilder mate(StringBuilder baseSeed, String mateSeed,
      int chunkSize, int numChunk, boolean deleteFromEnd) {

    if (chunkSize > mateSeed.length()) {
      chunkSize = mateSeed.length();
    }
    if (chunkSize > baseSeed.length()) {
      chunkSize = baseSeed.length();
    }
    for (int i = 0; i < numChunk; i++) {
      int mateStartIndex = rand.nextInt(mateSeed.length() - chunkSize + 1);
      String currChunk = mateSeed.substring(mateStartIndex,
          mateStartIndex + chunkSize);

      int baseStartMax = baseSeed.length() - (chunkSize) + 1;
      if (baseStartMax < 1) {
        baseStartMax = 1;
      }
      int baseStartIndex = rand.nextInt(baseStartMax);

      if (deleteFromEnd) {
        baseSeed.delete(baseSeed.length() - chunkSize, baseSeed.length());
      } else {
        baseSeed = baseSeed.delete(baseStartIndex, baseStartIndex + chunkSize);
      }

      baseSeed.insert(baseStartIndex, currChunk);

    }
    return baseSeed;
  }

  /*
   * Used for storing input information.
   */
  class InputRecord {
    protected String inputString;
    protected byte[] state;
    protected BitSet coverage;
    // protected int coverageAmmount;

    protected InputRecord(String input, BitSet coverage, byte[] state) {
      this.inputString = input;
      this.state = state;
      this.coverage = coverage;
      // this.coverageAmmount = coverage.cardinality();
    }
  }

  /**
   * Check if adding this input record will increase the total combined
   * coverage. Add to seed list if it does.
   * @param potentialSeed
   *    Seed which might be added
   */
  protected void insertIfImproveTotalCoverage(InputRecord potentialSeed) {
    int bestPotentialCoverage = 0;
    int bestSeedToReplace = -1;
    int baseCoverage = calcTotalCoverageWithReplacment(null, null);
    boolean memIsUnique = true;

    if (seeds.size() == 0 && potentialSeed.coverage.cardinality() > 0) {
      seeds.add(potentialSeed);
    } else {
      for (InputRecord seed : seeds) {
        if (Arrays.equals(seed.state, potentialSeed.state)) {
          memIsUnique = false;

        }
        int potentialCov = calcTotalCoverageWithReplacment(seed, potentialSeed);

        if (potentialCov > bestPotentialCoverage) {
          bestPotentialCoverage = potentialCov;
          bestSeedToReplace = seeds.indexOf(seed);
        }
      }

      if ((bestPotentialCoverage > baseCoverage)
          || (bestPotentialCoverage == baseCoverage && memIsUnique)) {
        if (seeds.size() > MAXSEEDS) {
          seeds.remove(bestSeedToReplace);
        }
        seeds.add(bestSeedToReplace, potentialSeed);

      }
    }
  }

  /**
   * Method used to calculate what the total coverage of the seed list would
   * be if potential seed was added at a specific location.
   * @param seedTobeReplaced
   *    Seed which is to be left out of calculation.
   * @param potentialSeed
   *    Seed to be added. 
   * @return
   */
  private int calcTotalCoverageWithReplacment(InputRecord seedTobeReplaced,
      InputRecord potentialSeed) {
    BitSet potentialCoverage = new BitSet(0);

    for (InputRecord seed : seeds) {
      if (seed != seedTobeReplaced) {
        potentialCoverage.or(seed.coverage);
      }
    }
    if (potentialSeed != null) {
      potentialCoverage.or(potentialSeed.coverage);
    }
    return potentialCoverage.cardinality();
  }

  /**
   * Check whether a given input sequence is completely subsumed by another.
   *
   * @param lhs
   *          The one which may be subsumed.
   * @param rhs
   *          The one which may be subsuming.
   * @return
   */
  public boolean subsumedBy(BitSet lhs, BitSet rhs) {

    assert lhs != null && rhs != null;
    for (int i = lhs.nextSetBit(0); i >= 0; i = lhs.nextSetBit(i + 1)) {
      if (!rhs.get(i)) {
        return false;
      }
    }
    return true;
  }

  /**
   * Reduce a given set of items to at most <code>n</code> inputs by randomly
   * sampling.
   *
   * @param inputs
   *          The inputs to be sampled.
   * @param n
   *          The number of samples.
   */
  private static <T> void randomSample(List<T> inputs, int n) {
    // Randomly shuffle inputs
    Collections.shuffle(inputs);
    // Remove inputs until only n remain
    while (inputs.size() > n) {
      inputs.remove(inputs.size() - 1);
    }
  }
}
