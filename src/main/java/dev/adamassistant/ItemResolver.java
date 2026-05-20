package dev.adamassistant;

import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public final class ItemResolver {
    private static final Map<String, String> ALIASES = new HashMap<>();

    static {
        registerBasicItems();
        registerWood("oak", "дуба", "дубовое", "дубовые", "дубовый");
        registerWood("spruce", "ели", "еловое", "еловые", "еловый");
        registerWood("birch", "березы", "березовое", "березовые", "березовый");
        registerWood("jungle", "тропического дерева", "тропическое", "тропические", "тропический");
        registerWood("acacia", "акации", "акациевое", "акациевые", "акациевый");
        registerWood("dark_oak", "темного дуба", "темно дубовое", "темно дубовые", "темно дубовый");
        registerWood("mangrove", "мангрового дерева", "мангровое", "мангровые", "мангровый");
        registerWood("cherry", "вишни", "вишневое", "вишневые", "вишневый");
        registerStem("crimson", "багровый", "багровая", "багровые");
        registerStem("warped", "искаженный", "искаженная", "искаженные");
    }

    private ItemResolver() {
    }

    public static Optional<Item> resolve(String rawPhrase) {
        String phrase = TextRu.normalize(rawPhrase);

        if (phrase.isBlank()) {
            return Optional.empty();
        }

        String alias = ALIASES.get(phrase);
        if (alias != null) {
            return byId(alias);
        }

        Optional<Item> direct = byId(rawPhrase.trim());
        if (direct.isPresent()) {
            return direct;
        }

        Optional<Item> pathLike = byId(phrase.replace(' ', '_'));
        if (pathLike.isPresent()) {
            return pathLike;
        }

        if (phrase.matches("[a-z0-9_ :/-]+")) {
            Optional<Item> fuzzy = fuzzyEnglishIdSearch(phrase);
            if (fuzzy.isPresent()) {
                return fuzzy;
            }
        }

        return Optional.empty();
    }

    private static Optional<Item> byId(String rawId) {
        if (rawId == null || rawId.isBlank()) {
            return Optional.empty();
        }

        String id = rawId.toLowerCase(Locale.ROOT).trim().replace(' ', '_');
        if (!id.contains(":")) {
            id = "minecraft:" + id;
        }

        Identifier identifier = Identifier.tryParse(id);
        if (identifier == null) {
            return Optional.empty();
        }

        Item item = Registries.ITEM.get(identifier);
        if (item == Items.AIR && !identifier.getPath().equals("air")) {
            return Optional.empty();
        }

        return Optional.of(item);
    }

    private static Optional<Item> fuzzyEnglishIdSearch(String phrase) {
        String[] tokens = phrase.replace(':', ' ').replace('/', ' ').replace('-', ' ').replace('_', ' ').split("\\s+");

        for (Identifier id : Registries.ITEM.getIds()) {
            String path = id.getPath().replace('_', ' ');
            boolean allMatch = true;

            for (String token : tokens) {
                if (!token.isBlank() && !path.contains(token)) {
                    allMatch = false;
                    break;
                }
            }

            if (allMatch) {
                return byId(id.toString());
            }
        }

        return Optional.empty();
    }

    private static void registerBasicItems() {
        register("minecraft:air", "воздух");
        register("minecraft:stone", "камень");
        register("minecraft:cobblestone", "булыжник", "коблстоун", "кобл");
        register("minecraft:dirt", "земля", "грязь");
        register("minecraft:grass_block", "блок травы", "трава");
        register("minecraft:sand", "песок");
        register("minecraft:red_sand", "красный песок");
        register("minecraft:gravel", "гравий");
        register("minecraft:glass", "стекло");
        register("minecraft:obsidian", "обсидиан");
        register("minecraft:torch", "факел", "факела", "факелы");
        register("minecraft:stick", "палка", "палки", "палочку");
        register("minecraft:apple", "яблоко", "яблоки");
        register("minecraft:bread", "хлеб");
        register("minecraft:diamond", "алмаз", "алмазы", "алмазов");
        register("minecraft:emerald", "изумруд", "изумруды", "изумрудов");
        register("minecraft:coal", "уголь", "угля");
        register("minecraft:charcoal", "древесный уголь");
        register("minecraft:iron_ingot", "железо", "железный слиток", "железные слитки", "слиток железа");
        register("minecraft:gold_ingot", "золото", "золотой слиток", "золотые слитки", "слиток золота");
        register("minecraft:copper_ingot", "медь", "медный слиток", "слиток меди");
        register("minecraft:netherite_ingot", "незерит", "незеритовый слиток", "слиток незерита");
        register("minecraft:iron_pickaxe", "железная кирка", "кирка железная");
        register("minecraft:diamond_pickaxe", "алмазная кирка", "кирка алмазная");
        register("minecraft:netherite_pickaxe", "незеритовая кирка", "кирка незеритовая");
        register("minecraft:crafting_table", "верстак");
        register("minecraft:furnace", "печь");
        register("minecraft:chest", "сундук");
        register("minecraft:water_bucket", "ведро воды", "вода в ведре");
        register("minecraft:lava_bucket", "ведро лавы", "лава в ведре");
    }

    private static void registerWood(String id, String genitive, String adjectiveSingular, String adjectivePlural, String adjectiveMasculine) {
        register("minecraft:" + id + "_log", "бревно " + genitive, adjectiveSingular + " бревно", adjectiveMasculine + " лог", id + " log");
        register("minecraft:stripped_" + id + "_log", "очищенное бревно " + genitive, "обтесанное бревно " + genitive, "зачищенное бревно " + genitive);
        register("minecraft:" + id + "_wood", "древесина " + genitive, adjectiveSingular + " дерево");
        register("minecraft:stripped_" + id + "_wood", "очищенная древесина " + genitive, "обтесанная древесина " + genitive);
        register("minecraft:" + id + "_planks", "доски " + genitive, adjectivePlural + " доски");
        register("minecraft:" + id + "_stairs", "ступеньки " + genitive, adjectivePlural + " ступеньки", adjectivePlural + " лестницы");
        register("minecraft:" + id + "_slab", "плита " + genitive, adjectiveSingular + " плита", adjectivePlural + " плиты");
        register("minecraft:" + id + "_fence", "забор " + genitive, adjectiveMasculine + " забор");
        register("minecraft:" + id + "_fence_gate", "калитка " + genitive, adjectiveSingular + " калитка");
        register("minecraft:" + id + "_door", "дверь " + genitive, adjectiveSingular + " дверь");
        register("minecraft:" + id + "_trapdoor", "люк " + genitive, adjectiveMasculine + " люк");
        register("minecraft:" + id + "_button", "кнопка " + genitive, adjectiveSingular + " кнопка");
        register("minecraft:" + id + "_pressure_plate", "нажимная плита " + genitive, adjectiveSingular + " нажимная плита");
        register("minecraft:" + id + "_sign", "табличка " + genitive, adjectiveSingular + " табличка");
        register("minecraft:" + id + "_hanging_sign", "подвесная табличка " + genitive, adjectiveSingular + " подвесная табличка");
        register("minecraft:" + id + "_leaves", "листва " + genitive, "листья " + genitive, adjectivePlural + " листья");
        register("minecraft:" + id + "_sapling", "саженец " + genitive, adjectiveMasculine + " саженец");
    }

    private static void registerStem(String id, String masculine, String feminine, String plural) {
        register("minecraft:" + id + "_stem", masculine + " стебель", "стебель " + masculine);
        register("minecraft:stripped_" + id + "_stem", "очищенный " + masculine + " стебель", "обтесанный " + masculine + " стебель");
        register("minecraft:" + id + "_hyphae", feminine + " гифа", plural + " гифы");
        register("minecraft:stripped_" + id + "_hyphae", "очищенная " + feminine + " гифа", "очищенные " + plural + " гифы");
        register("minecraft:" + id + "_planks", plural + " доски", "доски " + masculine);
    }

    private static void register(String id, String... aliases) {
        for (String alias : aliases) {
            ALIASES.put(TextRu.normalize(alias), id);
        }
    }
}
