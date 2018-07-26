package io.arcblock.task.usersearcher.query;

import android.content.Context;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.arcblock.task.usersearcher.list.UserListItem;

import static io.arcblock.task.usersearcher.ScrollingActivity.STATUS_CODE_GENERAL;
import static io.arcblock.task.usersearcher.ScrollingActivity.STATUS_CODE_NETWORKING;

public class RequestHandler {

    private static final String TAG = RequestHandler.class.getSimpleName();

    private static final String RESPONSE_HEADER_LINK_KEY = "link";
    private static final String RESPONSE_HEADER_LINK_NEXT_KEY = "rel=\"next\"";

    private final RequestQueue mQueue;

    public interface QueryCallback {
        void onStartSearch();
        void onValidResponse(List<UserListItem> list);
        void onError(int errorCode);
        void onNextLink(String url);
    }

    private final QueryCallback mCallback;

    public RequestHandler(Context context, QueryCallback callback) {
        mCallback = callback;
        mQueue = Volley.newRequestQueue(context);
    }

    public void runSearch(final String url) {
        if (url == null || url.isEmpty()) {
            return;
        }

        Log.d(TAG, "searching " + url);
        JsonObjectRequest postRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        JsonResponseHandler respHandler = new JsonResponseHandler(response);
                        List<UserListItem> ret = respHandler.parse();

                        mCallback.onValidResponse(ret);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (error.networkResponse == null) {
                            mCallback.onError(STATUS_CODE_NETWORKING);
                            return;
                        }

                        int code = error.networkResponse.statusCode;
                        String errorMsg = new String(error.networkResponse.data);
                        Log.e(TAG, "Failed to send msg: " + errorMsg + " code " + code);
                        mCallback.onError(STATUS_CODE_GENERAL);
                    }
                }) {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Accept", "application/vnd.github.v3.text-match+json");
                return headers;
            }

            @Override
            protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
                handleRespHeaders(response.headers);
                return super.parseNetworkResponse(response);
            }
        };
        mQueue.add(postRequest);
        mCallback.onStartSearch();
    }

    private void handleRespHeaders(Map<String, String> headers) {
        String link = headers.get(RESPONSE_HEADER_LINK_KEY);

        if (link == null || link.isEmpty()) {
            return;
        }

        String[] links = link.split(",");
        if (links.length == 0) {
            return;
        }

        for (String pair : links) {
            String[] keyValue = pair.split(";");
            if (keyValue.length > 1 && RESPONSE_HEADER_LINK_NEXT_KEY.equals(keyValue[1].trim())) {
                String nextLink = keyValue[0].replaceAll("[<> ]", "");
                if (!nextLink.isEmpty()) {
                    mCallback.onNextLink(nextLink);
                    break;
                }
            }
        }
    }
}
