import aiinterface.AIInterface;
import aiinterface.CommandCenter;
import enumerate.State;
import struct.CharacterData;
import struct.FrameData;
import struct.GameData;
import struct.Key;

import java.util.ArrayDeque;
import java.util.Deque;

import static enumerate.Action.STAND;

public class pootisMizunoAI implements AIInterface {

    private Key inputKey;
    private boolean playerNumber;
    private FrameData frameData;
    private CommandCenter cc;
    private GameData gameData;

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
        oppActAG = new ArrayDeque<ActData>();;
        oppActGG = new ArrayDeque<ActData>();;
        oppActGA = new ArrayDeque<ActData>();;


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
            if(cc.getSkillFlag()) {
                inputKey = cc.getSkillKey();
                //System.out.println(inputKey);
            }
            else {
                inputKey.empty();
                cc.skillCancel();

                //cc.commandCall("B");
                //System.out.println(oppData.getAction().toString());
                if(oppData.getAttack().getAttackType() > 0){
                    System.out.println("adding an attack to the memory bank");
                    collectData();

                }
                else if(oppData.getAttack().getAttackType() == 1){
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

    }

    public void collectData() {
        int characterStates = checkPosition();
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


    /**
     *
     * @return returns 1 if both characters is on the ground, 2 if the player is on the ground and opponent in the air, 3 if player is in the air and opponent on the ground and 4 if both are in the air
     */
    public int checkPosition() {
        System.out.println(myData.getState().equals(State.STAND));
        if(myData.getState().equals(State.STAND) && oppData.getState().equals(State.STAND)){
            return 1;
        }

        if(myData.getState().equals(State.STAND) && oppData.getState().equals(State.AIR)){
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

    public void add() {

    }
    private boolean canProcessing() {
        return !frameData.getEmptyFlag() && frameData.getRemainingFramesNumber() > 0;
    }
}