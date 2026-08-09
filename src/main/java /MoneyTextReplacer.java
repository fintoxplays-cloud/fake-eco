package com.fakemoney.scoreboard.client;

import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Replaces only money/shard values on the sidebar. All changes are client-side.
 */
public final class MoneyTextReplacer {
    private static final Pattern DOLLAR_MONEY =
            Pattern.compile("\\$\\s?\\d[\\d,]*(?:\\.\\d+)?\\s?[A-Za-z]{0,2}");
    private static final Pattern SUFFIXED_MONEY =
            Pattern.compile("\\b\\d[\\d,]*(?:\\.\\d+)?(?:K|M|B|T|Qa|Qi|Sx|Sp|Oc|No|Dc)\\b", Pattern.CASE_INSENSITIVE);
    private static final Pattern PLAIN_LARGE_NUMBER =
            Pattern.compile("\\b\\d[\\d,]{3,}\\b");
    private static final Pattern MONEY_CONTEXT_LABEL =
            Pattern.compile("(?i)\\b(money|balance|coins|cash|bank)\\b");

    private static final Pattern SHARD_CONTEXT =
            Pattern.compile("(?i)\\b(shards?|shard)\\b");
    private static final Pattern SHARD_NUMBER =
            Pattern.compile("\\b\\d[\\d,]*(?:\\.\\d+)?\\b");

    private MoneyTextReplacer() {}

    public static Text replace(Text original, String fakeMoney, String fakeShards) {
        String fullLine = original.getString();
        if (fullLine.isEmpty()) return original;

        boolean moneyContext = MONEY_CONTEXT_LABEL.matcher(fullLine).find();
        boolean shardContext = SHARD_CONTEXT.matcher(fullLine).find();

        if ((!moneyContext && !shardContext) && fakeMoney != null && !fakeMoney.isBlank()) {
            // Dollar/suffixed formats are strong enough signals on their own.
            if (!DOLLAR_MONEY.matcher(fullLine).find() && !SUFFIXED_MONEY.matcher(fullLine).find()) {
                return original;
            }
        }

        List<MutableText> runs = new ArrayList<>();
        original.visit((style, string) -> {
            runs.add(Text.literal(string).setStyle(style));
            return Optional.<Object>empty();
        }, Style.EMPTY);

        if (runs.isEmpty()) return original;

        MutableText result = Text.literal("");
        boolean replaced = false;

        for (MutableText run : runs) {
            String text = run.getString();

            if (!replaced && fakeMoney != null && !fakeMoney.isBlank()) {
                String match = findMoney(text, moneyContext);
                if (match != null) {
                    result.append(Text.literal(text.replaceFirst(
                            Pattern.quote(match),
                            Matcher.quoteReplacement(preserveDollar(match, fakeMoney))
                    )).setStyle(run.getStyle()));
                    replaced = true;
                    continue;
                }
            }

            if (!replaced && fakeShards != null && !fakeShards.isBlank() && shardContext) {
                Matcher shard = SHARD_NUMBER.matcher(text);
                if (shard.find()) {
                    String newText = text.substring(0, shard.start())
                            + fakeShards
                            + text.substring(shard.end());
                    result.append(Text.literal(newText).setStyle(run.getStyle()));
                    replaced = true;
                    continue;
                }
            }

            result.append(run);
        }

        return replaced ? result : original;
    }

    private static String findMoney(String text, boolean context) {
        Matcher dollar = DOLLAR_MONEY.matcher(text);
        if (dollar.find()) return dollar.group();

        Matcher suffix = SUFFIXED_MONEY.matcher(text);
        if (suffix.find()) return suffix.group();

        if (context) {
            Matcher plain = PLAIN_LARGE_NUMBER.matcher(text);
            if (plain.find()) return plain.group();
        }
        return null;
    }

    private static String preserveDollar(String match, String fake) {
        return match.trim().startsWith("$") ? "$" + fake : fake;
    }
}
