////////////////////////////////////////////////////////////////////////////////
//                    MIT Licence (C) 2021 Cubicpath@Github                    /
////////////////////////////////////////////////////////////////////////////////

package com.cubicpath.cubicthings.common.command;

import com.cubicpath.util.ComponentUtils;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.text.*;
import net.minecraft.util.RegistryKey;
import net.minecraftforge.registries.*;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public final class ListRegistryCommand {
    private static final Dynamic2CommandExceptionType EMPTY_RESULTS_EXCEPTION = new Dynamic2CommandExceptionType((searchedRegistry, filterPhrase) -> new TranslationTextComponent("commands.listRegistry.failure.emptyResults", searchedRegistry, filterPhrase));
    private static final DynamicCommandExceptionType NO_REGISTRY_EXCEPTION = new DynamicCommandExceptionType((searchedRegistry) -> new TranslationTextComponent("commands.listRegistry.failure.noSuchRegistry", searchedRegistry));
    private static final HashMap<String, IForgeRegistry<?>> REGISTRY_MAP = new HashMap<>();
    public static final String COMMAND_NAME = "listregistry";

    public static void register(CommandDispatcher<CommandSource> dispatcher){
        // Adds all registries in Forge using reflection during registration. Should NEVER fail.
        try { for (Field field : ForgeRegistries.class.getFields()){ REGISTRY_MAP.put(field.getName().toLowerCase(), (IForgeRegistry<?>) field.get(null)); }
        } catch (IllegalAccessException ignored){}

        dispatcher.register(Commands.literal(COMMAND_NAME).executes((context) -> {
            return listRegistry(context, null, null);
        }).then(Commands.argument("registry", StringArgumentType.string()).executes((context) -> {
            return listRegistry(context, StringArgumentType.getString(context, "registry"), null);
        }).then(Commands.argument("filter", StringArgumentType.string()).executes(((context) -> {
            return listRegistry(context, StringArgumentType.getString(context, "registry"), StringArgumentType.getString(context, "filter"));
        })))));
    }

    private static int listRegistry(CommandContext<CommandSource> contextIn, @Nullable String registryIn, @Nullable String filterIn) throws CommandSyntaxException {
        IFormattableTextComponent message = StringTextComponent.EMPTY.plainCopy();
        if (registryIn == null){
            // Return message containing all forge registries if registry is not specified.
            IFormattableTextComponent registriesComponent = StringTextComponent.EMPTY.plainCopy();
            REGISTRY_MAP.keySet().forEach((key) -> registriesComponent.append(key).append(ComponentUtils.stringToText(" | ", 0x55FF55)));
            message.append(ComponentUtils.stringToText("---------------------------------------\n", 0xAA0000));
            message.append(ComponentUtils.stringToText("List of Registries:\n ", 0xAA0000));
            message.append(registriesComponent);
            message.append(ComponentUtils.stringToText("\n---------------------------------------", 0xAA0000));
        } else {
            boolean emptyResults = true;
            for (Object o : REGISTRY_MAP.keySet().stream().sorted().toArray()){
                String registryName = (String) o;
                if (registryIn.equalsIgnoreCase(registryName)){
                    IForgeRegistry<?> forgeRegistry = REGISTRY_MAP.get(registryName);
                    IFormattableTextComponent entriesComponent = StringTextComponent.EMPTY.plainCopy();

                    for (Map.Entry<? extends RegistryKey<?>, ? extends IForgeRegistryEntry<?>> registryEntry : forgeRegistry.getEntries()) {
                        String value = Objects.requireNonNull(registryEntry.getValue().getRegistryName(), "Registry Entry doesn't have a Registry Name").toString();
                        if (value.startsWith("minecraft:")) value = value.replaceFirst("minecraft:", "");
                        if (filterIn != null) value = value.contains(filterIn) ? value : "";
                        if (value.isEmpty()) continue;
                        // If non-empty result, mark as not empty
                        emptyResults = false;
                        entriesComponent.append(value).append(ComponentUtils.stringToText(" --- ", 0x55FF55));
                    }

                    if (!emptyResults) {
                        entriesComponent.append(entriesComponent.getSiblings().remove(entriesComponent.getSiblings().size() -1).getString().replace("---", "")); // Remove trailing entry separator
                        message.append(ComponentUtils.stringToText("---------------------------------------\n", 0xAA0000));
                        message.append(ComponentUtils.stringToText(registryName + (filterIn != null ?(" (containing phrase \"" + filterIn + "\")") : "") + ":\n ", 0xAA0000));
                        message.append(entriesComponent);
                        message.append(ComponentUtils.stringToText("\n---------------------------------------", 0xAA0000));
                    } else if (filterIn != null) throw EMPTY_RESULTS_EXCEPTION.create(registryIn, filterIn);
                }
            }

            if (emptyResults) throw NO_REGISTRY_EXCEPTION.create(registryIn);
        }

        // Send feedback to player
        contextIn.getSource().getPlayerOrException().sendMessage(message, PlayerEntity.createPlayerUUID(contextIn.getSource().getPlayerOrException().getGameProfile()));
        return 1;
    }

}
