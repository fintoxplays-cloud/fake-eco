package com.fakemoney.scoreboard.mixin;

import com.fakemoney.scoreboard.client.FakeMoneyState;
import com.fakemoney.scoreboard.client.MoneyTextReplacer;
import com.fakemoney.scoreboard.client.ScoreboardRenderContext;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * Hooks the single low-level text-drawing method that both
 * {@code DrawContext#drawText} and {@code DrawContext#drawTextWithShadow}
 * funnel through, and swaps the {@link Text} argument for a money-value
 * version of itself - but only while
 * {@link ScoreboardRenderContext#isInsideSidebar()} is true.
 *
 * <p>Because the swap is gated by that flag, this mixin never touches chat,
 * the player list, item tooltips, or any other on-screen text - only text
 * drawn from inside {@code InGameHud#renderScoreboardSidebar}. This also
 * makes it resilient to internal refactors of the sidebar method itself: as
 * long as it still ultimately calls this method to draw each line (true in
 * every Minecraft version to date), the hook keeps working.</p>
 *
 * <p>Verified against Yarn mappings for Minecraft 1.21.11
 * (yarn-1.21.11+build.4): {@code DrawContext#drawText(TextRenderer, Text,
 * int, int, int, boolean)}.</p>
 */
@Mixin(DrawContext.class)
public abstract class DrawContextMixin {

    @ModifyVariable(
            method = "drawText(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/text/Text;IIIZ)V",
            at = @At("HEAD"),
            argsOnly = true
    )
    private Text fakemoneyscoreboard$maybeReplaceMoneyText(Text text) {
        if (!ScoreboardRenderContext.isInsideSidebar()) {
            return text;
        }

        FakeMoneyState state = FakeMoneyState.getInstance();
        if (!state.isEnabled()) {
            return text;
        }

        return MoneyTextReplacer.replaceIfMoney(text, state.getDisplayValue());
    }
}
