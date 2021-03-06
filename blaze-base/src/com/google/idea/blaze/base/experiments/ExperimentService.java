/*
 * Copyright 2016 The Bazel Authors. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.idea.blaze.base.experiments;

import com.intellij.openapi.components.ServiceManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Reads experiments.
 */
public interface ExperimentService {

  static ExperimentService getInstance() {
    return ServiceManager.getService(ExperimentService.class);
  }

  /**
   * Returns an experiment if it exists, else defaultValue
   */
  boolean getExperiment(@NotNull String key, boolean defaultValue);

  /**
   * Returns a string-valued experiment if it exists, else defaultValue.
   */
  String getExperimentString(@NotNull String key, @Nullable String defaultValue);

  /**
   * Returns an int-valued experiment if it exists, else defaultValue.
   */
  int getExperimentInt(@NotNull String key, int defaultValue);

  /**
   * Reloads all experiments.
   */
  void reloadExperiments();

  /**
   * Starts an experiment scope. During an experiment scope,
   * experiments won't be reloaded.
   */
  void startExperimentScope();

  /**
   * Ends an experiment scope.
   */
  void endExperimentScope();
}
