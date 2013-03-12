/*
 * Copyright (C) 2013 UiDriver committers
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

package com.google.android.uidriver.exceptions;

import com.google.android.uidriver.Poller.ConditionChecker;

/**
 * Thrown to indicate condition not met. Used in {@link ConditionChecker}.
 */
public class UnsatisfiedConditionException extends UiDriverException {
  public UnsatisfiedConditionException(String message) {
    super(message);
  }

  public UnsatisfiedConditionException(String message, Throwable cause) {
    super(message, cause);
  }
}