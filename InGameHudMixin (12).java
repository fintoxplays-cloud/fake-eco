package com.fakemoney.scoreboard.mixin;

import com.fakemoney.scoreboard.client.ScoreboardRenderContext;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Flags exactly when the game is rendering the sidebar scoreboard, so
 * {@link com.fakemoney.scoreboard.mixin.DrawContextMixin} knows it's safe to
 * inspect the text about to be drawn. This mixin does not draw or change
 * anything by itself - it only flips a boolean.
 *
 * <p>Verified against Yarn mappings for Minecraft 1.21.11
 * (yarn-1.21.11+build.1): {@code InGameHud#render} and
 * {@code InGameHud#renderScoreboardSidebar} both take
 * {@code (DrawContext, RenderTickCounter)}.</p>
 */
@Mixin(InGameHud.class)
public abstract class InGameHudMixin {

    @Inject(
            method = "render(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/client/render/RenderTickCounter;)V",
            at = @At("HEAD")
    )
    private void fakemoneyscoreboard$resetFlagEachFrame(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        // Safety net: guarantee the flag is false at the start of every frame,
        // in case a previous frame's sidebar render threw before reaching RETURN.
        ScoreboardRenderContext.setInsideSidebar(false);
    }

    @Inject(
            method = "renderScoreboardSidebar(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/client/render/RenderTickCounter;)V",
            at = @At("HEAD")
    )
    private void fakemoneyscoreboard$enterSidebar(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        ScoreboardRenderContext.setInsideSidebar(true);
    }

    @Inject(
            method = "renderScoreboardSidebar(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/client/render/RenderTickCounter;)V",
            at = @At("RETURN")
    )
    private void fakemoneyscoreboard$exitSidebar(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        ScoreboardRenderContext.setInsideSidebar(false);
    }
}
