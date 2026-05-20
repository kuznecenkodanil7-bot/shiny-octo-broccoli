package dev.adamassistant;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.Locale;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class AdamAssistant {
    private static final Pattern GIVE_PATTERN = Pattern.compile("^(?:дай|выдай|добавь|положи)(?:\\s+мне)?\\s+(.+)$");
    private static final Pattern COMMAND_PATTERN = Pattern.compile("^(?:выполни|исполни|команда|запусти)(?:\\s+команду)?\\s+(.+)$");

    /**
     * false = любой игрок может просить предметы.
     * true = выдача предметов работает только у OP / игроков с permission level 2+.
     */
    private static final boolean REQUIRE_OP_FOR_GIVE = false;

    private AdamAssistant() {
    }

    public static boolean isAddressedToAdam(String rawText) {
        String normalized = TextRu.normalize(rawText);
        return normalized.equals("адам") || normalized.startsWith("адам ");
    }

    public static void handle(ServerPlayerEntity player, String rawText) {
        String request = stripWakeWord(rawText);

        if (request.isBlank()) {
            reply(player, helpText());
            return;
        }

        Matcher commandMatcher = COMMAND_PATTERN.matcher(TextRu.normalizeKeepCommandChars(request));
        if (request.trim().startsWith("/") || commandMatcher.matches()) {
            runMinecraftCommand(player, commandMatcher.matches() ? commandMatcher.group(1) : request.trim());
            return;
        }

        Matcher giveMatcher = GIVE_PATTERN.matcher(TextRu.normalize(request));
        if (giveMatcher.matches()) {
            giveItem(player, giveMatcher.group(1));
            return;
        }

        if (TextRu.containsAny(TextRu.normalize(request), "помоги", "помощь", "help", "что ты умеешь")) {
            reply(player, helpText());
            return;
        }

        reply(player, "Не понял запрос. Примеры: 'адам дай бревно акации', 'адам дай 64 алмаза', 'адам выполни /time set day'.");
    }

    public static String helpText() {
        return "Адам: пиши в чат 'адам дай бревно акации', 'адам дай 64 алмаза', 'адам дай стак дубовых досок' или 'адам выполни /time set day'. Также работает /adam <запрос>.";
    }

    private static String stripWakeWord(String rawText) {
        String trimmed = rawText == null ? "" : rawText.trim();
        String lower = trimmed.toLowerCase(Locale.ROOT).replace('ё', 'е');

        if (lower.equals("адам")) {
            return "";
        }

        if (lower.startsWith("адам")) {
            return trimmed.substring(Math.min(trimmed.length(), 4)).trim();
        }

        return trimmed;
    }

    private static void giveItem(ServerPlayerEntity player, String itemRequest) {
        if (REQUIRE_OP_FOR_GIVE && !CommandManager.requirePermissionLevel(CommandManager.GAMEMASTERS_CHECK).test(player.getCommandSource())) {
            reply(player, "Выдача предметов отключена для игроков без OP.");
            return;
        }

        ParsedGive parsed = ParsedGive.parse(itemRequest);
        Optional<Item> maybeItem = ItemResolver.resolve(parsed.itemPhrase());

        if (maybeItem.isEmpty()) {
            reply(player, "Не нашел предмет: '" + parsed.itemPhrase() + "'. Можно написать ID, например: адам дай minecraft:acacia_log");
            return;
        }

        Item item = maybeItem.get();
        int remaining = parsed.count();
        int maxStack = Math.max(1, item.getMaxCount());

        while (remaining > 0) {
            int amount = Math.min(remaining, maxStack);
            player.getInventory().offerOrDrop(new ItemStack(item, amount));
            remaining -= amount;
        }

        Identifier id = Registries.ITEM.getId(item);
        reply(player, "Выдал: " + parsed.count() + " × " + id);
    }

    private static void runMinecraftCommand(ServerPlayerEntity player, String command) {
        String cleanCommand = command.trim();

        if (cleanCommand.startsWith("/")) {
            cleanCommand = cleanCommand.substring(1);
        }

        if (cleanCommand.isBlank()) {
            reply(player, "После 'выполни' нужна команда. Пример: адам выполни /time set day");
            return;
        }

        try {
            player.getCommandSource()
                    .getServer()
                    .getCommandManager()
                    .parseAndExecute(player.getCommandSource(), cleanCommand);

            reply(player, "Команда отправлена: /" + cleanCommand);
        } catch (Exception exception) {
            reply(player, "Не смог выполнить команду: " + exception.getMessage());
            AdamAssistantMod.LOGGER.warn("Failed to execute Adam command '{}' for {}", cleanCommand, player.getName().getString(), exception);
        }
    }

    private static void reply(ServerPlayerEntity player, String message) {
        player.sendMessage(Text.literal("§bАдам§r: " + message), false);
    }
}
