import aiinterface.AIInterface;
import aiinterface.CommandCenter;
import enumerate.Action;
import enumerate.State;
import struct.CharacterData;
import struct.FrameData;
import struct.GameData;
import struct.Key;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.EnumSet;
import java.util.LinkedList;

public class pootisMizunoAI implements AIInterface {

    private Key inputKey;
    private boolean playerNumber;
    private FrameData frameData;
    private CommandCenter cc;
    private GameData gameData;
    private int characterStates;

    private double kThreshhold = 0.3;
    private int Threshold = 3;
    private int kDistance = 50;

    Deque<Action> oppActs;
    int checkAct[];

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
        checkAct = new int[EnumSet.allOf(Action.class).size()];
        this.oppActs = new LinkedList<Action>();


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
                    if (oppData.getAttack().getAttackType() > 0) {
                        System.out.println("adding an attack to the memory bank");
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
                oppActGG.add(new ActData(frameData.getDistanceX(), frameData.getDistanceY(), oppData.getAction()));
                break;
            case 2:
                oppActGA.add(new ActData(frameData.getDistanceX(), frameData.getDistanceY(), oppData.getAction()));
                break;

            case 3:
                oppActAG.add(new ActData(frameData.getDistanceX(), frameData.getDistanceY(), oppData.getAction()));
                break;

            case 4:
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
            System.out.println("Is Opponent Going To Attack");

            System.out.println("OppActs size: " + oppActs.size());
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
        if(temp.size() < Math.min(actdataThreshold, Threshold)){
            return false;

        }
        //actArr might be uninitalized
        ActData[] tempArr = new ActData[temp.size()];
        for(int i = 0; i < temp.size(); i++){
            tempArr[i] = temp.pop();
        }

        System.out.println("temoArr is " + tempArr.length);

        actArr = arrSort((ActData[]) tempArr);

        organizeOppActs(actArr, Math.min(actdataThreshold, Threshold));

        return true;

    }
    private ActData[] arrSort(ActData[] actArr){
        mergeSort(actArr);
        return actArr;
    }

    private void mergeSort(ActData[] actArr){

        if(actArr.length > 1){
            int left = actArr.length / 2;
            int right = actArr.length - left;
            ActData[] arrLeft = new ActData[left];
            ActData[] arrRight = new ActData[right];
            for(int i = 0; i < left; i++){
                arrLeft[i] = new ActData(actArr[i]);
            }
            System.out.println("Size of arrRight " + arrRight.length);
            for(int i = 0; i < right; i++) {
                System.out.println("index: " + (left + i - 1));
                arrRight[i] = new ActData(actArr[left + i - 1]);
            }
            mergeSort(arrLeft);
            mergeSort(arrRight);
            merge(arrLeft, arrRight, actArr);

        }
    }

    private void merge(ActData[] arrLeft, ActData[] arrRight, ActData[] actArr){
        int i = 0;
        int j = 0;
        while(i < arrLeft.length || j < arrRight.length){
            if(j >= arrRight.length || (i <arrLeft.length && arrLeft[i].getDistance() < arrRight[j].getDistance())){
                actArr[i + j].setActData(arrLeft[i]);
                i++;
            }
            else {
                actArr[i + j].setActData(arrRight[j]);
                j++;
            }
        }
    }

    private void organizeOppActs(ActData[] actArr, int threshold) {
        Action[] actionArr = Action.values();
        int frequentActionIndex = 1;
        for(int i = 0; i < threshold; i++){
            checkAct[actArr[i].getAct().ordinal()]++;
        }

        for(int i = 0; i < EnumSet.allOf(Action.class).size(); i++){
            if(checkAct[i] > frequentActionIndex){
                oppActs.clear();
                oppActs.add(actionArr[i]);
                frequentActionIndex = checkAct[i];
            }
            else if(checkAct[i] == frequentActionIndex){
                oppActs.add(actionArr[i]);
            }
        }

    }
    private boolean canProcessing() {
        return !frameData.getEmptyFlag() && frameData.getRemainingFramesNumber() > 0;
    }
}