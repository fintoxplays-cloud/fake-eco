package com.fakemoney.scoreboard.client;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

public final class FakeEconomyScreen extends Screen {
    private TextFieldWidget moneyField;
    private TextFieldWidget shardsField;
    private TextFieldWidget paymentField;
    private Text status = Text.empty();

    public FakeEconomyScreen() {
        super(Text.literal("Fake Economy"));
    }

    @Override
    protected void init() {
        int center = width / 2;
        int y = height / 2 - 100;

        moneyField = new TextFieldWidget(textRenderer, center - 110, y + 28, 220, 20, Text.literal("Fake Money"));
        moneyField.setMaxLength(512);
        moneyField.setText(FakeEconomyState.getInstance().getMoney());
        addDrawableChild(moneyField);

        shardsField = new TextFieldWidget(textRenderer, center - 110, y + 76, 220, 20, Text.literal("Fake Shards"));
        shardsField.setMaxLength(512);
        shardsField.setText(FakeEconomyState.getInstance().getShards());
        addDrawableChild(shardsField);

        paymentField = new TextFieldWidget(textRenderer, center - 110, y + 124, 220, 20, Text.literal("Payment amount"));
        paymentField.setMaxLength(512);
        addDrawableChild(paymentField);

        addDrawableChild(ButtonWidget.builder(Text.literal("Apply"), button -> applyValues())
                .dimensions(center - 110, y + 154, 105, 20)
                .build());

        addDrawableChild(ButtonWidget.builder(Text.literal("Simulate Payment"), button -> simulatePayment())
                .dimensions(center + 5, y + 154, 115, 20)
                .build());

        addDrawableChild(ButtonWidget.builder(Text.literal("Reset"), button -> {
                    FakeEconomyState.getInstance().reset();
                    moneyField.setText("");
                    shardsField.setText("");
                    paymentField.setText("");
                    status = Text.literal("Fake values reset.");
                })
                .dimensions(center - 110, y + 180, 105, 20)
                .build());

        addDrawableChild(ButtonWidget.builder(Text.literal("Done"), button -> close())
                .dimensions(center + 5, y + 180, 115, 20)
                .build());

        setInitialFocus(moneyField);
    }

    private void applyValues() {
        try {
            String money = moneyField.getText().trim();
            String shards = shardsField.getText().trim();

            if (!money.isEmpty()) {
                FakeEconomyState.getInstance().setMoney(MoneyParser.formatNumber(MoneyParser.toNumber(money)));
            } else {
                FakeEconomyState.getInstance().setMoney("");
            }

            if (!shards.isEmpty()) {
                FakeEconomyState.getInstance().setShards(MoneyParser.formatNumber(MoneyParser.toNumber(shards)));
            } else {
                FakeEconomyState.getInstance().setShards("");
            }

            moneyField.setText(FakeEconomyState.getInstance().getMoney());
            shardsField.setText(FakeEconomyState.getInstance().getShards());
            status = Text.literal("Fake values applied.");
        } catch (MoneyParser.ParseException e) {
            status = Text.literal(e.getMessage());
        }
    }

    private void simulatePayment() {
        try {
            String amount = paymentField.getText().trim();
            if (amount.isEmpty()) {
                status = Text.literal("Enter a payment amount.");
                return;
            }

            BigDecimalCheck.parse(amount);
            if (FakeEconomyState.getInstance().simulatePayment(amount)) {
                moneyField.setText(FakeEconomyState.getInstance().getMoney());
                status = Text.literal("Payment simulated locally: " + amount);
            } else {
                status = Text.literal("Payment failed: not enough fake money or no fake balance.");
            }
        } catch (MoneyParser.ParseException e) {
            status = Text.literal(e.getMessage());
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context, mouseX, mouseY, delta);

        int center = width / 2;
        int y = height / 2 - 100;

        context.drawCenteredTextWithShadow(textRenderer, title, center, y, 0xFFFFFF);
        context.drawTextWithShadow(textRenderer, Text.literal("Fake Money"), center - 110, y + 16, 0xAFAFAF);
        context.drawTextWithShadow(textRenderer, Text.literal("Fake Shards"), center - 110, y + 64, 0xAFAFAF);
        context.drawTextWithShadow(textRenderer, Text.literal("Payment Amount"), center - 110, y + 112, 0xAFAFAF);

        context.drawCenteredTextWithShadow(textRenderer, status, center, y + 208, 0xAAAAAA);

        context.drawCenteredTextWithShadow(
                textRenderer,
                Text.literal("Open this screen with your keybind in Options > Controls."),
                center,
                y + 230,
                0x777777
        );

        super.render(context, mouseX, mouseY, delta);
    }

    private static final class BigDecimalCheck {
        static void parse(String value) {
            MoneyParser.toNumber(value);
        }
    }
}
