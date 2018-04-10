package ro.license.odbreader.data;

public class ObdResult {

    private String cmdName;
    private String cmdResult;
    private String cmdCalculatedResult;
    private String cmdUnit;
    private int position;

    public ObdResult(String cmdName, String cmdResult, int position){
        this.cmdName = cmdName;
        this.cmdResult = cmdResult;
        this.position = position;
    }

    public String getCmdName() {
        return cmdName;
    }

    public void setCmdName(String cmdName) {
        this.cmdName = cmdName;
    }

    public String getCmdResult() {
        return cmdResult;
    }

    public void setCmdResult(String cmdResult) {
        this.cmdResult = cmdResult;
    }

    public String getCmdCalculatedResult() {
        return cmdCalculatedResult;
    }

    public void setCmdCalculatedResult(String cmdCalculatedResult) {
        this.cmdCalculatedResult = cmdCalculatedResult;
    }

    public String getCmdUnit() {
        return cmdUnit;
    }

    public void setCmdUnit(String cmdUnit) {
        this.cmdUnit = cmdUnit;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }
}
