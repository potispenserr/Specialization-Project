import enumerate.Action;

public class ActData {
    private int x;
    private int y;
    private int distance;
    private Action act;

    public ActData(int x, int y, Action action){
        this.x = x;
        this.y = y;
        act = action;
    }

    public ActData(ActData actData){
        x = actData.x;
        y = actData.y;
        distance = actData.distance;
        act = actData.act;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public Action getAct() {
        return act;
    }

    public int getDistance() { return distance;}

    public void setDistance(int dist){
        distance = dist;

    }

    public void setActData(ActData actData){
        x = actData.x;
        y = actData.y;
        distance = actData.distance;
        act = actData.act;
    }

}
