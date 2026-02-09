package dev.gateguardian.extraloader.common.pack;

import dev.gateguardian.extraloader.common.ExtraLoader;
import lombok.Getter;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.repository.PackSource;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

/**
 * Extra pack load modes for resource packs and data packs
 *
 * @author GateGuardian
 * @since 2.0.0
 */
@Getter
public enum PackLoadMode {
    REQUIRED(
            "required",
            createPackSource(ChatFormatting.DARK_RED, true),
            true
    ),
    OPTIONAL(
            "optional",
            createPackSource(ChatFormatting.DARK_AQUA, false),
            false
    ),
    DEFAULT(
            "default",
            createPackSource(ChatFormatting.DARK_GREEN, true),
            false
    );

    public static final PackLoadMode[] VALUES = values();

    private final String folderName;
    private final PackSource packSource;
    private final boolean required;

    PackLoadMode(String folderName, PackSource packSource, boolean required) {
        this.folderName = folderName;
        this.packSource = packSource;
        this.required = required;
    }

    private static PackSource createPackSource(ChatFormatting color, boolean addAutomatically) {
        return PackSource.create(
                name -> Component.translatable("pack.nameAndSource", name, Component.literal(ExtraLoader.MOD_NAME))
                        .withStyle(color),
                addAutomatically
        );
    }

    @Nullable
    public static PackLoadMode fromFolderName(String folderName) {
        folderName = folderName.toLowerCase(Locale.ROOT);
        for (PackLoadMode mode : VALUES) {
            if (mode.getFolderName().equals(folderName)) {
                return mode;
            }
        }
        return null;
    }
}
