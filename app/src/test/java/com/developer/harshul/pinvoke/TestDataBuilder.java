package com.developer.harshul.pinvoke;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class TestDataBuilder {
    private List<TestCard> cards = new ArrayList<>();

    public static class TestCard {
        private String name;
        private int daysFromNow;

        public TestCard(String name, int daysFromNow) {
            this.name = name;
            this.daysFromNow = daysFromNow;
        }

        public JSONObject toJsonObject() throws JSONException {
            JSONObject card = new JSONObject();
            card.put("name", name);

            Calendar dueDate = Calendar.getInstance();
            dueDate.add(Calendar.DAY_OF_MONTH, daysFromNow);
            card.put("dueDate", dueDate.getTimeInMillis());

            return card;
        }
    }

    public TestDataBuilder addCard(String name, int daysFromNow) {
        cards.add(new TestCard(name, daysFromNow));
        return this;
    }

    public TestDataBuilder addOverdueCard(String name, int daysOverdue) {
        cards.add(new TestCard(name, -Math.abs(daysOverdue)));
        return this;
    }

    public TestDataBuilder addDueTodayCard(String name) {
        cards.add(new TestCard(name, 0));
        return this;
    }

    public TestDataBuilder addDueSoonCard(String name) {
        cards.add(new TestCard(name, 2)); // Due in 2 days
        return this;
    }

    public TestDataBuilder addMultipleCards(int count, String namePrefix) {
        for (int i = 0; i < count; i++) {
            cards.add(new TestCard(namePrefix + " " + (i + 1), i + 1));
        }
        return this;
    }

    public JSONArray build() throws JSONException {
        JSONArray array = new JSONArray();
        for (TestCard card : cards) {
            array.put(card.toJsonObject());
        }
        return array;
    }

    public String buildAsString() throws JSONException {
        return build().toString();
    }

    // Predefined common scenarios
    public static TestDataBuilder mixedScenario() {
        return new TestDataBuilder()
                .addOverdueCard("Overdue Card", 5)
                .addDueTodayCard("Due Today")
                .addDueSoonCard("Due Soon")
                .addCard("Future Card", 15);
    }

    public static TestDataBuilder allOverdueScenario() {
        return new TestDataBuilder()
                .addOverdueCard("Very Overdue", 30)
                .addOverdueCard("Moderately Overdue", 10)
                .addOverdueCard("Recently Overdue", 1);
    }

    public static TestDataBuilder performanceTestScenario() {
        TestDataBuilder builder = new TestDataBuilder();
        for (int i = 0; i < 50; i++) {
            builder.addCard("Performance Card " + i, i % 30);
        }
        return builder;
    }
}