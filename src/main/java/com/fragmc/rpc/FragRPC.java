package com.fragmc.rpc;

import club.minnced.discord.rpc.DiscordEventHandlers;
import club.minnced.discord.rpc.DiscordRPC;
import club.minnced.discord.rpc.DiscordRichPresence;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.MinecraftClient;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.file.Files;

public class FragRPC implements ClientModInitializer {

    // ========================================
    // CUSTOMIZATION SECTION - EDIT THESE VALUES
    // ========================================

    /**
     * Your Discord Application ID
     * Get one from: https://discord.com/developers/applications
     */
    private static final String APPLICATION_ID = "1131048770109460500";

    /**
     * The status details line (top line)
     */
    private static final String STATUS_DETAILS = "A new generation of Minecraft Parkour";

    /**
     * The status state line (bottom line)
     */
    private static final String STATUS_STATE = "fragmc.github.io | Play now";

    /**
     * Large image key (must be uploaded to your Discord app)
     */
    private static final String LARGE_IMAGE_KEY = "icon";

    /**
     * Large image hover text
     */
    private static final String LARGE_IMAGE_TEXT = "FragMC";

    /**
     * Enable Join button
     */
    private static final boolean ENABLE_JOIN = true;

    /**
     * Server IP for join button
     */
    private static final String SERVER_IP = "play.example.com";

    /**
     * Server Port (usually 25565)
     */
    private static final int SERVER_PORT = 25565;

    /**
     * Maximum party size (for showing player count)
     * Set to 0 to disable party display
     */
    private static final int MAX_PARTY_SIZE = 0;

    // ========================================
    // END CUSTOMIZATION SECTION
    // ========================================

    private static void loadNativeLibrary() {
        try {
            String os = System.getProperty("os.name").toLowerCase();
            String arch = System.getProperty("os.arch");
            
            String libraryPath;
            String libraryName;
            
            if (os.contains("win")) {
                if (arch.contains("64")) {
                    libraryPath = "/win32-x86-64/discord-rpc.dll";
                } else {
                    libraryPath = "/win32-x86/discord-rpc.dll";
                }
                libraryName = "discord-rpc.dll";
            } else if (os.contains("mac")) {
                libraryPath = "/darwin/libdiscord-rpc.dylib";
                libraryName = "libdiscord-rpc.dylib";
            } else {
                // Linux
                libraryPath = "/linux-x86-64/libdiscord-rpc.so";
                libraryName = "libdiscord-rpc.so";
            }
            
            // Try to load from JAR resources
            InputStream in = FragRPC.class.getResourceAsStream(libraryPath);
            if (in != null) {
                // Extract to temp directory
                File tempDir = new File(System.getProperty("java.io.tmpdir"), "discord-rpc");
                tempDir.mkdirs();
                File tempLib = new File(tempDir, libraryName);
                
                // Only extract if doesn't exist or is different
                if (!tempLib.exists()) {
                    try (FileOutputStream out = new FileOutputStream(tempLib)) {
                        byte[] buffer = new byte[8192];
                        int read;
                        while ((read = in.read(buffer)) != -1) {
                            out.write(buffer, 0, read);
                        }
                    }
                    tempLib.deleteOnExit();
                }
                in.close();
                
                // Load the library
                System.load(tempLib.getAbsolutePath());
                System.out.println("[FragRPC] Successfully loaded native Discord RPC library from: " + tempLib.getAbsolutePath());
            } else {
                System.err.println("[FragRPC] Could not find native library in JAR: " + libraryPath);
                System.err.println("[FragRPC] Attempting to use system library...");
            }
        } catch (Exception e) {
            System.err.println("[FragRPC] Failed to load Discord RPC native library:");
            e.printStackTrace();
        }
    }

    @Override
    public void onInitializeClient() {
        // Load native library first
        loadNativeLibrary();
        
        DiscordRPC lib = DiscordRPC.INSTANCE;

        DiscordEventHandlers handlers = new DiscordEventHandlers();

        if (ENABLE_JOIN) {
            handlers.joinRequest = (request) -> {
                lib.Discord_Respond(request.userId, DiscordRPC.DISCORD_REPLY_YES);
            };
        }

        lib.Discord_Initialize(APPLICATION_ID, handlers, true, "");

        // Update presence periodically
        new Thread(() -> {
            MinecraftClient client = MinecraftClient.getInstance();
            DiscordRichPresence presence = new DiscordRichPresence();
            presence.startTimestamp = System.currentTimeMillis() / 1000;

            while (!Thread.currentThread().isInterrupted()) {
                lib.Discord_RunCallbacks();

                // Update dynamic values
                presence.details = STATUS_DETAILS;
                presence.state = STATUS_STATE;
                presence.largeImageKey = LARGE_IMAGE_KEY;
                presence.largeImageText = LARGE_IMAGE_TEXT;

                // Dynamic Player Head & Username
                if (client.player != null) {
                    presence.smallImageKey = "https://crafatar.com/avatars/" + client.player.getUuid();
                    presence.smallImageText = client.player.getName().getString();
                } else {
                    presence.smallImageKey = "";
                    presence.smallImageText = "";
                }

                if (ENABLE_JOIN) {
                    presence.joinSecret = SERVER_IP + ":" + SERVER_PORT;
                    presence.partyId = "custom-modpack-party";
                    if (MAX_PARTY_SIZE > 0) {
                        presence.partySize = 1;
                        presence.partyMax = MAX_PARTY_SIZE;
                    }
                }

                lib.Discord_UpdatePresence(presence);

                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    break;
                }
            }
        }, "RPC-Handler").start();
    }
}
