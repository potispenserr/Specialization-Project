import aiinterface.AIInterface;
import aiinterface.CommandCenter;
import struct.CharacterData;
import struct.FrameData;
import struct.GameData;
import struct.Key;

public class pootisMizunoAI implements AIInterface {

    private Key inputKey;
    private boolean playerNumber;
    private FrameData frameData;
    private CommandCenter cc;
    private GameData gameData;

    CharacterData oppData;
    CharacterData myData;



    @Override
    public int initialize(GameData gameData, boolean playerNumber) {
        this.playerNumber = playerNumber;
        this.inputKey = new Key();
        this.gameData = gameData;
        cc = new CommandCenter();
        System.out.println("Shit has started");
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
                //System.out.println("Distance between players are " + frameData.getDistanceX());
                //System.out.println("Player front is " + oppData.isFront());


                if(frameData.getDistanceX() < 100) {
                     System.out.println("Throwing kick");
                     cc.commandCall("B");
                }
                else if(frameData.getDistanceX() > 300){
                    if(myData.getEnergy() > 70) {
                        System.out.println("Throwing fireball");
                        cc.commandCall("STAND_D_DF_FB");
                    }
                    else {
                        cc.commandCall("6 6");

                    }
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

    }
    private boolean canProcessing() {
        return !frameData.getEmptyFlag() && frameData.getRemainingFramesNumber() > 0;
    }
}