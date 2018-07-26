package io.arcblock.task.usersearcher.query;


import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import io.arcblock.task.usersearcher.list.UserListItem;

public class JsonResponseHandler {

    private static String TEXT_MATCHES_KEY = "text_matches";

    private static String TEXT_MATCHES_PROPERTY_KEY = "property";
    private static String TEXT_MATCHES_PROPERTY_EMAIL = "email";
    private static String TEXT_MATCHES_PROPERTY_LOGIN = "login";
    private static String TEXT_MATCHES_PROPERTY_NAME = "name";

    private static String TEXT_MATCHES_FRAGMENT_KEY = "fragment";

    private static String ITEMS_KEY = "items";
    private static String LOGIN_KEY = "login";
    private static String AVATAR_URL_KEY = "avatar_url";

    private JSONObject mInputJson;

    JsonResponseHandler(JSONObject input) {
        mInputJson = input;
    }

    List<UserListItem> parse() {
        List<UserListItem> ret = new ArrayList<>();

        JSONArray items = mInputJson.optJSONArray(ITEMS_KEY);
        if (items == null) {
            return ret;
        }

        int count = items.length();
        for (int i = 0; i < count; ++i) {
            JSONObject item = items.optJSONObject(i);
            if (item == null) {
                continue;
            }
            UserListItem listItem = new UserListItem();
            listItem.login = item.optString(LOGIN_KEY);
            listItem.avatarUrl = item.optString(AVATAR_URL_KEY);

            getTextInfo(item, listItem);
            ret.add(listItem);
        }

        return ret;
    }

    private void getTextInfo(JSONObject jsonObj, UserListItem listItem) {
        if (!jsonObj.has(TEXT_MATCHES_KEY)) {
            return;
        }

        JSONArray matches = jsonObj.optJSONArray(TEXT_MATCHES_KEY);
        int count = matches.length();
        for (int i = 0; i < count; ++i) {
            JSONObject match = matches.optJSONObject(i);
            String property = match.optString(TEXT_MATCHES_PROPERTY_KEY);

            if (TEXT_MATCHES_PROPERTY_EMAIL.equals(property)) {
                listItem.email = match.optString(TEXT_MATCHES_FRAGMENT_KEY);
            } else if (TEXT_MATCHES_PROPERTY_LOGIN.equals(property)) {
                listItem.login = match.optString(TEXT_MATCHES_FRAGMENT_KEY);
            } else if (TEXT_MATCHES_PROPERTY_NAME.equals(property)) {
                listItem.name = match.optString(TEXT_MATCHES_FRAGMENT_KEY);
            }
        }
    }
}
