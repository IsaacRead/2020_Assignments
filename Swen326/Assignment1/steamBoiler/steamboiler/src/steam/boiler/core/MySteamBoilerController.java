package steam.boiler.core;

import org.eclipse.jdt.annotation.Nullable;
import steam.boiler.model.SteamBoilerController;
import steam.boiler.util.Mailbox;
import steam.boiler.util.Mailbox.Message;
import steam.boiler.util.Mailbox.MessageKind;
import steam.boiler.util.MemoryAnnotations.Initialisation;
import steam.boiler.util.SteamBoilerCharacteristics;


/**MySteamBoilerController.
 * @author Isaac Read
 *  
 *
 */
public class MySteamBoilerController implements SteamBoilerController {

  /**
   * Captures the various modes in which the controller can operate.
   *
   * @author David J. Pearce
   *
   */
  private enum State {
    /**
     * waiting.
     */
    WAITING, 
    /**
     * READY.
     */
    READY, /**
     * NORMAL.
     */
    NORMAL, /**
     * DEGRADED.
     */
    DEGRADED, /**
     * RESCUE.
     */
    RESCUE, /**
     * EMERGENCY_STOP.
     */
    EMERGENCY_STOP
  }

  /**level.
   * @author Isaac.
   *
   */
  private enum Level {
    /**
     * TOOHIGH.
     */
    TOOHIGH, /**
     * TOOLOW.
     */
    TOOLOW, /**
     * GOOD.
     */
    GOOD
  }

  /**
   * valveOpen.
   */
  private boolean valveOpen;
  /**
   * predictedWaterLevelMax.
   */
  private double predictedWaterLevelMax = -(Math.PI);
  /**
   * predictedWaterLevelMin.
   */
  private double predictedWaterLevelMin = -(Math.PI);
  /**
   * prevWL.
   */
  private double prevWL = -(Math.PI);
  /**
   *  prevSR.
   */
  private double prevSR = -(Math.PI);
  /**
   * firstRun.
   */
  private boolean firstRun = true;
  /**
   * faultyWL.
   */
  private boolean faultyWL = false;
  /**
   * faultySR.
   */
  private boolean faultySR = false;
  /**
   * faultyValve.
   * 
   */
  private boolean faultyValve = false;
  /**
   * faultyWaterLevelUnAcknowleged .
   */
  private boolean faultyWaterLevelUnAcknowleged = false;
  /**
   * faultySteamRateUnAcknowleged.
   */
  private boolean faultySteamRateUnAcknowleged = false;
  //boolean arrays represented as binary in short, 1 true, 0 false
  //right most bit is index 0
  //short limits max number of pumps to 16
  /**
   * expectedPumpsOn.
   */
  private short expectedPumpsOn = 0;
  /**
   * faultyControllers.
   */
  private short faultyControllers = 0; 
  /**
   * faultyPumps.
   */
  private short faultyPumps = 0;                             
  /**
   * faultyPumpsUnAcknowleged.
   */
  private short faultyPumpsUnAcknowleged = 0;
  /**
   * faultyControllersUnAcknowleged.
   */
  private short faultyControllersUnAcknowleged = 0;
  /**
   * stopCount.
   */
  private int stopCount = 0;

  /**
   * msgOpenPump.
   */
  private Message[] messagesToSend;
  /**
   * msgOpenPump.
   */
  int msgOpenPump;
  /**
   * msgPumpFailDet.
   */
  int msgClosePump;
  /**
   * msgPumpFailDet.
   */
  int msgPumpFailDet;
  /**
   * msgControlfailDet.
   */
  int msgControlfailDet;
  /**
   * msgPumpRepairAck.
   */
  int msgPumpRepairAck;
  /**
   * msgControlRepairAck.
   */
  int msgControlRepairAck;

  /**
   * Records the configuration characteristics for the given boiler problem.
   */
  private final SteamBoilerCharacteristics configuration;

  /**
   * Identifies the current mode in which the controller is operating.
   */
  private State mode = State.WAITING;

  /**
   * Construct a steam boiler controller for a given set of characteristics.
   *
   * @param configuration
   *          The boiler characteristics to be used.
   */

  public MySteamBoilerController(SteamBoilerCharacteristics configuration) {
    this.configuration = configuration;
    messagesToSend = new Message[11 + configuration.getNumberOfPumps() * 6];
    initMsgArray();
  }

  /**
   * init message array.
   */
  @Initialisation
  private void initMsgArray() {
    messagesToSend[0] = new Message(Mailbox.MessageKind.MODE_m,
        Mailbox.Mode.INITIALISATION);
    messagesToSend[1] = new Message(Mailbox.MessageKind.MODE_m,
        Mailbox.Mode.NORMAL);
    messagesToSend[2] = new Message(Mailbox.MessageKind.MODE_m,
        Mailbox.Mode.DEGRADED);
    messagesToSend[3] = new Message(Mailbox.MessageKind.MODE_m,
        Mailbox.Mode.RESCUE);
    messagesToSend[4] = new Message(Mailbox.MessageKind.MODE_m,
        Mailbox.Mode.EMERGENCY_STOP);
    messagesToSend[5] = new Message(Mailbox.MessageKind.PROGRAM_READY);
    messagesToSend[6] = new Message(Mailbox.MessageKind.VALVE);
    messagesToSend[7] = new Message(
        Mailbox.MessageKind.LEVEL_FAILURE_DETECTION);
    messagesToSend[8] = new Message(
        Mailbox.MessageKind.STEAM_FAILURE_DETECTION);
    messagesToSend[9] = new Message(
        Mailbox.MessageKind.LEVEL_REPAIRED_ACKNOWLEDGEMENT);
    messagesToSend[10] = new Message(
        Mailbox.MessageKind.STEAM_REPAIRED_ACKNOWLEDGEMENT);

    msgOpenPump = 11;
    msgClosePump = msgOpenPump + configuration.getNumberOfPumps();
    msgPumpFailDet = msgClosePump
        + configuration.getNumberOfPumps();
    msgControlfailDet = msgPumpFailDet
        + configuration.getNumberOfPumps();
    msgPumpRepairAck = msgControlfailDet
        + configuration.getNumberOfPumps();
    msgControlRepairAck = msgPumpRepairAck
        + configuration.getNumberOfPumps();

    for (int p = 0; p < configuration.getNumberOfPumps(); p++) {
      messagesToSend[msgOpenPump + p] = new Message(
          Mailbox.MessageKind.OPEN_PUMP_n, p);
      messagesToSend[msgClosePump + p] = new Message(
          Mailbox.MessageKind.CLOSE_PUMP_n, p);
      messagesToSend[msgPumpFailDet + p] = new Message(
          Mailbox.MessageKind.PUMP_FAILURE_DETECTION_n, p);
      messagesToSend[msgControlfailDet + p] = new Message(
          Mailbox.MessageKind.PUMP_CONTROL_FAILURE_DETECTION_n, p);
      messagesToSend[msgPumpRepairAck + p] = new Message(
          Mailbox.MessageKind.PUMP_REPAIRED_ACKNOWLEDGEMENT_n, p);
      messagesToSend[msgControlRepairAck + p] = new Message(
          Mailbox.MessageKind.PUMP_CONTROL_REPAIRED_ACKNOWLEDGEMENT_n, p);
    }
  }

  /**
   * This message is displayed in the simulation window, and enables a limited
   * form of debug output. The content of the message has no material effect on
   * the system, and can be whatever is desired. In principle, however, it
   * should display a useful message indicating the current state of the
   * controller.
   *
   * 
   */
  @Override
  public String getStatusMessage() {
    @Nullable
    String modeString = mode.toString();
    if (modeString != null) {
      return modeString;
    } else {
      return "no mode";
    }
  }

  /**
   * Process a clock signal which occurs every 5 seconds. This requires reading
   * the set of incoming messages from the physical units and producing a set of
   * output messages which are sent back to them.
   *
   * @param incoming
   *          The set of incoming messages from the physical units.
   * @param outgoing
   *          Messages generated during the execution of this method should be
   *          written here.
   */

  @Override
  public void clock(Mailbox incoming, Mailbox outgoing) {
    @Nullable
    Message levelMessage = extractOnlyMatch(MessageKind.LEVEL_v, incoming);
    @Nullable
    Message steamMessage = extractOnlyMatch(MessageKind.STEAM_v, incoming);
    short pumpStateMessages = extractPumpMsgsToShort(MessageKind.PUMP_STATE_n_b,
        incoming, configuration.getNumberOfPumps(), true);
    short pumpControlStateMessages = extractPumpMsgsToShort(
        MessageKind.PUMP_CONTROL_STATE_n_b, incoming,
        configuration.getNumberOfPumps(), true);
    if (transmissionFailure(levelMessage, steamMessage, pumpStateMessages,
        pumpControlStateMessages)) {
      // Level, steam and pump system messages required, so emergency stop.
      mode = State.EMERGENCY_STOP;
    } else {
      if (extractOnlyMatch(MessageKind.STOP, incoming) != null) {
        stopCount++;
      } else {
        stopCount = 0;
      }
      if (stopCount >= 3) {
        mode = State.EMERGENCY_STOP;
      }
      assert levelMessage != null;
      assert steamMessage != null;
      if (firstRun) {
        prevWL = levelMessage.getDoubleParameter();
        prevSR = steamMessage.getDoubleParameter();
        firstRun = false;
      }
      assert prevWL != -(Math.PI);
      assert prevSR != -(Math.PI);
      checkForRepairs(outgoing, incoming);
      checkForFaults(outgoing, incoming, levelMessage, steamMessage,
          pumpStateMessages, pumpControlStateMessages);
      //set predictions back to default after they have been used for fault checking
      predictedWaterLevelMin = -(Math.PI); 
      predictedWaterLevelMax = -(Math.PI);
      operateAppropriateMode(levelMessage, steamMessage, incoming, outgoing);
    }
    sendFailDetectMsgs(outgoing, incoming);
    sendModeMessage(outgoing);
  }

  
  /**
   * selects the appropriate mode of operation for steam boiler 
   * and operates accordingly.
   * @param levelMessage
   *    message containing water level info
   * @param steamMessage
   *    message containing steam rate info
   * @param incoming
   *    mailbox containing info from physical unit
   * @param outgoing
   *    mailbox to be sent to physical unit
   */
  private void operateAppropriateMode(Message levelMessage,
      Message steamMessage, Mailbox incoming, Mailbox outgoing) {
    double currWL = levelMessage.getDoubleParameter();
    double currSR = steamMessage.getDoubleParameter();
    if (mode == State.WAITING || mode == State.READY) {
      if ((mode == State.WAITING)
          && (extractOnlyMatch(MessageKind.STEAM_BOILER_WAITING,
              incoming) != null)) {
        if (faultySR || faultyWL) {
          // steam is not 0 or WL unit has failed, something is wrong
          mode = State.EMERGENCY_STOP;
        } else {
          getSteamBoilerReady(outgoing, currWL, currSR);
          calculateOptimalPumpCfg(outgoing, currWL, currSR);
        }
      } else if ((mode == State.READY)
          && (extractOnlyMatch(MessageKind.PHYSICAL_UNITS_READY,
              incoming) != null)) {
        calculateOptimalPumpCfg(outgoing, currWL, currSR);
        mode = State.NORMAL;
      }
    } else if (mode != State.EMERGENCY_STOP) {
      if (faultyWL && faultySR) {
        mode = State.EMERGENCY_STOP;
      } else if (faultyWL && faultyControllers == 0 & !faultySR) {
        mode = State.RESCUE;
        assert (faultyWL);
        assert (faultySR || faultyControllers != 0) == false;
        currWL = estWL(currSR);

      } else if (!(faultyWL) && (faultyControllers != 0 || faultySR || faultyValve
          || faultyPumps != 0)) {
        mode = State.DEGRADED;
        assert faultyWL == false;
        if (faultySR) {
          currSR = estSR(currWL);
        }
      } else {
        assert (faultyWL || faultyControllers != 0 || faultySR || faultyValve
            || faultyPumps != 0) == false;
        mode = State.NORMAL;
      }

      if (mode != State.EMERGENCY_STOP) {
        calculateOptimalPumpCfg(outgoing, currWL, currSR);
        checkPredictedWaterLevelIsInBounds();
      }
    }
    prevWL = currWL;
    prevSR = currSR;
  }

  /**
   * checks if any repair messages have been received
   * Will update variables accordingly.
   * @param outgoing
   *    messages to be sent to physical units
   * @param incoming
   *    messages received from physical units
   */
  private void checkForRepairs(Mailbox outgoing, Mailbox incoming) {
    if (extractOnlyMatch(MessageKind.LEVEL_REPAIRED, incoming) != null) {
      sendMsg(outgoing, 9);
      faultyWL = false;
    }
    if (extractOnlyMatch(MessageKind.STEAM_REPAIRED, incoming) != null) {
      sendMsg(outgoing, 10);
      faultySR = false;
    }
    short pumpRep = extractPumpMsgsToShort(MessageKind.PUMP_REPAIRED_n,
        incoming, 1, false);
    short cntlrRep = extractPumpMsgsToShort(MessageKind.PUMP_CONTROL_REPAIRED_n,
        incoming, 1, false);
    for (int i = 0; i < configuration.getNumberOfPumps(); i++) {
      if (pumpRep != -1 && getArrayBool(pumpRep, i)) {
        sendMsg(outgoing, msgPumpRepairAck + i);
        faultyPumps = setArrayBool(faultyPumps, i, false);
      }
      if (cntlrRep != -1 && getArrayBool(cntlrRep, i)) {
        sendMsg(outgoing, msgControlRepairAck + i);
        faultyControllers = setArrayBool(faultyControllers, i, false);
      }
    }
  }

  
  /**
   * Checks info provided from physical units for potentially faulty equipment
   * 
   * 
   * @param outgoing
   *    messages to be sent
   * @param incoming
   *    messages received
   * @param levelMessage
   * 
   * @param steamMessage
   * 
   * @param pumpStateMessages
   * 
   * @param pumpControlStateMessages
   * 
   */
  private void checkForFaults(Mailbox outgoing, Mailbox incoming,
      Message levelMessage, Message steamMessage, short pumpStateMessages,
      short pumpControlStateMessages) {
    if (checkWaterLevelObvious(levelMessage.getDoubleParameter()) && (faultyWL == false)) {
      faultyWL = true;
      faultyWaterLevelUnAcknowleged = true;
    }
    if (checkSteamRate(steamMessage.getDoubleParameter()) && (faultySR == false)) {
      faultySR = true;
      faultySteamRateUnAcknowleged = true;
    }
    if (faultySR && faultyWL) {
      mode = State.EMERGENCY_STOP;
      return;
    }
    if (firstRun || predictedWaterLevelMin == -Math.PI || predictedWaterLevelMax == -Math.PI) {
      // first cycle or no previous prediction, no point checking pump system
      return;
    }
    double currWL = levelMessage.getDoubleParameter();
    double currSR = steamMessage.getDoubleParameter();
    if (faultyWL) {
      assert faultySR == false;
      currWL = estWL(currSR);
    } else if (faultySR) {
      assert faultyWL == false;
      currSR = estSR(currWL);
    }
    Level actWLvsPred = compareWaterLevelToPrediction(currWL);
    checkPumpSystem(pumpControlStateMessages, pumpStateMessages, actWLvsPred,
        outgoing);
  }

  /**checkPumpSystem
   * 
   * @param pumpControlStateMessages
   * 
   * @param pumpStateMessages
   * 
   * @param actWLvsPred
   * 
   * @param outgoing
   * 
   */
  private void checkPumpSystem(short pumpControlStateMessages,
      short pumpStateMessages, Level actWLvsPred, Mailbox outgoing) {
    for (int p = 0; p < configuration.getNumberOfPumps(); p++) {
      if (getArrayBool(expectedPumpsOn, p) != getArrayBool(pumpControlStateMessages,
          p)
          && getArrayBool(expectedPumpsOn, p) == getArrayBool(pumpStateMessages,
              p)) {
        
        if (actWLvsPred == Level.GOOD 
            && (configuration.getPumpCapacity(p) * 5 
              > (predictedWaterLevelMax - predictedWaterLevelMin))) {
          //water level is good and prediction range is sufficiently small 
          //conclude controller fault
    
          if (getArrayBool(faultyControllers, p) == false) {
            faultyControllers = setArrayBool(faultyControllers, p, true);
            faultyControllersUnAcknowleged = setArrayBool(faultyControllersUnAcknowleged, p, true);
          }
        } else if (actWLvsPred == Level.TOOHIGH
            && getArrayBool(pumpControlStateMessages, p) == true) {
          assert (getArrayBool(expectedPumpsOn, p) == false);
          // WL higher then predicted and controller corroborates, pump failure
          expectedPumpsOn = setArrayBool(expectedPumpsOn, p, true);
          if (getArrayBool(faultyPumps, p) == false) {
            faultyPumps = setArrayBool(faultyPumps, p, true);
            faultyPumpsUnAcknowleged = setArrayBool(faultyPumpsUnAcknowleged, p, true);
          }
        } else if (actWLvsPred == Level.TOOLOW
            && getArrayBool(pumpControlStateMessages, p) == false) {
          assert (getArrayBool(expectedPumpsOn, p) == true);
          // WL higher then predicted and controller corroborates, pump failure
          expectedPumpsOn = setArrayBool(expectedPumpsOn, p, false);
          if (getArrayBool(faultyPumps, p) == false) {
            faultyPumps = setArrayBool(faultyPumps, p, true);
            faultyPumpsUnAcknowleged = setArrayBool(faultyPumpsUnAcknowleged, p, true);
          }
        }
      } else if (getArrayBool(expectedPumpsOn,
          p) == getArrayBool(pumpControlStateMessages, p)
          && getArrayBool(expectedPumpsOn, p) != getArrayBool(pumpStateMessages,
              p)) {
        // pump is not responding correctly, faulty
        if (getArrayBool(faultyPumps, p) == false) {
          faultyPumps = setArrayBool(faultyPumps, p, true);
          faultyPumpsUnAcknowleged = setArrayBool(faultyPumpsUnAcknowleged, p, true);
        }
        if (actWLvsPred == Level.GOOD) {
          // indicates pump is working (acting on messages) but not responding
          // correctly
          if (getArrayBool(expectedPumpsOn, p) == true) {
            // try to turn off faulty pump
            sendMsg(outgoing, msgClosePump + p);
            expectedPumpsOn = setArrayBool(expectedPumpsOn, p, false);
          }
        }
      } else if (getArrayBool(expectedPumpsOn,
          p) != getArrayBool(pumpControlStateMessages, p)
          && getArrayBool(expectedPumpsOn, p) != getArrayBool(pumpStateMessages,
              p)) {
        // indicates pump is stuck, not listing to messages
        faultyPumps = setArrayBool(faultyPumps, p, true);
        faultyPumpsUnAcknowleged = setArrayBool(faultyPumpsUnAcknowleged, p, true);
        if (getArrayBool(pumpControlStateMessages, p) == false) {
          expectedPumpsOn = setArrayBool(expectedPumpsOn, p, false);
        } else {
          expectedPumpsOn = setArrayBool(expectedPumpsOn, p, true);
        }
      }
    }
  }

  /**sendModeMessage.
   * @param outgoing
   * 
   */
  private void sendModeMessage(Mailbox outgoing) {
    switch (mode) {
      case WAITING :
        sendMsg(outgoing, 0); // initialize mode message
        break;
      case READY :
        sendMsg(outgoing, 0); // initialize mode message
        sendMsg(outgoing, 5); // PROGRAM_READY
        break;
      case NORMAL :
        sendMsg(outgoing, 1); // normal mode message
        break;
      case DEGRADED :
        sendMsg(outgoing, 2); // degraded mode message
        break;
      case RESCUE :
        sendMsg(outgoing, 3); // rescue mode message
        break;
      case EMERGENCY_STOP :
        sendMsg(outgoing, 4); // emergency Stop message
        break;
      default :
        break;
    }
  }

  /**sendMsg.
   * @param outgoing
   * 
   * @param index
   * 
   */
  private void sendMsg(Mailbox outgoing, int index) {
    Message msg = messagesToSend[index];
    assert msg != null;
    outgoing.send(msg);
  }

  /**
   * sendFailDetectMsgs.
   * @param outgoing
   * 
   * 
   * @param incoming
   * 
   */
  private void sendFailDetectMsgs(Mailbox outgoing, Mailbox incoming) {
    if (faultySteamRateUnAcknowleged) {
      if (extractOnlyMatch(MessageKind.STEAM_OUTCOME_FAILURE_ACKNOWLEDGEMENT,
          incoming) != null) {
        faultySteamRateUnAcknowleged = false;
      } else {
        sendMsg(outgoing, 8); // STEAM_FAILURE_DETECTION
      }
    }
    if (faultyWaterLevelUnAcknowleged) {
      if (extractOnlyMatch(MessageKind.LEVEL_FAILURE_ACKNOWLEDGEMENT,
          incoming) != null) {
        faultyWaterLevelUnAcknowleged = false;
      } else {
        sendMsg(outgoing, 7); // LEVEL_FAILURE_DETECTION
      }
    }
    short pumpAck = extractPumpMsgsToShort(
        MessageKind.PUMP_FAILURE_ACKNOWLEDGEMENT_n, incoming, 1, false);
    short cntlrAck = extractPumpMsgsToShort(
        MessageKind.PUMP_CONTROL_FAILURE_ACKNOWLEDGEMENT_n, incoming, 1, false);
    for (int i = 0; i < configuration.getNumberOfPumps(); i++) {
      if (pumpAck != -1 && getArrayBool(pumpAck, i)) {
        if (getArrayBool(faultyPumpsUnAcknowleged, i)) {
          faultyPumpsUnAcknowleged = setArrayBool(faultyPumpsUnAcknowleged, i, false);
        }
      } else if (getArrayBool(faultyPumpsUnAcknowleged, i)) {
        sendMsg(outgoing, msgPumpFailDet + i);
      }
      if (cntlrAck != -1 && getArrayBool(cntlrAck, i)) {
        if (getArrayBool(faultyControllersUnAcknowleged, i)) {
          faultyControllersUnAcknowleged = setArrayBool(faultyControllersUnAcknowleged, i, false);
        }
      } else if (getArrayBool(faultyControllersUnAcknowleged, i)) {
        sendMsg(outgoing, msgControlfailDet + i);
      }
    }
  }

  /**
   * getArrayBool.
   * @param data
   * 
   * @param index
   * 
   */
  private boolean getArrayBool(short data, int index) {
    int bit = (data >> index) & (byte) 1;
    assert (bit == 1 || bit == 0);
    if (bit == 1) {
      return true;
    } else {
      return false;
    }
  }

  /**
   * setArrayBool.
   * @param data
   * 
   * @param index
   * 
   * @param value
   * 
   */
  private short setArrayBool(short data, int index, boolean value) {
    if (value) {
      data = (short) (data | (1 << index));
    } else {
      data = (short) (data & ~(1 << index));
    }
    return data;
  }

  // private void printBoolData(short data) {
  // System.out.println("==" + String.valueOf(data));
  // for (int i = 0; i < configuration.getNumberOfPumps() + 3; i++) {
  // System.out.println(String.valueOf(i) + " :" +
  // String.valueOf(getArrayBool(data, i)));
  // }
  // }

  /**
   * estWL.
   * @param currSR
   * 
   */
  private double estWL(double currSR) {
    return prevWL + getExptWaterPumped() - (prevSR * 5 + currSR * 5) / 2;
  }

  /**
   * estSR.
   * @param currWL
   * 
   */
  private double estSR(double currWL) {
    double currSR = (prevSR + configuration.getMaximualSteamRate()) / 2;
    assert currSR >= prevSR;
    return currSR;
  }

  /**
   * compareWaterLevelToPrediction.
   * @param currWL
   * 
   */
  private Level compareWaterLevelToPrediction(double currWL) {
    assert predictedWaterLevelMax != -(Math.PI);
    assert predictedWaterLevelMin != -(Math.PI);
    if (currWL > predictedWaterLevelMax) {
      return Level.TOOHIGH;
    } else if (currWL < predictedWaterLevelMin) {
      return Level.TOOLOW;
    } else {
      return Level.GOOD;
    }    
  }

  /**
   * checkPredictedWaterLevelIsInBounds.
   */
  private void checkPredictedWaterLevelIsInBounds() {
    assert predictedWaterLevelMin != -(Math.PI);
    assert predictedWaterLevelMax != -(Math.PI);
    if ((predictedWaterLevelMin < configuration.getMinimalLimitLevel())
        || predictedWaterLevelMax > configuration.getMaximalLimitLevel()) {
      mode = State.EMERGENCY_STOP;
    }

  }

  /**
   * .checkWaterLevelObvious.
   * @param currWL
   * 
   */
  private boolean checkWaterLevelObvious(double currWL) {
    if (currWL > configuration.getCapacity() || (currWL < 0) // 3% allowed margin of error   
        || ((currWL - prevWL) < -1.03 * (configuration.getEvacuationRate() * 5
            + configuration.getMaximualSteamRate() * 5))) {    
      return true;
    } else {
      double pumpCapSum = 0;
      // sum the pump capacity's and check if the WL has increased more then
      // this
      for (int p = 0; p < configuration.getNumberOfPumps(); p++) {
        pumpCapSum += configuration.getPumpCapacity(p) * 5;
      }
      // 3% allowed margin of error
      if ((currWL - prevWL) > (pumpCapSum * 1.03)) { 
        return true;
      } else {
        return false;
      }
    }
  }

  /**
   * getExptWaterPumped.
   */
  private double getExptWaterPumped() {
    double pumpSum = 0;
    for (int p = 0; p < configuration.getNumberOfPumps(); p++) {
      if (getArrayBool(expectedPumpsOn, p) == true) {
        pumpSum += configuration.getPumpCapacity(p) * 5;
      }
    }
    return pumpSum;
  }

  /**checkSteamRate.
   * @param currSR
   * 
   */
  private boolean checkSteamRate(double currSR) {
    if ((currSR != 0 && mode == State.WAITING) || (currSR < prevSR)
        || (currSR == prevSR && currSR != configuration.getMaximualSteamRate()
            && !(mode == State.WAITING || mode == State.READY))
        || (currSR > configuration.getMaximualSteamRate())) {
      return true;
    } else {
      return false;
    }
  }

  /**
   * getSteamBoilerReady.
   * @param outgoing
   * 
   * @param currWL
   * 
   * @param currSR
   * 
   */
  private void getSteamBoilerReady(Mailbox outgoing, double currWL, double currSR) {
    if (currWL > configuration.getMinimalNormalLevel()
        && currWL < configuration.getMaximalNormalLevel()) {
      // Water level is normal
      mode = State.READY;
      if (valveOpen == true) {
        sendMsg(outgoing, 6); // close valve message
        valveOpen = false;
      }

    } else if (currWL >= configuration.getMaximalNormalLevel()) {
      // needs less water, open valve
      if (valveOpen == false) {
        sendMsg(outgoing, 6);// open valve message
        valveOpen = true;
      }
    } else if (currWL <= configuration.getMinimalNormalLevel()
        && valveOpen == true) {
      // valve has emptied to much water in past cycle
      sendMsg(outgoing, 6);// close valve message
      valveOpen = false;
    }
  }

  /**
   * calculateOptimalPumpCfgs.
   * @param outgoing
   * 
   * @param currWL
   * 
   * @param currSR
   * 
   */
  private void calculateOptimalPumpCfg(Mailbox outgoing, double currWL, double currSR) {
   
    double marginOfError;
    double maxSR;
    // When in Waiting mode the element is not on so max steam is 0
    if (mode == State.WAITING || mode == State.READY) {
      maxSR = 0;
      marginOfError = 0.01;
    } else {
      maxSR = configuration.getMaximualSteamRate();
      if (mode == State.RESCUE) {
        marginOfError = 0.10;
      } else {
        marginOfError = 0.03;
      }
    }
    //use class scope var to save predicted water level range
    //these are checked with the actual Water level next cycle
    predictedWaterLevelMax = (currWL - (5 * currSR)); 
    predictedWaterLevelMin = (currWL - (5 * maxSR));
    if (valveOpen) {
      predictedWaterLevelMax -= (configuration.getEvacuationRate() * 5);
      predictedWaterLevelMin -= (configuration.getEvacuationRate() * 5);
    }
    //the mean predicted water level if no pumps are on
    double currPredWL = ((predictedWaterLevelMax + predictedWaterLevelMin) / 2); 
    double optWL = (configuration.getMaximalNormalLevel()
        + configuration.getMinimalNormalLevel()) / 2; 
    //the difference between the prediction and the optimal WaterLevel
    double currWaterLevelDiff = Math.abs(currPredWL - optWL);
    //Add all faulty pumps which are stuck in on position to currWL prediction
    for (int b = 0; b < configuration.getNumberOfPumps(); b++) { 
      if ((getArrayBool(expectedPumpsOn, b) == true)
          && ((getArrayBool(faultyPumps, b) == true))) {
        predictedWaterLevelMax += configuration.getPumpCapacity(b) * 5;
        predictedWaterLevelMin += configuration.getPumpCapacity(b) * 5;
        currPredWL = currPredWL + configuration.getPumpCapacity(b) * 5;
        currWaterLevelDiff = Math.abs(currPredWL - optWL);
      }
    }
    tryAllPumpConfigurations(currPredWL, optWL, currWaterLevelDiff, outgoing);
    predictedWaterLevelMax += predictedWaterLevelMax * marginOfError;
    predictedWaterLevelMin -= predictedWaterLevelMin * marginOfError;
    assert predictedWaterLevelMax >= predictedWaterLevelMin;
  }

  /**
   * tryAllPumpConfigurations.
   * tryAllPumpConfigurations.
   * @param currPredWL
   * 
   * @param optWL
   * 
   * @param currWaterLevelDiff
   * 
   * @param outgoing
   * 
   */
  private void tryAllPumpConfigurations(double currPredWL, double optWL,
      double currWaterLevelDiff, Mailbox outgoing) {
    //iterate through each pump, decide if it should be turned on
    for (int p = 0; p < configuration.getNumberOfPumps(); p++) {
      //calculate the water level if this specific pump was turned on
      double proposedWaterLevel = currPredWL + configuration.getPumpCapacity(p) * 5;
      double proposedWaterLevelDiff = Math.abs(proposedWaterLevel - optWL);
      if ((proposedWaterLevelDiff < currWaterLevelDiff)
          && (getArrayBool(faultyPumps, p) == false)) {
        //if this proposed Water level is an improvement turn it on, if it isnt already
        if (getArrayBool(expectedPumpsOn, p) == false) {
          sendMsg(outgoing, msgOpenPump + p);
          expectedPumpsOn = setArrayBool(expectedPumpsOn, p, true);
        }
        predictedWaterLevelMax += configuration.getPumpCapacity(p) * 5;
        predictedWaterLevelMin += configuration.getPumpCapacity(p) * 5;
        currWaterLevelDiff = proposedWaterLevelDiff;
        currPredWL = proposedWaterLevel;
      } else if (getArrayBool(expectedPumpsOn, p) == true) {
        //if the proposed Water level is worse and the pump is already on, turn it off 
        sendMsg(outgoing, msgClosePump + p);
        expectedPumpsOn = setArrayBool(expectedPumpsOn, p, false);
      }
    }
  }

  /**
   * Check whether there was a transmission failure. This is indicated in
   * several ways. Firstly, when one of the required messages is missing.
   * Secondly, when the values returned in the messages are nonsensical.
   *
   * @param levelMessage
   *          Extracted LEVEL_v message.
   * @param steamMessage
   *          Extracted STEAM_v message.
   * @param pumpStates
   *          Extracted PUMP_STATE_n_b messages.
   * @param pumpControlStates
   *          Extracted PUMP_CONTROL_STATE_n_b messages.
   * 
   */
  private boolean transmissionFailure(@Nullable Message levelMessage,
      @Nullable Message steamMessage, short pumpStates,
      short pumpControlStates) {
    // Check level readings
    if (levelMessage == null) {
      // Nonsense or missing level reading
      return true;
    } else if (steamMessage == null) {
      // Nonsense or missing steam reading
      return true;
    } else if (pumpStates == -1) {
      // Nonsense pump state readings
      return true;
    } else if (pumpControlStates == -1) {
      // Nonsense pump control state readings
      return true;
    }
    // Done
    return false;
  }

  /**
   * Find and extract a message of a given kind in a mailbox. This must the only
   * match in the mailbox, else <code>null</code> is returned.
   *
   * @param kind
   *          The kind of message to look for.
   * @param incoming
   *          The mailbox to search through.
   * @return The matching message, or <code>null</code> if there was not exactly
   *         one match.
   */
  @Nullable
  private static Message extractOnlyMatch(MessageKind kind, Mailbox incoming) {
    Message match = null;
    for (int i = 0; i != incoming.size(); ++i) {
      Message ith = incoming.read(i);
      if (ith.getKind() == kind) {
        if (match == null) {
          match = ith;
        } else {
          // This indicates that we matched more than one message of the given
          // kind.
          return null;
        }
      }
    }
    return match;
  }

  /**
   * extractPumpMsgsToShort.
   * @param kind
   * 
   * @param incoming
   * 
   * @param requiredNum
   * 
   * @param hasBool
   * 
   */
  private short extractPumpMsgsToShort(MessageKind kind, Mailbox incoming,
      int requiredNum, boolean hasBool) {
    int count = 0;
    for (int i = 0; i != incoming.size(); ++i) {
      Message ith = incoming.read(i);
      if (ith.getKind() == kind) {
        count = count + 1;
      }
    }
    if (!(count >= requiredNum && count <= configuration.getNumberOfPumps())) {
      return -1;
    } else {
      short message = 0;
      for (int i = 0; i != incoming.size(); ++i) {
        Message ith = incoming.read(i);
        if (ith.getKind() == kind) {
          if (hasBool) {
            message = setArrayBool(message, ith.getIntegerParameter(),
                ith.getBooleanParameter());
          } else {
            message = setArrayBool(message, ith.getIntegerParameter(), true);
          }
        }
      }
      return message;
    }
  }
}
