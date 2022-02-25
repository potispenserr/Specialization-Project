import aiinterface.AIInterface;
import aiinterface.CommandCenter;
import enumerate.Action;
import enumerate.State;
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

    private double kThreshhold = 0.3;
    private int distThreshold = 3;
    private int kDistance = 50;

    private int attackCounter = 0;

    Deque<Action> predictedOppActs;
    int[] actionFreqArr;
    int[] actsOccuringFrequency;

    CharacterData oppData;
    CharacterData myData;

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
        oppActAA = new ArrayDeque<ActData>();
        oppActAG = new ArrayDeque<ActData>();
        oppActGG = new ArrayDeque<ActData>();
        oppActGA = new ArrayDeque<ActData>();
        actionFreqArr = new int[EnumSet.allOf(Action.class).size()];
        this.predictedOppActs = new LinkedList<Action>();


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
                //System.out.println(inputKey);
            } else {
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
                    decideAction();
                /*else if(oppData.getAttack().getAttackType() == 1){
                    System.out.println("Opponent threw a HIGH attack at relative pos X:" + frameData.getDistanceX() + " pos Y" + frameData.getDistanceY());
                    System.out.println(oppData.getState());
                }
                else if(oppData.getAttack().getAttackType() == 2){
                    System.out.println("Opponent threw a MID attack at relative pos X:" + frameData.getDistanceX() + " pos Y" + frameData.getDistanceY());
                    System.out.println(oppData.getState());
                }
                else if(oppData.getAttack().getAttackType() == 3){
                    System.out.println("Opponent threw a LOW attack at relative pos X:" + frameData.getDistanceX() + " pos Y" + frameData.getDistanceY());
                    System.out.println(oppData.getState());
                }
                else if(oppData.getAttack().getAttackType() == 4){
                    System.out.println("Opponent threw a THROW attack at relative pos X:" + frameData.getDistanceX() + " pos Y" + frameData.getDistanceY());
                    System.out.println(oppData.getState());
                }

                if(myData.getEnergy() > 150){
                    System.out.println("Throwing big boi fireball");
                    cc.commandCall("STAND_D_DF_FC");
                }


                if(frameData.getDistanceX() < 100) {
                     //System.out.println("Throwing kick");
                     //cc.commandCall("B");
                }
                else if(frameData.getDistanceX() > 300){
                    if(myData.getEnergy() > 50) {
                        System.out.println("Throwing fireball");
                        cc.commandCall("STAND_D_DF_FB");
                    }

                }
                else {
                    //cc.commandCall("6");
                    //inputKey = cc.getSkillKey();

                }*/


                    cc.commandCall(action.name());
                    inputKey = cc.getSkillKey();

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

    private void decideAction() {
        int relativeX = frameData.getDistanceX();
        int relativeY = frameData.getDistanceY();

        Deque<ActData> actData = null;
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
                return;
        }
        int count = 0;
        if(actData.size() != 0){
            System.out.println("actdata is of size " + actData.size());
        /*for (int i = 0; i < actData.size(); i++){


        }*/
            boolean isOpponentGoingToAttack = calculateDist(actData, relativeX, relativeY);
            System.out.println("Is Opponent Going To Attack? " + isOpponentGoingToAttack);
            System.out.println("OppActs size: " + predictedOppActs.size());
        }


    }


    /**
     *
     * @return returns 1 if both characters is on the ground, 2 if the player is on the ground and opponent in the air, 3 if player is in the air and opponent on the ground and 4 if both are in the air
     */
    public int checkPosition() {
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
        System.out.println("AI state is " + myData.getState() + " Player state is " + oppData.getState());
        return -1;
    }

    public boolean calculateDist(Deque<ActData> actData, int relativeX, int relativeY) {
        int actdataThreshold = (int)(actData.size() * kThreshhold + 1);
        Deque<ActData> temp = new LinkedList<ActData>();
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

        for (ActData act :
                temp) {
            System.out.println("Action: " + act.getAct() + " X: " + act.getX());
        }



        for (ActData act :
                actData) {
            System.out.println("ACtdata Action: "+ act.getAct() + " actdata X: " + act.getX());

        }

        actArr = arrSort(temp);

        predictActsKNN(actArr, Math.min(actdataThreshold, distThreshold));

        return true;

    }
    private ActData[] arrSort(Deque<ActData> actData){
        //moved this from calculateDist() and it miraculously worked
        ActData[] actArr = new ActData[actData.size()];

        System.out.println("arrSort actarr length " + actArr.length);

        for(int i = 0 ; i < actArr.length ; i ++){
            actArr[i] = new ActData(actData.pop());
        }

        for (ActData act :
                actArr) {
            System.out.println("ACtdata Action: "+ act.getAct() + " actdata X: " + act.getX());

        }

        if(actArr.length > 3){
            insertionSort(actArr);
            for (ActData act :
                    actArr) {
                System.out.println("Action: "+ act.getAct() + " Distance: " + act.getDistance());

            }
        }
        return actArr;
    }



    private void insertionSort(ActData[] actArr){
        for (ActData act :
                actArr) {
            System.out.println("Action: "+ act.getAct() + " Distance: " + act.getDistance());

        }
        for (int j = 1; j < actArr.length; j++) {
            System.out.println("Index j is " + j);
            ActData key = actArr[j];
            System.out.println("key is action " + key.getAct() + " distance " + key.getDistance());
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
            System.out.println("OppActs in predictActsKNN is: " + act.name());

        }



    }
    private boolean canProcessing() {
        return !frameData.getEmptyFlag() && frameData.getRemainingFramesNumber() > 0;
    }
}