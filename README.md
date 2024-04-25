# Sendaroo

Sendaroo is a plugin targeting API7 of SpongeAPI

## Dependencies

[Sendaroo depends on Spotlin](https://ore.spongepowered.org/pxlpowered/Spotlin) to provide the kotlin runtime.

## Usage

`/send <player> [amount]`

The amount is optional, if not provided it will consume the whole stack.

### Permissions

There are two permissions for this plugin

`sendaroo.send.base` is the permission to use `/send`

`sendaroo.debug.send.self` is a debug permission that will have unintended side effects if enabled.

It will enable you to send items to yourself, this has the potential to delete items under certain conditions.
**Do not use in production!**

### Config

There are two config options

`debug` which does the same as the above debug permission.

`transactionLogging` This will log transactions to the console

`player1 --[minecraft:dirt]x5-> player2`
