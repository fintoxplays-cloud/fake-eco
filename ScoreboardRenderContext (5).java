package com.fakemoney.scoreboard.client;

/**
 * A tiny piece of shared state that lets {@code InGameHudMixin} tell
 * {@code DrawContextMixin} "we are currently rendering the sidebar scoreboard,
 * so it's safe to inspect/replace the text about to be drawn".
 *
 * <p>This is the core trick that keeps the mod from touching anything else on
 * screen: the flag is only ever {@code true} while control is inside
 * {@code InGameHud#renderScoreboardSidebar}, so chat, the player list, the
 * hotbar, boss bars, etc. are never modified, no matter how the sidebar
 * method happens to be implemented internally.</p>
 *
 * <p>Both fields are only ever touched on the render thread, but they are
 * marked volatile defensively since HUD rendering and command execution can
 * both run close together in time.</p>
 */
public final class ScoreboardRenderContext {

    private static volatile boolean insideSidebar = false;

    private ScoreboardRenderContext() {
    }

    public static void setInsideSidebar(boolean value) {
        insideSidebar = value;
    }

    public static boolean isInsideSidebar() {
        return insideSidebar;
    }
}
