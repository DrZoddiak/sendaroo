package plugin

import com.google.inject.Inject
import org.slf4j.Logger
import org.spongepowered.api.Sponge
import org.spongepowered.api.event.Listener
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
    private val logger: Logger
) {
    @Listener
    fun onServerInit(event: GamePreInitializationEvent) {
        logger.info("Sendaroo loading commands!")
        Sponge.getCommandManager().register(this, Commands().spec, "send")
    }
}
