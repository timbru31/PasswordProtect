ORIGINAL README
------------------------------------------------------------------------------
What is PasswordProtect?
------------------------

PasswordProtect is a plugin for Bukkit, a new Minecraft server mod.

When enabled, users that join your server have to provide a password
before they can move around, build stuff, use items, commands, etc.

Ops can change the password, spawn jail area with the /setpassword and /setpasswordjail commands

------------------------------------------------------------------------------

This is the README of PasswordProtect!
For support visit the old forum thread: http://bit.ly/ppbukkit
or the new dev.bukkit.org page: http://bit.ly/ppbukkitdev
Thanks to DisabledHamster/brianewing for the original plugin!
Thanks to muHum for mPasswordProtector
Thanks for using!

This plugin sends usage statistics! If you wish to disable the usage stats, look at plugins/PluginMetrics/config.yml!
This plugin is released under the Creative Commons Attribution-NonCommercial-ShareAlike 3.0 Unported (CC BY-NC-SA 3.0) license.


Standard config:
# For help please refer to http://bit.ly/ppbukkit or http://bit.ly/ppbukkitdev
encryption: SHA-256
OpsRequirePassword: true
cleanPassword: false
password: ''
passwordClean: ''
prevent:
  Movement: true
  Interaction: true
  InteractionMobs: true
  ItemPickup: true
  ItemDrops: true
  Portal: true
  BlockPlace: true
  BlockBreak: true
  Triggering: true
  Attacks: true
  Damage: true
  Chat: true
wrongAttempts:
  kick: 3
  ban: 5
  banIP: true
darkness: true
slowness: true
allowedCommands:
- help
- rules
- motd
teleportBack: true
loginMessage: true

Commands & Permissions (if no permissions system is detected, only OPs are able to use the commands!)
Only bukkit's permissions system is supported!

Node: passwordprotect.nopassword
Description: Bypass the login password

/password
Node: passwordprotect.getpassword
Description: Allows you to get the password

/setpassword <password>
Node: passwordprotect.setpassword
Description: Allows you to set the password

/setjaillocation <radius>
Aliases: /setjail, /setjailarea, /setpasswordjail
Node: passwordprotect.setjailarea
Description: Allows you to set the jail area

/login <password>
Node: No permission node
Description: Used to login.

Of course you can use passwordprotect.*
But passwordprotect.nopassword is excluded!