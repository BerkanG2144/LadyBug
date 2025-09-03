package commands;

public interface Command {
    void execute(String[] args) throws Exception;
    String getCommandName();
    String getUsage();
}
