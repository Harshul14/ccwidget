package com.developer.harshul.ccwidget;

import org.json.JSONArray;
import org.json.JSONObject;
import java.util.concurrent.TimeUnit;

public class TestUtils {

    public static final int VALID_WIDGET_ID = 123;
    public static final String TEST_CARD_NAME = "Test Credit Card";

    public static JSONArray createValidCardData() throws Exception {
        JSONArray cardsArray = new JSONArray();
        JSONObject cardObj = new JSONObject();
        cardObj.put("name", TEST_CARD_NAME);
        cardObj.put("dueDate", System.currentTimeMillis() + TimeUnit.DAYS.toMillis(15));
        cardsArray.put(cardObj);
        return cardsArray;
    }

    public static JSONArray createMultipleCardsData(int count) throws Exception {
        JSONArray cardsArray = new JSONArray();
        for (int i = 0; i < count; i++) {
            JSONObject cardObj = new JSONObject();
            cardObj.put("name", "Card " + (i + 1));
            cardObj.put("dueDate", System.currentTimeMillis() + TimeUnit.DAYS.toMillis(i + 1));
            cardsArray.put(cardObj);
        }
        return cardsArray;
    }

    public static JSONArray createOverdueCardData() throws Exception {
        JSONArray cardsArray = new JSONArray();
        JSONObject cardObj = new JSONObject();
        cardObj.put("name", "Overdue Card");
        cardObj.put("dueDate", System.currentTimeMillis() - TimeUnit.DAYS.toMillis(5));
        cardsArray.put(cardObj);
        return cardsArray;
    }

    public static JSONArray createTodayDueCardData() throws Exception {
        JSONArray cardsArray = new JSONArray();
        JSONObject cardObj = new JSONObject();
        cardObj.put("name", "Due Today");
        cardObj.put("dueDate", System.currentTimeMillis());
        cardsArray.put(cardObj);
        return cardsArray;
    }

    public static String createInvalidJsonString() {
        return "{ invalid json structure";
    }

    public static String createLargeJsonString() {
        StringBuilder large = new StringBuilder("[");
        for (int i = 0; i < 1000; i++) {
            if (i > 0) large.append(",");
            large.append("{\"name\":\"Card").append(i).append("\",\"dueDate\":")
                    .append(System.currentTimeMillis() + TimeUnit.DAYS.toMillis(i))
                    .append("}");
        }
        large.append("]");
        return large.toString();
    }
}