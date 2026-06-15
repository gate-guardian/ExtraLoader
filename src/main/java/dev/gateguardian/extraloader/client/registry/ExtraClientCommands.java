package dev.gateguardian.extraloader.client.registry;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import dev.gateguardian.extraloader.common.ExtraConfig;
import dev.gateguardian.extraloader.common.ExtraLoader;
import lombok.experimental.UtilityClass;
import net.minecraft.util.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * /extraloader open instance - Opens the global pack directory
 * /extraloader open system - Opens the system global pack directory
 */
@UtilityClass
public class ExtraClientCommands {

    public void init(CommandDispatcher<CommandSourceStack> dispatcher) {
        var root = Commands.literal(ExtraLoader.MOD_ID)
                .then(Commands.literal("open")
                        .then(Commands.literal("instance")
                                .executes(openAndReport(ExtraLoader.GLOBAL_PACK_DIR))
                        )
                        .then(Commands.literal("system")
                                .requires(src -> ExtraConfig.COMMON.enableSystemGlobalPacks.get())
                                .executes(openAndReport(ExtraLoader.SYSTEM_GLOBAL_PACK_DIR))
                        )
                );
        dispatcher.register(root);
    }

    private Command<CommandSourceStack> openAndReport(Path directory) {
        return ctx -> {
            try {
                Files.createDirectories(directory);
            } catch (FileAlreadyExistsException e) {
                ExtraLoader.LOGGER.error("Path is not a directory: {}", directory);
                ctx.getSource().sendFailure(
                        Component.translatable("command.extraloader.open.failure.notDirectory", directory)
                );
                return 0;
            } catch (Exception e) {
                ExtraLoader.LOGGER.error("Failed to open directory {}", directory, e);
                ctx.getSource().sendFailure(
                        Component.translatable("command.extraloader.open.failure.failed", directory)
                );
                return 0;
            }

            Util.getPlatform().openFile(directory.toFile());
            ctx.getSource().sendSuccess(
                    () -> Component.translatable("command.extraloader.open.success", directory),
                    false
            );
            return 1;
        };
    }
}

