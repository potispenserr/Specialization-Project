import aiinterface.AIInterface;
import aiinterface.CommandCenter;
import enumerate.Action;
import enumerate.State;
import simulator.Simulator;
import struct.CharacterData;
import struct.FrameData;
import struct.GameData;
import struct.Key;

import java.util.*;

public class pootisMizunoAI implements AIInterface {

    private Key inputKey;
    private boolean playerNumber;
    private FrameData frameData;
    private CommandCenter cc;
    private GameData gameData;
    private int characterStates;
    private Simulator simulator;

    private final double kThreshhold = 0.3;
    private final int distThreshold = 2;
    private final int kDistance = 100;

    private boolean isAirAttacking = false;
    private Action airAttackAction;
    private boolean hasJumped = false;
    private int airAttackFramesCount = 0;

    private Deque<Action> predictedOppActs;
    private int[] actionFreqArr;

    private CharacterData oppData;
    private CharacterData myData;

    private Deque<Action> groundActions;
    private Deque<Action> airActions;

    private Deque<ActData> oppActAA;
    private Deque<ActData> oppActAG;
    private Deque<ActData> oppActGG;
    private Deque<ActData> oppActGA;



    @Override
    public int initialize(GameData gameData, boolean playerNumber) {
        this.playerNumber = playerNumber;
        this.inputKey = new Key();
        this.gameData = gameData;
        cc = new CommandCenter();
        oppActAA = new ArrayDeque<>();
        oppActAG = new ArrayDeque<>();
        oppActGG = new ArrayDeque<>();
        oppActGA = new ArrayDeque<>();
        actionFreqArr = new int[EnumSet.allOf(Action.class).size()];
        this.predictedOppActs = new LinkedList<>();
        this.simulator = gameData.getSimulator();
        setCounterGroundAirActions();


        return 0;
    }

    @Override
    public void getInformation(FrameData fd, boolean player) {
        frameData = fd;
        cc.setFrameData(frameData, playerNumber);
        oppData = frameData.getCharacter(true);
        myData = frameData.getCharacter(false);



    }

    @Override
    public void processing() {
        if(canProcessing()) {
            if (cc.getSkillFlag()) {
                inputKey = cc.getSkillKey();
            }
            else {
                if (myData.isControl() || myData.getRemainingFrame() <= 0) {
                    characterStates = checkPosition();
                    Action action = Action.CROUCH_GUARD;



                    //cc.commandCall("B");
                    //System.out.println(oppData.getAction().toString());
                    /*if (oppData.getAttack().getAttackType() > 0) {
                        if(attackCounter == 0){
                            System.out.println("adding an attack to the memory bank");
                            collectData();
                            attackCounter++;
                        }
                        else{
                            if(attackCounter < 3){
                                attackCounter++;
                                System.out.println("skipping duplicate attack");
                            }
                            else if(attackCounter == 3){
                                System.out.println("skipping final duplicate and restting counter");
                                attackCounter = 0;

                            }
                        }

                    }*/
                    if (oppData.getAttack().getAttackType() > 0) {
                        collectData();
                    }
                    int OGaiHP = myData.getHp();
                    int OGoppHP = oppData.getHp();
                    decideAction();
                    /*
                    if(isAirAttacking == false){
                        if(frameData.getDistanceX() < 165){

                            Deque<Action> actionsToSimulate = (myData.getState() == State.STAND || myData.getState() == State.CROUCH) ? groundActions : airActions;
                            //System.out.println("Distance between players: " + frameData.getDistanceX());
                            System.out.println("AI STATE IS " + myData.getState());
                            int bestScore = 0;
                            Action bestAction = Action.CROUCH_GUARD;
                            Deque<Action> mySimActs = new LinkedList<>();
                            Deque<Action> oppSimActs = new LinkedList<>();

                            for (Action predictedAction : predictedOppActs) {
                                if(predictedAction.name().matches("(.*)AIR(.*)")){
                                    oppSimActs.add(Action.JUMP);
                                }
                                System.out.println("Simulating best attack against " + predictedAction.name());
                                for (Action act : actionsToSimulate) {
                                    if(checkEnergyForAttack(act) == false){
                                        System.out.println("AI doesn't have enough energy to throw " + act.name());
                                        continue;
                                    }
                                    mySimActs.clear();
                                    if(act.name().matches("(.*)AIR(.*)")){
                                        System.out.println("Adding jump");
                                        mySimActs.add(Action.JUMP);
                                    }

                                    mySimActs.add(act);
                                    if(act == Action.STAND_GUARD){
                                        System.out.println("Adding stand guard recov");
                                        mySimActs.add(Action.STAND_GUARD_RECOV);
                                    }
                                    else if (act == Action.CROUCH_GUARD){
                                        System.out.println("Adding crouch guard recov");
                                        mySimActs.add(Action.CROUCH_GUARD_RECOV);
                                    }
                                    FrameData fd = simulator.simulate(frameData, playerNumber, mySimActs, oppSimActs, 60);
                                    CharacterData mySimData = fd.getCharacter(playerNumber);
                                    CharacterData oppSimData = fd.getCharacter(!playerNumber);
                                    int tempScore = (mySimData.getHp() - OGaiHP) - (oppSimData.getHp() - OGoppHP);
                                    System.out.println(act.name() + " got a score of " + tempScore);

                                    if(tempScore > bestScore){
                                        bestAction = act;
                                        bestScore = tempScore;
                                    }
                                }
                            }
                            action = bestAction;
                            if(bestAction.name().matches("(.*)AIR(.*)")){
                                isAirAttacking = true;
                                airAttackAction = bestAction;
                            }
                            System.out.println(bestAction.name() + " is the best counter action with a score of " + bestScore);
                        }
                        //if AI is way to far from the opponent
                        else{
                            if(myData.getEnergy() >= 35){
                                action = Action.STAND_D_DF_FB;
                            }
                            else if(myData.getEnergy() >= 5){
                                action = Action.STAND_D_DF_FA;
                            }

                        }
                        if(isAirAttacking == false){
                            cc.commandCall(action.name());

                        }
                    }
                    // FIXME: 2022-03-07 Delay in air attacks
                    else{
                        if(Math.abs(airAttackFramesCount - frameData.getFramesNumber()) > 10){
                            airAttackFramesCount = 0;
                            cc.commandCall(airAttackAction.name());
                            System.out.println("attacking " + airAttackAction.name());
                            isAirAttacking = false;

                        }
                        else if(airAttackFramesCount == 0){
                            cc.commandCall("JUMP");
                            System.out.println("jumping");
                        }

                    }*/

                    int bestScore = 0;
                    Action bestAction = Action.CROUCH_GUARD;
                    if(isAirAttacking == false){
                        Deque<Action> actionsToSimulate = (myData.getState() == State.STAND || myData.getState() == State.CROUCH) ? groundActions : airActions;
                        if(frameData.getDistanceX() < 165){
                            Deque<Action> mySimActs = new LinkedList<>();
                            Deque<Action> oppSimActs = new LinkedList<>();
                            for (Action predictedAction :
                                    predictedOppActs) {
                                oppSimActs.clear();
                                if(predictedAction.name().matches("(.*)AIR(.*)")){
                                    oppSimActs.add(Action.JUMP);
                                }
                                oppSimActs.add(predictedAction);
                                System.out.println("Simulating best attack against " + predictedAction.name());
                                for (Action simAct :
                                        actionsToSimulate) {
                                    System.out.println("Testing counter attack " + simAct.name());
                                    if(checkEnergyForAttack(simAct) == false){
                                        System.out.println("AI doesn't have enough energy to throw " + simAct.name());
                                        continue;
                                    }
                                    System.out.println("AI has energy to throw " + simAct.name());
                                    mySimActs.clear();
                                    //System.out.println("Does " + act.name() + " match a air move? " +  act.name().matches("(.*)AIR(.*)"));
                                    if(simAct.name().matches("(.*)AIR(.*)")){
                                        //System.out.println("Adding jump");
                                        mySimActs.add(Action.JUMP);
                                    }

                                    mySimActs.add(simAct);
                                    if(simAct == Action.STAND_GUARD){
                                        //System.out.println("Adding stand guard recov");
                                        mySimActs.add(Action.STAND_GUARD_RECOV);
                                    }
                                    else if (simAct == Action.CROUCH_GUARD){
                                        //System.out.println("Adding crouch guard recov");
                                        mySimActs.add(Action.CROUCH_GUARD_RECOV);
                                    }
                                    //System.out.println("DOuble check AI: " + mySimActs.peekFirst() + " " + mySimActs.peekLast());
                                    //System.out.println("DOuble check OPp: " + oppSimActs.peekFirst() + " " + oppSimActs.peekLast());
                                    FrameData fd = simulator.simulate(frameData, playerNumber, mySimActs, oppSimActs, 60);
                                    CharacterData mySimData = fd.getCharacter(playerNumber);
                                    CharacterData oppSimData = fd.getCharacter(!playerNumber);
                                    //System.out.println("After simulation AI HP:" + mySimData.getHp() + " opp HP: " + oppSimData.getHp());
                                    int tempScore = (mySimData.getHp() - OGaiHP) - (oppSimData.getHp() - OGoppHP);
                                    System.out.println(simAct.name() + " got a score of " + tempScore);
                                    if(tempScore > bestScore){
                                        bestAction = simAct;
                                        bestScore = tempScore;
                                    }

                                }

                            }
                            if(bestAction.name().matches("(.*)AIR(.*)")){
                                isAirAttacking = true;
                                airAttackAction = bestAction;
                                System.out.println("it is a air attack");
                            }
                            else{
                                System.out.println("Attacking withc attack " + bestAction.name());
                                cc.commandCall(bestAction.name());
                            }
                            System.out.println(bestAction.name() + " IS THE BEST COUNTER ACTION TO " + predictedOppActs.peek() +   " WITH A SCORE OF " + bestScore);

                        }
                        //if AI is way to far from the opponent
                        else{
                            /*if(myData.getEnergy() >= 35){
                                action = Action.STAND_D_DF_FB;
                            }
                            else if(myData.getEnergy() >= 5){
                                action = Action.STAND_D_DF_FA;
                            }*/
                            if(myData.getEnergy() >= 150){
                                cc.commandCall("STAND_D_DF_FC");
                            }

                        }

                    }
                    else{
                        System.out.println("Airattack Frames inside else: " + airAttackFramesCount);
                        if (airAttackFramesCount == 0){
                            airAttackFramesCount = frameData.getFramesNumber();
                            cc.commandCall("JUMP");
                            System.out.println("jumping");

                        }
                        else if(Math.abs(airAttackFramesCount - frameData.getFramesNumber()) > 42){
                            cc.commandCall(airAttackAction.name());
                            System.out.println("attacking " + airAttackAction.name());
                            isAirAttacking = false;
                            airAttackFramesCount = 0;
                        }

                    }

                    //cc.commandCall(action.name());
                    //inputKey = cc.getSkillKey();
                }
            }
        }
    }

    @Override
    public Key input() {
        return inputKey;
    }

    @Override
    public void close() {

    }

    @Override
    public void roundEnd(int p1HP, int p2HP, int frames) {
        int numOfAttacks = oppActGG.size() + oppActAA.size() + oppActAG.size() + oppActGA.size();
        System.out.println("Opponent threw " + numOfAttacks + " attacks this round");
        for (ActData act :
                oppActGG) {
            System.out.println(act.getAct());
        }

    }

    private void collectData() {

        System.out.println("State of players " + characterStates);
        switch(characterStates) {
            case 1:
                System.out.println("Saving " + oppData.getAction() + " at X: " + frameData.getDistanceX() + " Y: " + frameData.getDistanceY() + " in oppActGG");
                oppActGG.add(new ActData(frameData.getDistanceX(), frameData.getDistanceY(), oppData.getAction()));
                System.out.println("Size of oppActGG " + oppActGG.size());
                break;
            case 2:
                System.out.println("Saving " + oppData.getAction() + " at X: " + frameData.getDistanceX() + " Y: " + frameData.getDistanceY() + " in oppActGA");
                oppActGA.add(new ActData(frameData.getDistanceX(), frameData.getDistanceY(), oppData.getAction()));
                break;

            case 3:
                System.out.println("Saving " + oppData.getAction() + " at X: " + frameData.getDistanceX() + " Y: " + frameData.getDistanceY() + " in oppActAG");
                oppActAG.add(new ActData(frameData.getDistanceX(), frameData.getDistanceY(), oppData.getAction()));
                break;

            case 4:
                System.out.println("Saving " + oppData.getAction() + " at X: " + frameData.getDistanceX() + " Y: " + frameData.getDistanceY() + " in oppActAA");
                oppActAA.add(new ActData(frameData.getDistanceX(), frameData.getDistanceY(), oppData.getAction()));
                break;
        }


    }

    private boolean decideAction() {
        int relativeX = frameData.getDistanceX();
        int relativeY = frameData.getDistanceY();

        Deque<ActData> actData;
        switch(characterStates) {
            case 1:
                actData = oppActGG;
                break;
            case 2:
                actData = oppActGA;
                break;

            case 3:
                actData = oppActAG;
                break;

            case 4:
                actData = oppActAA;
                break;
            default:
                System.out.println("UNEXPECTED VALUE " + characterStates);
                return false;
        }

        boolean isOpponentGoingToAttack = calculateDist(actData, relativeX, relativeY);
        return isOpponentGoingToAttack;
    }


    /**
     *
     * @return returns 1 if both characters is on the ground, 2 if the player is on the ground and opponent in the air, 3 if player is in the air and opponent on the ground and 4 if both are in the air
     */
    private int checkPosition() {
        if(myData.getState().equals(State.STAND) || myData.getState().equals(State.STAND) && oppData.getState().equals(State.STAND) || oppData.getState().equals(State.STAND)){
            return 1;
        }

        if(myData.getState().equals(State.STAND) || myData.getState().equals(State.CROUCH) && oppData.getState().equals(State.AIR)){
            return 2;
        }

        if(myData.getState().equals(State.AIR) && oppData.getState().equals(State.STAND)){
            return 3;
        }

        if(myData.getState().equals(State.AIR) && oppData.getState().equals(State.AIR)){
            return 4;
        }
        return -1;
    }

    private boolean calculateDist(Deque<ActData> actData, int relativeX, int relativeY) {
        int actdataThreshold = (int)(actData.size() * kThreshhold + 1);
        Deque<ActData> temp = new LinkedList<>();
        ActData[] actArr;
        for (ActData actDataI :
                actData) {
            ActData act = new ActData(actDataI);
            if (myData.isFront()) {
                act.setDistance((int) Math.sqrt(Math.pow((act.getX() + relativeX), 2) + Math.pow((act.getY() + relativeY), 2)));
            }
            else{
                act.setDistance((int) Math.sqrt(Math.pow((act.getX() - relativeX), 2) + Math.pow((act.getY() - relativeY), 2)));
            }
            if(act.getDistance() < kDistance){
                temp.add(act);
            }


        }
        if(temp.size() < Math.min(actdataThreshold, distThreshold)){
            return false;

        }


        actArr = arrSort(temp);

        predictActsKNN(actArr, Math.min(actdataThreshold, distThreshold));

        return true;
    }
    private ActData[] arrSort(Deque<ActData> actData){
        //moved this from calculateDist() and it miraculously worked
        ActData[] actArr = new ActData[actData.size()];

        //System.out.println("arrSort actarr length " + actArr.length);

        for(int i = 0 ; i < actArr.length ; i ++){
            actArr[i] = new ActData(actData.pop());
        }

        /*for (ActData act :
                actArr) {
            System.out.println("ACtdata Action: "+ act.getAct() + " actdata X: " + act.getX());

        }*/

        if(actArr.length > 3){
            insertionSort(actArr);
            /*for (ActData act :
                    actArr) {
                System.out.println("Action: "+ act.getAct() + " Distance: " + act.getDistance());

            }*/
        }
        return actArr;
    }



    private void insertionSort(ActData[] actArr){
        /*for (ActData act :
                actArr) {
            System.out.println("Action: "+ act.getAct() + " Distance: " + act.getDistance());

        }*/
        for (int j = 1; j < actArr.length; j++) {
            //System.out.println("Index j is " + j);
            ActData key = actArr[j];
            //System.out.println("key is action " + key.getAct() + " distance " + key.getDistance());
            int i = j-1;
            while ( (i > -1) && ( actArr[i].getDistance() > key.getDistance() ) ) {
                actArr[i+1] = actArr[i];
                i--;
            }
            actArr[i+1] = key;
        }
    }

    private void predictActsKNN(ActData[] actArr, int threshold) {
        Action[] actionEnum = Action.values();
        int maxActionFrequency = 1;

        //sets the frequency of acts to the values in checkAct[] indexed by Action ordinal
        for(int i = 0; i < threshold; i++){
            actionFreqArr[actArr[i].getAct().ordinal()]++;
        }

        //loops through all actions and adds the actions with the most frequent actions to predictedOppActs deque
        for(int i = 0; i < Action.values().length; i++){
            if(actionFreqArr[i] > maxActionFrequency){
                predictedOppActs.clear();
                predictedOppActs.add(actionEnum[i]);
                maxActionFrequency = actionFreqArr[i];
            }
            else if(actionFreqArr[i] == maxActionFrequency){
                predictedOppActs.add(actionEnum[i]);
            }
        }

        for (Action act :
                predictedOppActs) {

        }



    }


    private void setCounterGroundAirActions() {
        groundActions = new LinkedList<>();
        airActions = new LinkedList<>();
        groundActions.add(Action.STAND_GUARD);
        groundActions.add(Action.CROUCH_GUARD);
        groundActions.add(Action.JUMP);
        groundActions.add(Action.FOR_JUMP);
        groundActions.add(Action.BACK_JUMP);
        groundActions.add(Action.BACK_STEP);
        groundActions.add(Action.DASH);
        groundActions.add(Action.THROW_A);
        groundActions.add(Action.THROW_B);
        groundActions.add(Action.STAND_A);
        groundActions.add(Action.CROUCH_A);
        groundActions.add(Action.STAND_FA);
        groundActions.add(Action.CROUCH_FA);
        groundActions.add(Action.STAND_D_DF_FA);
        groundActions.add(Action.STAND_D_DF_FB);
        groundActions.add(Action.STAND_F_D_DFA);
        groundActions.add(Action.STAND_F_D_DFB);
        groundActions.add(Action.STAND_D_DB_BA);
        groundActions.add(Action.STAND_D_DB_BB);
        groundActions.add(Action.STAND_D_DF_FC);
        groundActions.add(Action.AIR_A);
        groundActions.add(Action.AIR_B);
        groundActions.add(Action.AIR_DA);
        groundActions.add(Action.AIR_DB);
        groundActions.add(Action.AIR_UB);
        groundActions.add(Action.AIR_FA);
        groundActions.add(Action.AIR_FB);
        groundActions.add(Action.AIR_UA);

        airActions.add(Action.AIR_GUARD);
        airActions.add(Action.AIR_A);
        airActions.add(Action.AIR_DA);
        airActions.add(Action.AIR_FA);
        airActions.add(Action.AIR_UA);
        airActions.add(Action.AIR_D_DF_FA);
        airActions.add(Action.AIR_F_D_DFA);
        airActions.add(Action.AIR_D_DB_BA);


    }
    private boolean checkEnergyForAttack(Action act){
        switch (act){
            case STAND_D_DF_FA:
            case THROW_A:
                if (myData.getEnergy() > 5){
                    return true;
                }
                return false;
            case THROW_B:
                if (myData.getEnergy() > 10){
                    return true;
                }
                return false;
            case STAND_D_DF_FB:
                if (myData.getEnergy() > 30){
                    return true;
                }
                return false;

            case STAND_D_DB_BB:
                if (myData.getEnergy() > 50){
                    return true;
                }
                return false;
            case STAND_F_D_DFB:
                if (myData.getEnergy() > 55){
                    return true;
                }
                return false;
            case STAND_D_DF_FC:
                if (myData.getEnergy() > 150){
                    return true;
                }
                return false;
        }
        return true;
    }
    private boolean canProcessing() {
        return !frameData.getEmptyFlag() && frameData.getRemainingFramesNumber() > 0;
    }
}