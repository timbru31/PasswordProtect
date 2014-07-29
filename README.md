# PasswordProtect [![Build Status](http://ci.dustplanet.de/job/PasswordProtect/badge/icon)](http://ci.dustplanet.de/job/PasswordProtect/)

## Info
This CraftBukkit plugin aims to offer a simple server password which is the same for _all_ users.

**THIS IS NOT A PLUGIN FOR A USER SPECIFIC PASSWORD**

Without logging in the user is jailed in a pre defined cuboid and ported back once he leaves the jail area.
You can define jail areas for each world and configure which actions like block breaking, chatting or flying should be disabled.
There is an additional ability to allow certain commands or to auto-kick or auto-ban a user after X failed attempts.

**Features**
* Cancel different interaction events like
  * Pickup items
  * Drop items
  * Break blocks
  * Hit mobs or players
  * Chat
  * Triggering of mobs
  * Interaction with items
  * Using a portal
  * Drops on death
  * Flying
* Auto kick and auto ban (even the IP) after configurable amount of tries
* Blindness and slowness for the player
* Jail area - the player is teleported back if he leaves the area
* Per world jail area!
* **Safe encryption** of password, choose between **SHA, SHA-256, SHA-512, MD5 & more**
* Custom commands are allowed to execute (like /rules)
* Teleport the player back to the previous location (location on logout)

*Third party features, all of them can be disabled*
* Metrics for usage statistics

## License
This plugin is released under the  
*Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International (CC BY-NC-SA 4.0)* license.  
Please see [LICENSE.md](LICENSE.md) for more information.

## Standard config
````yaml
# For help please refer to http://dev.bukkit.org/bukkit-plugins/passwordprotect/
# Which encryption should be used? Example: MD5 or SHA-256
encryption: SHA-512
# Are ops forced, to enter the password, too?
OpsRequirePassword: true
# Should the password be stored in clean (plain) text?
cleanPassword: false
password: ''
passwordClean: ''
# What events should be prevented?
prevent:
  Movement: true
  Interaction: true
  InteractionMobs: true
  ItemPickup: true
  ItemDrop: true
  Portal: true
  BlockPlace: true
  BlockBreak: true
  # Players won't be triggered by mobs anymore
  Triggering: true
  Attacks: true
  Damage: true
  Chat: true
# After how many attempts should a player be kicked or banned
wrongAttempts:
  kick: 3
  ban: 5
  banIP: true
# Broadcast messages when a player is kicked or banned?
broadcast:
  kick: true
  ban: true
# Make the players slow and add darkness effects?
darkness: true
slowness: true
# These commands are available, even without logging in
allowedCommands:
- help
- rules
- motd
# Teleport back to the location they left?
teleportBack: true
# Show the message that a password is required
loginMessage: true
````

## Commands & Permissions
(Fallback to OPs, if no permissions system is found)

**Please note that _/setjaillocation_ has the following aliases**  
* /setjail
* /setjailarea
* /setpasswordjail

#### General commands
| Command | Permission node | Description |
|:----------:|:----------:|:----------:|
| /login <password> | - | allows you to login |
| /password | passwordprotect.getpassword | Gets the password if not stored encrypted |
| /setpassword <xyz> | passwordprotect.setpassword | Sets the password to xyz |
| /setjailloctation <radius> | passwordprotect.setjailarea | Sets the jail location with the given radius |

#### Special permissions
* passwordprotect.* - Grants access to ALL other permissions (**EXECPT**: passwordprotect.nopassword)
* passwordprotect.nopassword - Bypass the login password

## Credits
* DisabledHamster/brianewing for the original plugin!
* muHum for mPasswordProtector
* You - for using it!

## Support
For support visit the dev.bukkit.org page: http://dev.bukkit.org/bukkit-plugins/password-protect

## Pull Requests
Feel free to submit any PRs here. :)  
Please follow the Sun Coding Guidelines, thanks!

## Usage statistics
[![MCStats](http://mcstats.org/signature/PasswordProtect.png)](http://mcstats.org/plugin/PasswordProtect)

## Data usage collection of Metrics

#### Disabling Metrics
The file ../plugins/Plugin Metrics/config.yml contains an option to *opt-out*

#### The following data is **read** from the server in some way or another
* File Contents of plugins/Plugin Metrics/config.yml (created if not existent)
* Players currently online (not max player count)
* Server version string (the same version string you see in /version)
* Plugin version of the metrics-supported plugin
* Mineshafter status - it does not properly propagate Metrics requests however it is a very simple check and does not read the filesystem

#### The following data is **sent** to http://mcstats.org and can be seen under http://mcstats.org/plugin/PasswordProtect
* Metrics revision of the implementing class
* Server's GUID
* Players currently online (not max player count)
* Server version string (the same version string you see in /version)
* Plugin version of the metrics-supported plugin

## Donation
[![PayPal](https://www.paypalobjects.com/en_US/i/btn/btn_donateCC_LG.gif "Donation via PayPal")](https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=T9TEV7Q88B9M2)

![BitCoin](https://dl.dropboxusercontent.com/u/26476995/bitcoin_logo.png "Donation via BitCoins")  
Address: 1NnrRgdy7CfiYN63vKHiypSi3MSctCP55C
