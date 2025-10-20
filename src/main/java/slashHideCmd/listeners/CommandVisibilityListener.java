package slashHideCmd.listeners;

import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.TabCompleteEvent;
import org.bukkit.event.player.PlayerCommandSendEvent;
import slashHideCmd.SlashHideCmd;

import java.util.*;
import java.util.stream.Collectors;

public class CommandVisibilityListener implements Listener {

    private final SlashHideCmd plugin;

    public CommandVisibilityListener(SlashHideCmd plugin) {
        this.plugin = plugin;
    }

    private boolean isEnabled() {
        return plugin.getConfig().getBoolean("enabled", true);
    }

    private boolean opsSeeAll() {
        return plugin.getConfig().getBoolean("ops-see-all", true);
    }

    private boolean hideWhenNoPermissionNode() {
        return plugin.getConfig().getBoolean("hide-when-no-permission-node", false);
    }

    private Set<String> whitelist() {
        return plugin.getConfig().getStringList("whitelist-commands").stream()
                .map(String::toLowerCase).collect(Collectors.toSet());
    }

    private Set<String> blacklist() {
        return plugin.getConfig().getStringList("blacklist-commands").stream()
                .map(String::toLowerCase).collect(Collectors.toSet());
    }

    private boolean canSeeCommand(Player player, String label) {
        if (label == null || label.isEmpty()) return false;
        String cmd = label.toLowerCase();

        if (whitelist().contains(cmd)) return true;
        if (blacklist().contains(cmd)) return false;

        if (player.isOp() && opsSeeAll()) return true;

        // Try to resolve registered PluginCommand (works for most plugins)
        PluginCommand pCmd = plugin.getServer().getPluginCommand(cmd);
        if (pCmd == null) {
            // Unknown / subcommand / not registered in pluginCommand map
            return !hideWhenNoPermissionNode();
        }

        String perm = pCmd.getPermission();
        if (perm == null || perm.trim().isEmpty()) {
            return !hideWhenNoPermissionNode();
        }

        return player.hasPermission(perm);
    }

    @EventHandler
    public void onPlayerCommandSend(PlayerCommandSendEvent event) {
        if (!isEnabled()) return;

        Player player = event.getPlayer();
        Set<String> current = new HashSet<>(event.getCommands());
        boolean changed = false;

        Iterator<String> it = current.iterator();
        while (it.hasNext()) {
            String root = it.next();
            if (!canSeeCommand(player, root)) {
                it.remove();
                changed = true;
            }
        }

        if (changed) {
            event.getCommands().clear();
            event.getCommands().addAll(current);
        }
    }

    @EventHandler
    public void onTabComplete(TabCompleteEvent event) {
        if (!isEnabled()) return;
        if (!(event.getSender() instanceof Player)) return;

        Player player = (Player) event.getSender();
        List<String> completions = event.getCompletions();
        if (completions == null || completions.isEmpty()) return;

        List<String> filtered = new ArrayList<>();
        for (String s : completions) {
            String suggestion = s.startsWith("/") ? s.substring(1) : s;
            int idx = suggestion.indexOf(' ');
            if (idx != -1) suggestion = suggestion.substring(0, idx);
            // also handle namespaced suggestions like "worldedit:pos1"
            int colon = suggestion.indexOf(':');
            if (colon != -1) suggestion = suggestion.substring(colon + 1);

            if (canSeeCommand(player, suggestion)) {
                filtered.add(s);
            }
        }

        if (filtered.size() != completions.size()) {
            completions.clear();
            completions.addAll(filtered);
        }
    }
}
