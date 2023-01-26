package me.cprox.practice.util.external;

import me.cprox.practice.Practice;

public abstract class BaseCommand {
    public Practice plugin = Practice.get();

    public BaseCommand() {
        plugin.getCommandsHandler().registerCommands(this, null);
    }

    public abstract void onCommand(CommandArgs command);
}
