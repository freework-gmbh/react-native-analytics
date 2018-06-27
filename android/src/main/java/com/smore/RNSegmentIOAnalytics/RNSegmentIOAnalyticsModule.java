package com.smore.RNSegmentIOAnalytics;

import org.json.*;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.ReadableMapKeySetIterator;
import com.facebook.react.bridge.ReadableNativeMap;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeMap;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableNativeArray;
import java.util.Iterator;
import com.facebook.react.bridge.ReadableType;
import com.segment.analytics.Analytics;
import com.segment.analytics.Analytics.Builder;
import com.segment.analytics.Properties;
import com.segment.analytics.Traits;
import com.segment.analytics.Options;
import android.util.Log;
import android.content.Context;
import com.segment.analytics.android.integrations.amplitude.AmplitudeIntegration;

public class RNSegmentIOAnalyticsModule extends ReactContextBaseJavaModule {
  private static Analytics mAnalytics = null;
  private Boolean mEnabled = true;
  private Boolean mDebug = false;

  @Override
  public String getName() {
    return "RNSegmentIOAnalytics";
  }

  private void log(String message) {
    Log.d("RNSegmentIOAnalytics", message);
  }

  public RNSegmentIOAnalyticsModule(ReactApplicationContext reactContext) {
    super(reactContext);
  }

  /*
   https://segment.com/docs/libraries/android/#identify
   */
  @ReactMethod
  public void setup(String writeKey, Integer flushAt, Boolean shouldUseLocationServices) {
    if (mAnalytics == null) {
      Context context = getReactApplicationContext().getApplicationContext();
      Builder builder = new Analytics.Builder(context, writeKey)
        .trackApplicationLifecycleEvents();
      builder.flushQueueSize(flushAt);

      if (mDebug) {
        builder.logLevel(Analytics.LogLevel.DEBUG);
      }

      mAnalytics = builder.use(AmplitudeIntegration.FACTORY).build();
    } else {
      log("Segment Analytics already initialized. Refusing to re-initialize.");
    }
  }

  /*
   https://segment.com/docs/libraries/android/#identify
   */
  @ReactMethod
  public void identify(String userId, JSONObject traits) {
    if (!mEnabled) {
      return;
    }
    mAnalytics.identify(userId, toTraits(traits), toOptions(null));
  }

  /*
   https://segment.com/docs/libraries/android/#track
   */
  @ReactMethod
  public void track(String trackText, JSONObject properties) {
    if (!mEnabled) {
      return;
    }
    mAnalytics.track(trackText, toProperties(properties));
  }

  /*
   https://segment.com/docs/libraries/android/#screen
   */
  @ReactMethod
  public void screen(String screenName, JSONObject properties) {
    if (!mEnabled) {
      return;
    }
    mAnalytics.screen(null, screenName, toProperties(properties));
  }

  /*
   https://segment.com/docs/libraries/android/#flushing
   */
  @ReactMethod
  public void flush() {
    mAnalytics.flush();
  }

  /*
   https://segment.com/docs/libraries/android/#reset
   */
  @ReactMethod
  public void reset() {
    mAnalytics.reset();
  }

  /*
   https://segment.com/docs/libraries/android/#logging
   */
  @ReactMethod
  public void debug(Boolean isEnabled) {
    if (isEnabled == mDebug) {
      return;
    } else if (mAnalytics == null) {
      mDebug = isEnabled;
    } else {
      log("On Android, debug level may not be changed after calling setup");
    }
  }

  /*
   https://segment.com/docs/libraries/android/#opt-out
   */
  @ReactMethod
  public void disable() {
    mEnabled = false;
  }

  /*
   https://segment.com/docs/libraries/android/#opt-out
   */
  @ReactMethod
  public void enable() {
    mEnabled = true;
  }


  private WritableMap convertJsonToMap(JSONObject jsonObject) throws JSONException {
      WritableMap map = new WritableNativeMap();

      Iterator<String> iterator = jsonObject.keys();
      while (iterator.hasNext()) {
          String key = iterator.next();
          Object value = jsonObject.get(key);
          if (value instanceof JSONObject) {
              map.putMap(key, convertJsonToMap((JSONObject) value));
          } else if (value instanceof  JSONArray) {
              map.putArray(key, convertJsonToArray((JSONArray) value));
          } else if (value instanceof  Boolean) {
              map.putBoolean(key, (Boolean) value);
          } else if (value instanceof  Integer) {
              map.putInt(key, (Integer) value);
          } else if (value instanceof  Double) {
              map.putDouble(key, (Double) value);
          } else if (value == null)  {
              map.putNull(key);
          } else if (value instanceof String)  {
              map.putString(key, (String) value);
          } else {
              map.putString(key, value.toString());
          }
      }
      return map;
  }


  private WritableArray convertJsonToArray(JSONArray jsonArray) throws JSONException {
      WritableArray array = new WritableNativeArray();

      for (int i = 0; i < jsonArray.length(); i++) {
          Object value = jsonArray.get(i);
          if (value instanceof JSONObject) {
              array.pushMap(convertJsonToMap((JSONObject) value));
          } else if (value instanceof  JSONArray) {
              array.pushArray(convertJsonToArray((JSONArray) value));
          } else if (value instanceof  Boolean) {
              array.pushBoolean((Boolean) value);
          } else if (value instanceof  Integer) {
              array.pushInt((Integer) value);
          } else if (value instanceof  Double) {
              array.pushDouble((Double) value);
          } else if (value == null)  {
              array.pushNull();
          } else if (value instanceof String)  {
              array.pushString((String) value);

          } else {
              array.pushString(value.toString());
          }
      }
      return array;
  }

  private Properties toProperties (JSONObject jsonObject) throws JSONException {
    if (jsonObject == null) {
      return new Properties();
    }
    Properties props = new Properties();

    Iterator<String> iterator = jsonObject.keys();
    while (iterator.hasNext()) {
        String key = iterator.next();
        Object value = jsonObject.get(key);

        if (value instanceof JSONObject) {
          props.putValue(key, convertJsonToMap((JSONObject) value));
        } else if (value instanceof  JSONArray) {
          props.putValue(key, convertJsonToArray((JSONArray) value));
        } else if (value instanceof  Boolean) {
          props.putValue(key, (Boolean) value);
        } else if (value instanceof  Integer) {
          props.putValue(key, (Integer) value);
        } else if (value instanceof  Double) {
          props.putValue(key, (Double) value);
        } else if (value == null)  {
          props.putValue(key, null);
        } else if (value instanceof String)  {
          props.putValue(key, (String) value);
        } else {
          props.putValue(key, value.toString());
        }
    }
    return props;
  }

  private Traits toTraits (JSONObject jsonObject) throws JSONException {
    if (jsonObject == null) {
      return new Traits();
    }
    Traits traits = new Traits();

    Iterator<String> iterator = jsonObject.keys();
    while (iterator.hasNext()) {
        String key = iterator.next();
        Object value = jsonObject.get(key);

        if (value instanceof JSONObject) {
          traits.putValue(key, convertJsonToMap((JSONObject) value));
        } else if (value instanceof  JSONArray) {
          traits.putValue(key, convertJsonToArray((JSONArray) value));
        } else if (value instanceof  Boolean) {
          traits.putValue(key, (Boolean) value);
        } else if (value instanceof  Integer) {
          traits.putValue(key, (Integer) value);
        } else if (value instanceof  Double) {
          traits.putValue(key, (Double) value);
        } else if (value == null)  {
          traits.putValue(key, null);
        } else if (value instanceof String)  {
          traits.putValue(key, (String) value);
        } else {
          traits.putValue(key, value.toString());
        }
    }
    return traits;
  }

  private Options toOptions (ReadableMap map) {
    return new Options();
  }
}
