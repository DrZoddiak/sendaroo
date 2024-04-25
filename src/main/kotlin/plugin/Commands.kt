package plugin

import org.slf4j.Logger
import org.spongepowered.api.command.CommandResult
import org.spongepowered.api.command.args.CommandContext
import org.spongepowered.api.command.args.CommandElement
import org.spongepowered.api.command.args.GenericArguments
import org.spongepowered.api.command.spec.CommandSpec
import org.spongepowered.api.data.type.HandTypes
import org.spongepowered.api.entity.living.player.Player
import org.spongepowered.api.item.ItemTypes
import org.spongepowered.api.item.inventory.ItemStack
import org.spongepowered.api.item.inventory.ItemStackSnapshot
import org.spongepowered.api.item.inventory.transaction.InventoryTransactionResult
import org.spongepowered.api.text.LiteralText
import org.spongepowered.api.text.Text
import org.spongepowered.api.text.format.TextColors
import java.util.*

class Commands(private val logger: Logger, private val config: SendarooConfig) {

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

    // Command arguments
    private val nameKey: LiteralText = Text.of("name")
    private val quantityKey: LiteralText = Text.of("quantity")

    // Command elements
    private val quantityArg: CommandElement =
        GenericArguments.optional(GenericArguments.onlyOne(GenericArguments.integer(quantityKey)))
    private val nameArg: CommandElement = GenericArguments.onlyOne(GenericArguments.player(nameKey))

    // The command to register
    val send: CommandSpec = CommandSpec.builder()
        .description(Text.of("Sends an item to another user!"))
        .arguments(nameArg, quantityArg)
        .permission("sendaroo.send.base")
        .executor { src, args ->
            // Ensure sender is a player
            if (src !is Player) {
                src.sendMessage(err("Must be a player to use this command! Try /give?"))
                return@executor CommandResult.empty()
            }

            // The target player to send items to
            val target: Player = args.getOne<Player>(nameKey).getOrNull().let {
                if (it == null) {
                    src.sendMessage(err("You must use a valid player name!"))
                    return@executor CommandResult.empty()
                }
                it
            }

            if (!config.debug || !src.hasPermission("sendaroo.debug.send.self")) {
                // Prevent from sending items to self
                // The code doesn't make special consideration for this case
                // If you send items to yourself there is a chance of deleting items
                // under certain conditions.
                if (src == target) {
                    src.sendMessage(err("You cannot send items to yourself!"))
                    return@executor CommandResult.empty()
                }
            }

            // Check players item in their hand
            val item: ItemStackSnapshot = src.mainHand().let {
                if (it.isNothing()) {
                    src.sendMessage(err("You must have an item in your hand to send something!"))
                    return@executor CommandResult.empty()
                }
                // #isNothing is a null check
                it!!
            }

            // The amount to send, this is either the whole stack
            // or the arg from the command if provided.
            val amt = quantityKey.getOneOrDefault(args) { item.quantity }.let {
                if (it < 1) {
                    src.sendMessage(err("You must send a positive amount of items!"))
                    return@executor CommandResult.empty()
                }
                it
            }

            // This logs the transaction from the command
            logTransaction(src, target, item, amt)

            return@executor when {
                item.quantity >= amt -> {
                    target.inventory.offer(item.eq(amt)).isSuccessful({
                        src.setItemInHand(HandTypes.MAIN_HAND, item.minus(amt))
                        notifyPlayers(src, target, item.type.name, amt)
                        CommandResult.success()
                    }) {
                        src.sendMessage(err("Target players inventory is too full!"))
                        CommandResult.empty()
                    }
                }

                else -> {
                    src.sendMessage(err("Not enough items! Try again with the correct amount!"))
                    CommandResult.empty()
                }
            }

        }.build()

    private fun <T> LiteralText.getOneOrDefault(args: CommandContext, default: () -> T): T {
        return args.getOne<T>(this).getOrNull() ?: default()
    }

    private fun InventoryTransactionResult.isSuccessful(
        succ: () -> CommandResult,
        err: () -> CommandResult
    ): CommandResult {
        return when (this.type) {
            InventoryTransactionResult.Type.SUCCESS -> {
                succ()
            }

            InventoryTransactionResult.Type.CANCELLED -> {
                logger.info("Another plugin or mod cancelled this transaction!")
                err()
            }

            InventoryTransactionResult.Type.FAILURE -> {
                err()
            }

            else -> {
                logger.info("Some unexpected error has occurred during this transaction!")
                err()
            }
        }
    }

    private fun Player.mainHand() = this.getItemInHand(HandTypes.MAIN_HAND).getOrNull()?.createSnapshot()

    private fun ItemStackSnapshot?.isNothing() = (this == null || this.isEmpty || this.type == ItemTypes.AIR)

    private fun notifyPlayers(p1: Player, p2: Player, name: String, sent: Int) {
        val sentItem: Text = succ("You have sent x$sent of $name to ${p1.name}")
        val receivedItem: Text = succ("You have received x$sent of $name from ${p2.name}")
        p1.sendMessage(sentItem)
        p2.sendMessage(receivedItem)
    }

    private fun logTransaction(src: Player, target: Player, item: ItemStackSnapshot, amount: Int) {
        if (!config.transactionLogging) return
        logger.info("${src.name} --[${item.type.name}]x$amount-> ${target.name}")
    }

    private fun ItemStackSnapshot.minus(amount: Int): ItemStack {
        val stack = this.copy().createStack()
        stack.quantity -= amount
        return stack
    }

    private fun ItemStackSnapshot.eq(amount: Int): ItemStack {
        val stack = this.copy().createStack()
        stack.quantity = amount
        return stack
    }

    private fun <T> Optional<T?>.getOrNull(): T? {
        return orElse(null)
    }
}

