package plugin

import ninja.leaping.configurate.objectmapping.Setting
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable

@ConfigSerializable
class SendarooConfig {
    @Setting
    val debug: Boolean = false

    @Setting
    val transactionLogging: Boolean = false
}
