package ru.niksne.vanillarpc;

import club.minnced.discord.rpc.DiscordEventHandlers;
import club.minnced.discord.rpc.DiscordRPC;
import club.minnced.discord.rpc.DiscordRichPresence;
import net.fabricmc.api.ClientModInitializer;

public class VanillaRPC implements ClientModInitializer {
    
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
     * Example: "Playing Minecraft"
     */
    private static final String STATUS_DETAILS = "Playing Custom Modpack";
    
    /**
     * The status state line (bottom line)
     * Example: "In the Overworld"
     */
    private static final String STATUS_STATE = "Exploring the World";
    
    /**
     * Large image key (must be uploaded to your Discord app)
     * Leave empty "" to not show an image
     */
    private static final String LARGE_IMAGE_KEY = "minecraft";
    
    /**
     * Large image hover text
     */
    private static final String LARGE_IMAGE_TEXT = "Custom Modpack";
    
    /**
     * Small image key (must be uploaded to your Discord app)
     * Leave empty "" to not show a small image
     */
    private static final String SMALL_IMAGE_KEY = "";
    
    /**
     * Small image hover text
     */
    private static final String SMALL_IMAGE_TEXT = "";
    
    /**
     * Enable Join button
     * Set to true to allow people to join your server
     */
    private static final boolean ENABLE_JOIN = true;
    
    /**
     * Server IP for join button
     * Example: "play.myserver.net"
     */
    private static final String SERVER_IP = "play.example.com";
    
    /**
     * Server Port (usually 25565)
     */
    private static final int SERVER_PORT = 25565;
    
    /**
     * Join button text
     */
    private static final String JOIN_TEXT = "Join Server";
    
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
        
        // Handle join requests if enabled
        if (ENABLE_JOIN) {
            handlers.joinRequest = (request) -> {
                System.out.println("Join request from: " + request.username);
                // Auto-accept join requests
                lib.Discord_Respond(request.userId, DiscordRPC.DISCORD_REPLY_YES);
            };
            
            handlers.joinGame = (joinSecret) -> {
                System.out.println("Player joining via Discord");
            };
        }
        
        lib.Discord_Initialize(APPLICATION_ID, handlers, true, "");
        
        DiscordRichPresence presence = new DiscordRichPresence();
        presence.startTimestamp = System.currentTimeMillis() / 1000;
        
        // Set status text
        if (!STATUS_DETAILS.isEmpty()) {
            presence.details = STATUS_DETAILS;
        }
        
        if (!STATUS_STATE.isEmpty()) {
            presence.state = STATUS_STATE;
        }
        
        // Set images
        if (!LARGE_IMAGE_KEY.isEmpty()) {
            presence.largeImageKey = LARGE_IMAGE_KEY;
            presence.largeImageText = LARGE_IMAGE_TEXT;
        }
        
        if (!SMALL_IMAGE_KEY.isEmpty()) {
            presence.smallImageKey = SMALL_IMAGE_KEY;
            presence.smallImageText = SMALL_IMAGE_TEXT;
        }
        
        // Set party/player count if enabled
        if (MAX_PARTY_SIZE > 0) {
            presence.partySize = 1;
            presence.partyMax = MAX_PARTY_SIZE;
        }
        
        // Set join secret if enabled
        if (ENABLE_JOIN) {
            presence.joinSecret = SERVER_IP + ":" + SERVER_PORT;
            // You can also set a party ID to allow joining
            presence.partyId = "custom-modpack-party";
        }
        
        lib.Discord_UpdatePresence(presence);
        
        // Update presence periodically
        new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                lib.Discord_RunCallbacks();
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    break;
                }
            }
        }).start();
    }
}
