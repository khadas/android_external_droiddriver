/*
 * Copyright (C) 2013 DroidDriver committers
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.android.droiddriver.uiautomation.base;

import android.app.Instrumentation;
import android.app.UiAutomation;
import android.content.Context;
import android.os.SystemClock;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.view.accessibility.AccessibilityNodeInfo;

import com.google.android.droiddriver.base.BaseDroidDriver;
import com.google.android.droiddriver.exceptions.TimeoutException;
import com.google.android.droiddriver.util.Logs;

/**
 * Base implementation of DroidDriver that gets attributes via the Accessibility
 * API.
 */
public abstract class BaseUiAutomationDriver<E extends BaseUiAutomationElement<E>> extends
    BaseDroidDriver {
  // TODO: magic const from UiAutomator, but may not be useful
  /**
   * This value has the greatest bearing on the appearance of test execution
   * speeds. This value is used as the minimum time to wait before considering
   * the UI idle after each action.
   */
  private static final long QUIET_TIME_TO_BE_CONSIDERD_IDLE_STATE = 500;// ms

  private final BaseUiAutomationContext<E> context;
  private final UiAutomationUiDevice uiDevice;

  protected BaseUiAutomationDriver(Instrumentation instrumentation) {
    this.context = newContext(instrumentation);
    this.uiDevice = new UiAutomationUiDevice(context);
  }

  protected abstract BaseUiAutomationContext<E> newContext(Instrumentation instrumentation);

  @Override
  protected E getNewRootElement() {
    return context.getUiElement(getRootNode(), null /* parent */);
  }

  @Override
  protected BaseUiAutomationContext<E> getContext() {
    return context;
  }

  private AccessibilityNodeInfo getRootNode() {
    final long timeoutMillis = getPoller().getTimeoutMillis();
    context.callUiAutomation(new UiAutomationCallable<Void>() {
      @Override
      public Void call(UiAutomation uiAutomation) {
        try {
          uiAutomation.waitForIdle(QUIET_TIME_TO_BE_CONSIDERD_IDLE_STATE, timeoutMillis);
          return null;
        } catch (java.util.concurrent.TimeoutException e) {
          throw new TimeoutException(e);
        }
      }
    });

    long end = SystemClock.uptimeMillis() + timeoutMillis;
    while (true) {
      AccessibilityNodeInfo root =
          context.callUiAutomation(new UiAutomationCallable<AccessibilityNodeInfo>() {
            @Override
            public AccessibilityNodeInfo call(UiAutomation uiAutomation) {
              return uiAutomation.getRootInActiveWindow();
            }
          });
      if (root != null) {
        return root;
      }
      long remainingMillis = end - SystemClock.uptimeMillis();
      if (remainingMillis < 0) {
        throw new TimeoutException(
            String.format("Timed out after %d milliseconds waiting for root AccessibilityNodeInfo",
                timeoutMillis));
      }
      SystemClock.sleep(Math.min(250, remainingMillis));
    }
  }

  /**
   * Some widgets fail to trigger some AccessibilityEvent's after actions,
   * resulting in stale AccessibilityNodeInfo's. As a work-around, force to
   * clear the AccessibilityNodeInfoCache.
   */
  public void clearAccessibilityNodeInfoCache() {
    Logs.call(this, "clearAccessibilityNodeInfoCache");
    uiDevice.sleep();
    uiDevice.wakeUp();
  }

  /**
   * {@link #clearAccessibilityNodeInfoCache} causes the screen to blink. This
   * method clears the cache without blinking by employing an implementation
   * detail of AccessibilityNodeInfoCache. This is a hack; use it at your own
   * discretion.
   */
  public void clearAccessibilityNodeInfoCacheHack() {
    Logs.call(this, "clearAccessibilityNodeInfoCacheHack");
    AccessibilityManager accessibilityManager =
        (AccessibilityManager) context.getInstrumentation().getTargetContext()
            .getSystemService(Context.ACCESSIBILITY_SERVICE);
    accessibilityManager.sendAccessibilityEvent(AccessibilityEvent
        .obtain(AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED));
  }

  @Override
  public UiAutomationUiDevice getUiDevice() {
    return uiDevice;
  }
}