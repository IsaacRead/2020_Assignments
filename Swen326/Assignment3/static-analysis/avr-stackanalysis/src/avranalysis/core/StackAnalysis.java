package avranalysis.core;

import java.util.HashMap;
import javr.core.AvrDecoder;
import javr.core.AvrInstruction;
import javr.core.AvrInstruction.AbsoluteAddress;
import javr.core.AvrInstruction.RelativeAddress;
import javr.io.HexFile;
import javr.memory.ElasticByteMemory;

/**
 * Class for analyzing a program and finding the maximum stack height it could
 * need.
 * 
 * @author Isaac Read.
 */
public class StackAnalysis {

  /**
   * Contains the raw bytes of the given firmware image being analyzed.
   */
  private ElasticByteMemory firmware;

  /**
   * The decoder is used for actually decoding an instruction.
   */
  private AvrDecoder decoder = new AvrDecoder();

  /**
   * Records the maximum height seen so far.
   */
  private int maxHeight;

  /**
   * Create firmware memory from Hex file and upload to firmware.
   * 
   * @param hf
   *          Hex file to be uploaded
   */
  public StackAnalysis(HexFile hf) {
    // Create firmware memory
    this.firmware = new ElasticByteMemory();
    // Upload image to firmware memory
    hf.uploadTo(this.firmware);
  }

  /**
   * Apply the stack analysis to the given firmware image producing a maximum
   * stack usage (in bytes).
   *
   * @return The maximum height of stack the program could need.
   */
  public int apply() {
    // Reset the maximum, height
    this.maxHeight = 0;
    // Traverse instructions starting at beginning
    HashMap<Integer, Integer> vistedInstructions = new HashMap<>();
    traverse(0, 0, vistedInstructions);
    // Return the maximum height observed
    return this.maxHeight;
  }

  /**
   * Traverse the instruction at a given pc address, assuming the stack has a
   * given height on entry.
   *
   * @param pc
   *          Program Counter of instruction to traverse
   * @param currentHeight
   *          Current height of the stack at this point (in bytes)
   */
  private void traverse(int pc, int currentHeight,
      HashMap<Integer, Integer> visitedInstructions) {
    // Check whether current stack height is maximum
    this.maxHeight = Math.max(this.maxHeight, currentHeight);
    // Check whether we have terminated or not
    if ((pc * 2) >= this.firmware.size()) {
      // We've gone over end of instruction sequence, so stop.
      return;
    }
    // Process instruction at this address
    AvrInstruction instruction = decodeInstructionAt(pc);
    // Move to the next logical instruction as this is always the starting
    // point.
    // System.out.print(pc);
    // System.out.print(" CH:");
    // System.out.print(currentHeight);
    // System.out.print(" ");
    // System.out.println(instruction.getOpcode());
    int next = pc + instruction.getWidth();
    //
    process(instruction, next, currentHeight, visitedInstructions);
  }

  /**
   * Checks if current instruction has been visited before, i.e the program is
   * looping. If loop has increased stack height, assume worst and set max
   * height to max integer value.
   * 
   * @param pc
   *          Current line of code to check
   * @param currentHeight
   *          Current height of stack
   * @param vistedInstructions
   *          Map of visited lines of code and their stack heights
   * @return 
   *          True if in a loop, Else false
   */
  public boolean checkLoop(int pc, int currentHeight,
      HashMap<Integer, Integer> vistedInstructions) {

    Integer prevHeight = vistedInstructions.get(Integer.valueOf(pc));
    if (prevHeight != null) {
      if (prevHeight.intValue() < currentHeight) {
        this.maxHeight = Integer.MAX_VALUE;
      }
      return true;
    }
    vistedInstructions.put(Integer.valueOf(pc), Integer.valueOf(currentHeight));
    return false;
  }

  /**
   * Process the effect of a given instruction.
   *
   * @param instruction
   *          Instruction to process
   * @param pc
   *          Program counter of following instruction
   * @param currentHeight
   *          Current height of the stack at this point (in bytes)
   */
  private void process(AvrInstruction instruction, int pc, int currentHeight,
      HashMap<Integer, Integer> visitedInstructions) {
    int currentHeightVal = currentHeight;
    switch (instruction.getOpcode()) {
      case BRNE :
      case BRSH :
      case BRLO :
      case BREQ :
      case BRTC:
      case BRGE :
      case BRPL :
      case BRLT : {
        if (!checkLoop(pc, currentHeight, visitedInstructions)) {
          RelativeAddress branch = (RelativeAddress) instruction;
          traverse(pc + branch.k, currentHeight,
              new HashMap<>(visitedInstructions));
          traverse(pc, currentHeight, visitedInstructions);
        }
        break;
      }
      case SBRC :
      case SBRS : {

        if (!checkLoop(pc, currentHeight, visitedInstructions)) {
          AvrInstruction nextInstruction = decodeInstructionAt(pc + 1);
          traverse(pc + nextInstruction.getWidth(), currentHeight,
              new HashMap<>(visitedInstructions));
          traverse(pc, currentHeight, visitedInstructions);

        }

        break;
      }
      case CALL : {
        if (!checkLoop(pc, currentHeight, visitedInstructions)) {
          AbsoluteAddress branch = (AbsoluteAddress) instruction;

          // retValue.push(pc);
          traverse(branch.k, currentHeight + 2,
              new HashMap<>(visitedInstructions));
          traverse(pc, currentHeight, visitedInstructions);
        }
        break;
      }
      case RCALL : {
        if (!checkLoop(pc, currentHeight, visitedInstructions)) {
          RelativeAddress branch = (RelativeAddress) instruction;
          if (branch.k != -1) {
            // retValue.push(pc);
            traverse(pc + branch.k, currentHeight + 2,
                new HashMap<>(visitedInstructions));
            traverse(pc, currentHeight, visitedInstructions);
          }
        }
        break;
      }
      case JMP : {
        if (!checkLoop(pc, currentHeight, visitedInstructions)) {
          AbsoluteAddress branch = (AbsoluteAddress) instruction;
          //
          traverse(branch.k, currentHeight, visitedInstructions);
        }
        break;
        // throw new RuntimeException("implement me!");
      }
      case RJMP : {
        if (!checkLoop(pc, currentHeight, visitedInstructions)) {
          // NOTE: this one is implemented for you.
          RelativeAddress branch = (RelativeAddress) instruction;
          // Check whether infinite loop; if so, terminate.
          if (branch.k != -1) {
            // Explore the branch target
            traverse(pc + branch.k, currentHeight, visitedInstructions);

          }
          //
        }
        break;
      }
      case RET : {

        break;
      }
      case RETI :

        break;

      case PUSH :
        currentHeightVal++;
        traverse(pc, currentHeightVal, visitedInstructions);
        break;
      case POP :
        currentHeightVal--;
        traverse(pc, currentHeightVal, visitedInstructions);
        break;
      default :
        // Indicates a standard instruction where control is transferred to the
        // following instruction.
        System.out.println(instruction.getOpcode());
        traverse(pc, currentHeightVal, visitedInstructions);

        break;
    }
  }

  /**
   * Decode the instruction at a given PC location.
   *
   * @param pc
   *          program counter, location of line to be decoded
   * @return
   */
  private AvrInstruction decodeInstructionAt(int pc) {
    return this.decoder.decode(this.firmware, pc);
  }
}
