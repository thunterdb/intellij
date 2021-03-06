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
package com.google.idea.blaze.java.sync;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.google.idea.blaze.base.ideinfo.ArtifactLocation;
import com.google.idea.blaze.base.model.primitives.Label;
import com.google.idea.blaze.base.scope.BlazeContext;
import com.google.idea.blaze.base.scope.output.PerformanceWarning;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Detects and reports duplicate sources
 */
public class DuplicateSourceDetector {
  Multimap<ArtifactLocation, Label> artifacts = ArrayListMultimap.create();

  public void add(Label label, ArtifactLocation artifactLocation) {
    artifacts.put(artifactLocation, label);
  }

  static class Duplicate {
    final ArtifactLocation artifactLocation;
    final Collection<Label> labels;
    public Duplicate(ArtifactLocation artifactLocation, Collection<Label> labels) {
      this.artifactLocation = artifactLocation;
      this.labels = labels;
    }
  }

  public void reportDuplicates(BlazeContext context) {
    List<Duplicate> duplicates = Lists.newArrayList();
    for (ArtifactLocation key : artifacts.keySet()) {
      Collection<Label> labels = artifacts.get(key);
      if (labels.size() > 1) {

        // Workaround for aspect bug. Can be removed after the next blaze release, as of May 27 2016
        Set<Label> labelSet = Sets.newHashSet(labels);
        if (labelSet.size() > 1) {
          duplicates.add(new Duplicate(key, labelSet));
        }
      }
    }

    if (duplicates.isEmpty()) {
      return;
    }

    Collections.sort(duplicates, (lhs, rhs) -> lhs.artifactLocation.getRelativePath().compareTo(rhs.artifactLocation.getRelativePath()));

    context.output(new PerformanceWarning("Duplicate sources detected:"));
    for (Duplicate duplicate : duplicates) {
      ArtifactLocation artifactLocation = duplicate.artifactLocation;
      context.output(new PerformanceWarning("  Source: " + artifactLocation.getRelativePath()));
      context.output(new PerformanceWarning("  Consumed by rules:"));
      for (Label label : duplicate.labels) {
        context.output(new PerformanceWarning("    " + label));
      }
      context.output(new PerformanceWarning("")); // Newline
    }
  }
}
