package dev.adamassistant;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class AdamAssistantMod implements ModInitializer {
    public static final String MOD_ID = "adam_assistant";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        ServerMessageEvents.ALLOW_CHAT_MESSAGE.register((message, sender, params) -> {
            String rawText = message.getContent().getString();

            if (!AdamAssistant.isAddressedToAdam(rawText)) {
                return true;
            }

            AdamAssistant.handle(sender, rawText);
            return false;
        });

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(
                CommandManager.literal("adam")
                        .executes(context -> {
                            context.getSource().sendFeedback(() -> Text.literal(AdamAssistant.helpText()), false);
                            return 1;
                        })
                        .then(CommandManager.argument("request", StringArgumentType.greedyString())
                                .executes(context -> {
                                    ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
                                    AdamAssistant.handle(player, "адам " + StringArgumentType.getString(context, "request"));
                                    return 1;
                                }))
        ));

        LOGGER.info("Adam Assistant initialized for Minecraft chat trigger 'адам'.");
    }
}
