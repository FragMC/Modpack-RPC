package com.fragmc.rpc;

import club.minnced.discord.rpc.DiscordEventHandlers;
import club.minnced.discord.rpc.DiscordRPC;
import club.minnced.discord.rpc.DiscordRichPresence;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.MinecraftClient;

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

    @Override
    public void onInitializeClient() {
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
