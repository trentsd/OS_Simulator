public class ProcessControlBlock {
    private State state;
    private String name;
    private int cyclesRequired;
    private int cyclesRemaining;
    private int pid;
    private PageTable ptbr;

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getCyclesRequired() {
        return cyclesRequired;
    }

    public void setCyclesRequired(int cyclesRequired) {
        this.cyclesRequired = cyclesRequired;
    }

    public int getCyclesRemaining() {
        return cyclesRemaining;
    }

    public void setCyclesRemaining(int cyclesRemaining) {
        this.cyclesRemaining = cyclesRemaining;
    }

    public int getPid() {
        return pid;
    }

    public void setPid(int pid) {
        this.pid = pid;
    }

    public PageTable getPtbr() {
        return ptbr;
    }

    public void setPtbr(PageTable ptbr) {
        this.ptbr = ptbr;
    }
}
