# PasswordProtect
[![Build Status](https://ci.dustplanet.de/job/PasswordProtect/badge/icon)](https://ci.dustplanet.de/job/PasswordProtect/)
[![Build Status](https://travis-ci.org/timbru31/PasswordProtect.svg?branch=master)](https://travis-ci.org/timbru31/PasswordProtect)
[![Circle CI](https://img.shields.io/circleci/project/timbru31/PasswordProtect.svg)](https://circleci.com/gh/timbru31/PasswordProtect)
[![Build status](https://ci.appveyor.com/api/projects/status/alqexsstuby24650?svg=true)](https://ci.appveyor.com/project/timbru31/passwordprotect)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/c44d903ca5124d3c8b3278070c0eb704)](https://www.codacy.com/app/timbru31/PasswordProtect?utm_source=github.com&utm_medium=referral&utm_content=timbru31/PasswordProtect&utm_campaign=badger)
[![BukkitDev](https://img.shields.io/badge/BukkitDev-v1.3.1-orange.svg)](https://dev.bukkit.org/projects/passwordprotect)
[![License](https://img.shields.io/badge/License-CC%20BY--NC--SA%204.0-blue.svg)](LICENSE.md)

## Info
This CraftBukkit/Spigot plugin aims to offer a simple server password which is the same for _all_ users.

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
* Jail area - the player is teleported back if he leaves the area - can be turned off with disableJailArea
* Per world jail area!
* **Hashing (one way!)** of password, choose between **SHA, SHA-256, SHA-512, MD5 & more**
* Custom commands are allowed to execute (like /rules)
* Teleport the player back to the previous location (location on logout)

*Third party features, all of them can be disabled*
* bStats for usage statistics

## License
This plugin is released under the
*Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International (CC BY-NC-SA 4.0)* license.
Please see [LICENSE.md](LICENSE.md) for more information.

## Standard config
```yaml
# For help please refer to https://dev.bukkit.org/projects/passwordprotect
# Which hash should be used? Example: SHA-256 or SHA-512
hash: SHA-512
# Are ops forced, to enter the password, too?
opsRequirePassword: true
# Should the jail area be disabled?
disableJailArea: false
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
  DeathDrops: true
  Flying: true
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
```

## Commands & Permissions
(Fallback to OPs, if no permissions system is found)

**Please note that _/setjaillocation_ has the following aliases**
* /setjail
* /setjailarea
* /setpasswordjail

#### General commands
| Command                    | Permission node             | Description                                  |
|:---------------------------|:----------------------------|:---------------------------------------------|
| /login <password>          | -                           | allows you to login                          |
| /password                  | passwordprotect.getpassword | Gets the password if not stored encrypted    |
| /setpassword <xyz>         | passwordprotect.setpassword | Sets the password                            |
| /setjailloctation <radius> | passwordprotect.setjailarea | Sets the jail location with the given radius |

#### Special permissions
* passwordprotect.* - Grants access to ALL other permissions (**EXECPT**: passwordprotect.nopassword)
* passwordprotect.nopassword - Bypass the login password

## Credits
* DisabledHamster/brianewing for the original plugin!
* muHum for [mPasswordProtector](https://github.com/muHum/mPasswordProtector)

## Support
For support visit the dev.bukkit.org page: https://dev.bukkit.org/projects/passwordprotect

## Pull Requests
Feel free to submit any PRs here. :)
Please follow the Sun Coding Guidelines, thanks!

## Usage statistics
_stats images are returning soon!_

## Data usage collection of bStats

#### Disabling bStats
The file `./plugins/bStats/config.yml` contains an option to *opt-out*

#### The following data is **read and sent** to https://bstats.org and can be seen under https://bstats.org/plugin/bukkit/PasswordProtect
* Your server's randomly generated UUID
* The amount of players on your server
* The online mode of your server
* The bukkit version of your server
* The java version of your system (e.g. Java 8)
* The name of your OS (e.g. Windows)
* The version of your OS
* The architecture of your OS (e.g. amd64)
* The system cores of your OS (e.g. 8)
* bStats-supported plugins
* Plugin version of bStats-supported plugins

## Donation
[![PayPal](https://www.paypalobjects.com/en_US/i/btn/btn_donateCC_LG.gif "Donation via PayPal")](https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=T9TEV7Q88B9M2)

![BitCoin](https://dustplanet.de/wp-content/uploads/2015/01/bitcoin-logo-plain.png "Donation via BitCoins")  
1NnrRgdy7CfiYN63vKHiypSi3MSctCP55C
