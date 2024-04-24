package plugin

import com.google.inject.Inject
import ninja.leaping.configurate.commented.CommentedConfigurationNode
import ninja.leaping.configurate.loader.ConfigurationLoader
import ninja.leaping.configurate.objectmapping.Setting
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable
import org.slf4j.Logger
import org.spongepowered.api.Sponge
import org.spongepowered.api.config.DefaultConfig
import org.spongepowered.api.event.Listener
import org.spongepowered.api.event.game.state.GameConstructionEvent
import org.spongepowered.api.event.game.state.GamePreInitializationEvent
import org.spongepowered.api.plugin.Dependency
import org.spongepowered.api.plugin.Plugin
import org.spongepowered.api.plugin.PluginContainer

@Plugin(
    id = "sendaroo",
    name = "sendaroo",
    version = "1.0-SNAPSHOT",
    description = "Allows users to send items to other users!",
    dependencies = [Dependency(id = "spotlin", version = "0.2.0", optional = false)]
)
class Sendaroo @Inject constructor(
    private val container: PluginContainer,
    private val logger: Logger,
    @DefaultConfig(sharedRoot = false)
    private val loader: ConfigurationLoader<CommentedConfigurationNode>
) {

    private lateinit var config: SendarooConfig

    @Listener
    fun onGameConstruct(event: GameConstructionEvent) {
        logger.info("initializing config...")
        val ref = loader.loadToReference().referenceTo(SendarooConfig::class.java)
        config = ref.get() ?: SendarooConfig()
        ref.setAndSave(config)
    }

    @Listener
    fun onServerInit(event: GamePreInitializationEvent) {
        logger.info("Sendaroo loading commands!")
        Sponge.getCommandManager().register(this, Commands(logger, config).spec, "send")
    }
}

@ConfigSerializable
class SendarooConfig {
    @Setting
    val debug: Boolean = false
}
