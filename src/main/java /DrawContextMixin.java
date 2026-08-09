package com.fakemoney.scoreboard.mixin;

import com.fakemoney.scoreboard.client.FakeEconomyState;
import com.fakemoney.scoreboard.client.MoneyTextReplacer;
import com.fakemoney.scoreboard.client.ScoreboardRenderContext;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(DrawContext.class)
public abstract class DrawContextMixin {
    @ModifyVariable(
            method = "drawText(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/text/Text;IIIZ)V",
            at = @At("HEAD"),
            argsOnly = true
    )
    private Text fakemoneyscoreboard$replaceEconomyText(Text text) {
        if (!ScoreboardRenderContext.isInsideSidebar()) {
            return text;
        }

        FakeEconomyState state = FakeEconomyState.getInstance();
        if ((state.getMoney() == null || state.getMoney().isBlank())
                && (state.getShards() == null || state.getShards().isBlank())) {
            return text;
        }

        return MoneyTextReplacer.replace(text, state.getMoney(), state.getShards());
    }
}
