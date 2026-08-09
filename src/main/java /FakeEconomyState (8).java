package com.fakemoney.scoreboard.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import net.minecraft.client.MinecraftClient;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Client-only fake economy state. Nothing in this class is sent to a server.
 */
public final class FakeEconomyState {
    private static final FakeEconomyState INSTANCE = new FakeEconomyState();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private String money = "";
    private String shards = "";

    private FakeEconomyState() {}

    public static FakeEconomyState getInstance() {
        return INSTANCE;
    }

    public synchronized String getMoney() {
        return money;
    }

    public synchronized String getShards() {
        return shards;
    }

    public synchronized void setMoney(String value) {
        money = value == null ? "" : value.trim();
        save();
    }

    public synchronized void setShards(String value) {
        shards = value == null ? "" : value.trim();
        save();
    }

    public synchronized void setValues(String newMoney, String newShards) {
        money = newMoney == null ? "" : newMoney.trim();
        shards = newShards == null ? "" : newShards.trim();
        save();
    }

    public synchronized void reset() {
        money = "";
        shards = "";
        save();
    }

    /**
     * Simulates a payment locally by subtracting the requested amount from
     * the configured fake money balance. No network packet is sent.
     */
    public synchronized boolean simulatePayment(String amount) {
        if (money.isBlank()) {
            return false;
        }

        BigDecimal current = MoneyParser.toNumber(money);
        BigDecimal payment = MoneyParser.toNumber(amount);

        if (payment.signum() < 0 || current.compareTo(payment) < 0) {
            return false;
        }

        money = MoneyParser.formatNumber(current.subtract(payment));
        save();
        return true;
    }

    public synchronized void load(MinecraftClient client) {
        Path path = getPath(client);
        try {
            if (!Files.exists(path)) {
                return;
            }

            try (Reader reader = Files.newBufferedReader(path)) {
                JsonObject json = GSON.fromJson(reader, JsonObject.class);
                if (json == null) return;

                if (json.has("money")) {
                    money = json.get("money").getAsString();
                }
                if (json.has("shards")) {
                    shards = json.get("shards").getAsString();
                }
            }
        } catch (Exception ignored) {
            // A broken config should never prevent Minecraft from starting.
        }
    }

    private synchronized void save() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null) return;

        Path path = getPath(client);
        try {
            Files.createDirectories(path.getParent());

            JsonObject json = new JsonObject();
            json.addProperty("money", money);
            json.addProperty("shards", shards);

            try (Writer writer = Files.newBufferedWriter(path)) {
                GSON.toJson(json, writer);
            }
        } catch (IOException ignored) {
            // Config persistence is best-effort.
        }
    }

    private static Path getPath(MinecraftClient client) {
        return client.runDirectory.toPath()
                .resolve("config")
                .resolve("fake-economy.json");
    }
}
