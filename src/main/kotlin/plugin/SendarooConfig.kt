package plugin

import ninja.leaping.configurate.objectmapping.Setting
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable

@ConfigSerializable
class SendarooConfig {
    @Setting(comment = "Intended for dev use only. Will enable sending items to self for whole server.")
    val debug: Boolean = false

    @Setting(comment = "Logs /send transactions to console ex: player1 --[minecraft:dirt]x5-> player2")
    val transactionLogging: Boolean = false
}
