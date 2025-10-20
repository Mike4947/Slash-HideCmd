package slashHideCmd;

import org.bukkit.plugin.java.JavaPlugin;
import slashHideCmd.listeners.CommandVisibilityListener;
import slashHideCmd.commands.ShdCommand;

public final class SlashHideCmd extends JavaPlugin {

    private static SlashHideCmd INSTANCE;

    @Override
    public void onEnable() {
        INSTANCE = this;
        saveDefaultConfig();

        // Register listeners
        getServer().getPluginManager().registerEvents(new CommandVisibilityListener(this), this);

        // Register command /shd
        if (getCommand("shd") != null) {
            ShdCommand shd = new ShdCommand(this);
            getCommand("shd").setExecutor(shd);
            getCommand("shd").setTabCompleter(shd);
        } else {
            getLogger().warning("Command 'shd' not found in plugin.yml");
        }

        getLogger().info("SlashHideCmd enabled.");
    }

    @Override
    public void onDisable() {
        getLogger().info("SlashHideCmd disabled.");
    }

    public static SlashHideCmd getInstance() {
        return INSTANCE;
    }
}
