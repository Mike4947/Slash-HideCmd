package slashHideCmd.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import slashHideCmd.SlashHideCmd;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ShdCommand implements CommandExecutor, TabCompleter {

    private final SlashHideCmd plugin;

    public ShdCommand(SlashHideCmd plugin) {
        this.plugin = plugin;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage("§6=== SlashHideCmd Help ===");
        sender.sendMessage("§e/shd help §7- Show this help");
        sender.sendMessage("§e/shd reload §7- Reload the plugin config (permission: slashhide.reload)");
        sender.sendMessage("§6=========================");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        String sub = args[0].toLowerCase();
        switch (sub) {
            case "help":
                // anyone can view help
                sendHelp(sender);
                return true;

            case "reload":
                if (!sender.hasPermission("slashhide.reload") && !sender.isOp()) {
                    sender.sendMessage("§cYou don't have permission to reload.");
                    return true;
                }

                plugin.reloadConfig();
                sender.sendMessage("§aSlashHideCmd configuration reloaded.");
                return true;

            default:
                sendHelp(sender);
                return true;
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> subs = new ArrayList<>();
            subs.add("help");
            if (sender.hasPermission("slashhide.reload") || sender.isOp()) {
                subs.add("reload");
            }
            String partial = args[0].toLowerCase();
            if (partial.isEmpty()) return subs;
            List<String> out = new ArrayList<>();
            for (String s : subs) {
                if (s.startsWith(partial)) out.add(s);
            }
            return out;
        }
        return Collections.emptyList();
    }
}
