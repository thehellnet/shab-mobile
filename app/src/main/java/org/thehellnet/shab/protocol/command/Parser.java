package org.thehellnet.shab.protocol.command;

/**
 * Created by sardylan on 19/07/16.
 */
public final class Parser {

    public static Command parseRawCommand(String rawCommand) {
        switch (rawCommand.split("\\|")[0]) {
            case ClientUpdate.COMMAND:
                return Command.CLIENT_UPDATE;
            case ShabUpdate.COMMAND:
                return Command.SHAB_UPDATE;
            default:
                return Command.NONE;
        }
    }
}
