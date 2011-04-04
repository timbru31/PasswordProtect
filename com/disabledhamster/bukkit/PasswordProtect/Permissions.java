package com.disabledhamster.bukkit.PasswordProtect;

import com.nijiko.permissions.PermissionHandler;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class Permissions {

    private PasswordProtect plugin;
    private static PermissionHandler permissions;

    public Permissions(PasswordProtect plugin) {
        this.plugin = plugin;

        Plugin theYetiPermissions = plugin.getServer().getPluginManager().getPlugin("Permissions");
        if (theYetiPermissions != null) {
            permissions = ((com.nijikokun.bukkit.Permissions.Permissions) theYetiPermissions).getHandler();
        }
    }

    public boolean canSetPassword(Player player) {
        if (permissions != null)
            return permissions.has(player, "passwordprotect.setpassword");
        else
            return player.isOp();
    }

    public boolean needsPassword(Player player) {
        if (permissions != null)
            return !permissions.has(player, "passwordprotect.nopassword");
        else
            return (!player.isOp() || plugin.getRequireOpsPassword());
    }

    public boolean canSetOpsRequirePassword(Player player) {
        return player.isOp();
    }

    public boolean canGetPassword(Player player) {
        if (permissions != null)
            return permissions.has(player, "passwordprotect.getpassword");
        else
            return player.isOp();
    }

    public boolean canSetJail(Player player) {
        if (permissions != null)
            return permissions.has(player, "passwordprotect.setjailarea");
        else
            return player.isOp();
    }

}