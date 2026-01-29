import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.file.Files;

package com.fragmc.rpc;

import club.minnced.discord.rpc.*;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.MinecraftClient;

public class FragRPC implements ClientModInitializer {

    private static final String APPLICATION_ID = "1131048770109460500";

    private static final String STATUS_DETAILS =
            "A new generation of Minecraft Parkour";
    private static final String STATUS_STATE =
            "fragmc.github.io | Play now";

    private static final String LARGE_IMAGE_KEY = "icon";
    private static final String LARGE_IMAGE_TEXT = "FragMC";

    @Override
    public void onInitializeClient() {
        DiscordRPC lib = DiscordRPC.INSTANCE;

        DiscordEventHandlers handlers = new DiscordEventHandlers();

        lib.Discord_Initialize(APPLICATION_ID, handlers, true, "");

        Thread rpcThread = new Thread(() -> {
            DiscordRichPresence presence = new DiscordRichPresence();
            presence.startTimestamp = System.currentTimeMillis() / 1000;

            MinecraftClient client = MinecraftClient.getInstance();

            while (!Thread.currentThread().isInterrupted()) {
                lib.Discord_RunCallbacks();

                presence.details = STATUS_DETAILS;
                presence.state = STATUS_STATE;

                presence.largeImageKey = LARGE_IMAGE_KEY;
                presence.largeImageText = LARGE_IMAGE_TEXT;

                lib.Discord_UpdatePresence(presence);

                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    break;
                }
            }
        }, "FragRPC-Thread");

        rpcThread.setDaemon(true);
        rpcThread.start();
    }
}
