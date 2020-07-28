package avranalysis.core;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Stack;

import javr.core.AvrDecoder;
import javr.core.AvrInstruction;
import javr.core.AvrInstruction.AbsoluteAddress;
import javr.core.AvrInstruction.FlagRelativeAddress;
import javr.core.AvrInstruction.RelativeAddress;
import javr.io.HexFile;
import javr.memory.ElasticByteMemory;

public class StackAnalysis {
	/**
	 * Contains the raw bytes of the given firmware image being analysed.
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

	//private Stack<Integer> retValue = new Stack<Integer>();

	/**
	 *
	 * @param firmware
	 */
	public StackAnalysis(HexFile hf) {
		// Create firmware memory
		this.firmware = new ElasticByteMemory();
		// Upload image to firmware memory
		hf.uploadTo(firmware);
	}

	/**
	 * Apply the stack analysis to the given firmware image producing a maximum
	 * stack usage (in bytes).
	 *
	 * @return
	 */
	public int apply() {
		// Reset the maximum, height
		this.maxHeight = 0;
		// Traverse instructions starting at beginning
		HashMap<Integer, Integer> vistedInstructions = new HashMap();
		traverse(0, 0, vistedInstructions);
		// Return the maximum height observed
		return maxHeight;
	}

	/**
	 * Traverse the instruction at a given pc address, assuming the stack has a
	 * given height on entry.
	 *
	 * @param pc            Program Counter of instruction to traverse
	 * @param currentHeight Current height of the stack at this point (in bytes)
	 * @return
	 */
	private void traverse(int pc, int currentHeight, HashMap<Integer, Integer> visitedInstructions) {
		// Check whether current stack height is maximum
		maxHeight = Math.max(maxHeight, currentHeight);
		// Check whether we have terminated or not
		if ((pc * 2) >= firmware.size()) {
			// We've gone over end of instruction sequence, so stop.
			return;
		}
		// Process instruction at this address
		AvrInstruction instruction = decodeInstructionAt(pc);
		// Move to the next logical instruction as this is always the starting point.
		System.out.print(pc);
		System.out.print("    CH:");
		System.out.print(currentHeight);
		System.out.print("    ");
		System.out.println(instruction.getOpcode());
		int next = pc + instruction.getWidth();
		//
		process(instruction, next, currentHeight,  visitedInstructions);
	}
	
	public boolean checkLoop(int pc, int currentHeight, HashMap<Integer, Integer> vistedInstructions) {
	 
	  Integer prevHeight = vistedInstructions.get(pc);
	  if (prevHeight != null) {
	    if (prevHeight < currentHeight) {
	      this.maxHeight = Integer.MAX_VALUE;
	      }
      return true;
	  }
    vistedInstructions.put(pc, currentHeight);
    return false;
	}

	/**
	 * Process the effect of a given instruction.
	 *
	 * @param instruction   Instruction to process
	 * @param pc            Program counter of following instruction
	 * @param currentHeight Current height of the stack at this point (in bytes)
	 */
	private void process(AvrInstruction instruction, int pc, int currentHeight, HashMap<Integer, Integer> visitedInstructions) {
		
		switch (instruction.getOpcode()) {
		case BREQ:
		case BRGE:
		case BRLT: {
		    if(!checkLoop(pc, currentHeight, visitedInstructions)) {
			RelativeAddress branch = (RelativeAddress) instruction;
			traverse(pc + branch.k, currentHeight, (HashMap<Integer, Integer>)visitedInstructions.clone());
			traverse(pc, currentHeight, visitedInstructions);
		    }
			break;
		}
		case SBRC:
		case SBRS: {
		  if(!checkLoop(pc, currentHeight, visitedInstructions)) {
			AvrInstruction nextInstruction = decodeInstructionAt(pc+1);
			traverse(pc + nextInstruction.getWidth(), currentHeight, (HashMap<Integer, Integer>)visitedInstructions.clone());
			traverse(pc, currentHeight, (HashMap<Integer, Integer>)visitedInstructions.clone());	
			break;
		  }
		}
		case CALL: {
		  if(!checkLoop(pc, currentHeight, visitedInstructions)) {
			AbsoluteAddress branch = (AbsoluteAddress) instruction;
			
			//retValue.push(pc);
			traverse(branch.k, currentHeight + 2, (HashMap<Integer, Integer>)visitedInstructions.clone()); 
			traverse(pc, currentHeight, visitedInstructions); 
		  }
			break;
		}
		case RCALL: {
		  if(!checkLoop(pc, currentHeight, visitedInstructions)) {
			RelativeAddress branch = (RelativeAddress) instruction;
			if (branch.k != -1) {
				//retValue.push(pc);
				traverse(pc + branch.k, currentHeight + 2, (HashMap<Integer, Integer>)visitedInstructions.clone());
				traverse(pc, currentHeight, visitedInstructions); 
			}
		  }
			break;
		}
		case JMP: {
		  if(!checkLoop(pc, currentHeight, visitedInstructions)) {
			AbsoluteAddress branch = (AbsoluteAddress) instruction;
			//
			traverse(branch.k, currentHeight, visitedInstructions);
			break;
			// throw new RuntimeException("implement me!");
		  }
		}
		case RJMP: {
		  if(!checkLoop(pc, currentHeight, visitedInstructions)) {
			// NOTE: this one is implemented for you.
			RelativeAddress branch = (RelativeAddress) instruction;
			// Check whether infinite loop; if so, terminate.
			if (branch.k != -1) {
				// Explore the branch target
				traverse(pc + branch.k, currentHeight,  visitedInstructions);

			}
			//
		  }
			break;
		}
		case RET: {
			
			break;
		}
		case RETI:
			
			break;
		case PUSH:
			currentHeight++;
			traverse(pc, currentHeight,  visitedInstructions);
			break;
		case POP:
			currentHeight--;
			traverse(pc, currentHeight,  visitedInstructions);
			break;
		default:
			// Indicates a standard instruction where control is transferred to the
			// following instruction.
			traverse(pc, currentHeight,  visitedInstructions);
			break;
		}
	}

	/**
	 * Decode the instruction at a given PC location.
	 *
	 * @param pc
	 * @return
	 */
	private AvrInstruction decodeInstructionAt(int pc) {
		return decoder.decode(firmware, pc);
	}
}
