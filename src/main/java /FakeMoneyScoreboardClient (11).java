package com.fakemoney.scoreboard.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public final class FakeMoneyScoreboardClient implements ClientModInitializer {
    public static final String KEY_CATEGORY = "key.categories.fakeeconomy";
    public static final String KEY_TRANSLATION = "key.fakeeconomy.open_gui";

    private static KeyBinding openGuiKey;

    @Override
    public void onInitializeClient() {
        FakeEconomyState.getInstance().load(net.minecraft.client.MinecraftClient.getInstance());

        openGuiKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                KEY_TRANSLATION,
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_RIGHT_ALT,
                KEY_CATEGORY
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openGuiKey.wasPressed()) {
                if (client.currentScreen == null) {
                    client.setScreen(new FakeEconomyScreen());
                }
            }
        });
    }
}
