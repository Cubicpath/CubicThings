////////////////////////////////////////////////////////////////////////////////
//                    MIT Licence (C) 2021 Cubicpath@Github                    /
////////////////////////////////////////////////////////////////////////////////

package com.cubicpath.cubicthings.common.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.text.*;
import net.minecraftforge.registries.*;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ListRegistryCommand {
    private static final Dynamic2CommandExceptionType EMPTY_RESULTS_EXCEPTION = new Dynamic2CommandExceptionType((searchedRegistry, filterPhrase) -> new TranslationTextComponent("commands.listRegistry.failure.emptyResults", searchedRegistry, filterPhrase));
    private static final DynamicCommandExceptionType NO_REGISTRY_EXCEPTION = new DynamicCommandExceptionType((searchedRegistry) -> new TranslationTextComponent("commands.listRegistry.failure.noSuchRegistry", searchedRegistry));
    private static final HashMap<String, IForgeRegistry<?>> REGISTRY_MAP = new HashMap<>();
    private static final Style RED_STYLE = Style.EMPTY.setColor(Color.fromHex("#AA0000"));
    public static final String COMMAND_NAME = "listregistry";

    public static void register(CommandDispatcher<CommandSource> dispatcher){
        // Adds all registries in Forge using reflection during registration. Should NEVER fail.
        try{ for (Field field : ForgeRegistries.class.getFields()){ REGISTRY_MAP.put(field.getName().toLowerCase(), (IForgeRegistry<?>) field.get(null)); }
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
        IFormattableTextComponent message = (IFormattableTextComponent) ITextComponent.getTextComponentOrEmpty("");
        if (registryIn == null){
            // Return message containing all forge registries if registry is not specified.
            IFormattableTextComponent registriesComponent = (IFormattableTextComponent) ITextComponent.getTextComponentOrEmpty("");
            REGISTRY_MAP.keySet().forEach((key) -> registriesComponent.appendString(key + " \u00A7a|\u00A7r "));
            message.appendSibling(((IFormattableTextComponent) ITextComponent.getTextComponentOrEmpty("---------------------------------------\n")).setStyle(RED_STYLE));
            message.appendSibling(((IFormattableTextComponent) ITextComponent.getTextComponentOrEmpty("List of Registries:\n ")).setStyle(RED_STYLE));
            message.appendSibling(registriesComponent);
            message.appendSibling(((IFormattableTextComponent) ITextComponent.getTextComponentOrEmpty("\n---------------------------------------")).setStyle(RED_STYLE));
        } else {
            boolean emptyResults = true;
            for (Object o : REGISTRY_MAP.keySet().stream().sorted().toArray()){
                String registryName = (String) o;
                if (registryIn.equalsIgnoreCase(registryName)){
                    IForgeRegistry<?> forgeRegistry = REGISTRY_MAP.get(registryName);
                    IFormattableTextComponent entriesComponent = (IFormattableTextComponent) ITextComponent.getTextComponentOrEmpty("");


                    for (Map.Entry<? extends RegistryKey<?>, ? extends IForgeRegistryEntry<?>> registryEntry : forgeRegistry.getEntries()) {
                        String value = Objects.requireNonNull(registryEntry.getValue().getRegistryName(), "Registry Entry doesn't have a Registry Name").toString();
                        if (value.startsWith("minecraft:")) value = value.replaceFirst("minecraft:", "");
                        if (filterIn != null) value = value.contains(filterIn) ? value : "";
                        if (value.isEmpty()) continue;
                        // If non-empty result, mark as not empty
                        emptyResults = false;
                        entriesComponent.appendString(value + " \u00A7a---\u00A7r ");
                    }

                    if (!emptyResults) {
                        entriesComponent.appendString(entriesComponent.getSiblings().remove(entriesComponent.getSiblings().size() -1).getString().replace("---", "")); // Remove trailing entry separator
                        message.appendSibling(((IFormattableTextComponent) ITextComponent.getTextComponentOrEmpty("---------------------------------------\n")).setStyle(RED_STYLE));
                        message.appendSibling(((IFormattableTextComponent) ITextComponent.getTextComponentOrEmpty(registryName + (filterIn != null ?(" (containing phrase \"" + filterIn + "\")") : "") + ":\n ")).setStyle(RED_STYLE));
                        message.appendSibling(entriesComponent);
                        message.appendSibling(((IFormattableTextComponent) ITextComponent.getTextComponentOrEmpty("\n---------------------------------------")).setStyle(RED_STYLE));
                    } else if (filterIn != null) throw EMPTY_RESULTS_EXCEPTION.create(registryIn, filterIn);
                }
            }

            if (emptyResults) throw NO_REGISTRY_EXCEPTION.create(registryIn);
        }

        // Send feedback to player
        contextIn.getSource().asPlayer().sendMessage(message, PlayerEntity.getUUID(contextIn.getSource().asPlayer().getGameProfile()));
        return 1;
    }

}
