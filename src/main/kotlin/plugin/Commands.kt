package plugin

import org.spongepowered.api.command.CommandResult
import org.spongepowered.api.command.args.CommandElement
import org.spongepowered.api.command.args.GenericArguments
import org.spongepowered.api.command.spec.CommandSpec
import org.spongepowered.api.data.type.HandTypes
import org.spongepowered.api.entity.living.player.Player
import org.spongepowered.api.item.ItemTypes
import org.spongepowered.api.item.inventory.ItemStack
import org.spongepowered.api.item.inventory.transaction.InventoryTransactionResult
import org.spongepowered.api.text.LiteralText
import org.spongepowered.api.text.Text
import org.spongepowered.api.text.format.TextColors
import java.util.*

class Commands {

    private fun err(msg: String): Text {
        return Text.builder()
            .color(TextColors.RED)
            .append(Text.of(msg))
            .build()
    }

    private fun succ(msg: String): Text {
        return Text.builder()
            .color(TextColors.GREEN)
            .append(Text.of(msg))
            .build()
    }

    private val argName: LiteralText = Text.of("name")
    private val argQuantity: LiteralText = Text.of("quantity")
    private val quantityArg: CommandElement =
        GenericArguments.optional(GenericArguments.onlyOne(GenericArguments.integer(argQuantity)))
    private val nameArg: CommandElement = GenericArguments.onlyOne(GenericArguments.player(argName))

    val spec: CommandSpec = CommandSpec.builder()
        .description(Text.of("Sends an item to another user!"))
        .arguments(nameArg, quantityArg)
        .executor { src, args ->
            // Ensure sender is a player
            if (src !is Player) {
                src.sendMessage(err("Must be a player to use this command! Try /give?"))
                return@executor CommandResult.empty()
            }

            // Target player being given an item
            if (!args.hasAny(argName)) {
                src.sendMessage(err("Who do you want to send it to?"))
                return@executor CommandResult.empty()
            }
            val target = args.getOne<Player>(argName).getOrNull()

            if (target == null) {
                src.sendMessage(err("You must use a valid player name!"))
                return@executor CommandResult.empty()
            }
            // Prevent from sending items to self
            if (src == target) {
                src.sendMessage(err("You cannot send items to yourself!"))
                return@executor CommandResult.empty()
            }

            // Check players item in their hand
            val item = src.getItemInHand(HandTypes.MAIN_HAND).getOrNull()?.createSnapshot()
            // Player has no item in their hand
            if (item == null || item == ItemStack.empty() || item == ItemTypes.AIR) {
                src.sendMessage(err("You must have an item in your hand to send something!"))
                return@executor CommandResult.empty()
            }

            if (!args.hasAny(argQuantity)) {
                // Transaction result
                val stack = item.createStack()
                val result = target.inventory.offer(stack)

                if (result.type != InventoryTransactionResult.Type.SUCCESS) {
                    // Potentially something else could've gone wrong but this is lazy
                    src.sendMessage(err("Target players inventory is too full!"))
                    return@executor CommandResult.empty()
                }

                // Remove itemstack from Originating player
                src.setItemInHand(HandTypes.MAIN_HAND, null)
                notifyPlayers(src, target, item.type.name, item.quantity)
            } else {
                val amt = args.getOne<Int>(argQuantity).get()

                if (amt < 1) {
                    src.sendMessage(err("You must send a positive amount of items!"))
                    return@executor CommandResult.empty()
                }

                val currentAmt = item.quantity

                when {
                    currentAmt == amt || currentAmt > amt -> {
                        val stack = item.copy().createStack()
                        stack.quantity = amt
                        target.inventory.offer(stack)

                        val ustack = item.copy().createStack()
                        ustack.quantity -= amt

                        src.setItemInHand(HandTypes.MAIN_HAND, ustack)
                        notifyPlayers(src, target, item.type.name, amt)
                    }
                    else -> {
                        src.sendMessage(Text.of("Not enough items! Try again with the correct amount!"))
                        return@executor CommandResult.empty()
                    }

                }
            }

            CommandResult.success()
        }.build()

    private fun notifyPlayers(p1: Player, p2: Player, name: String, sent: Int) {
        val sentItem: Text = succ("You have sent x$sent of $name to ${p1.name}")
        val receivedItem: Text = succ("You have received x$sent of $name from ${p2.name}")
        p1.sendMessage(sentItem)
        p2.sendMessage(receivedItem)
    }
}

fun <T> Optional<T?>.getOrNull(): T? {
    return orElseGet(null)
}
