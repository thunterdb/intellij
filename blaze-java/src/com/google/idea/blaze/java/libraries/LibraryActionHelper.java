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
package com.google.idea.blaze.java.libraries;

import com.google.idea.blaze.base.model.BlazeProjectData;
import com.google.idea.blaze.base.sync.data.BlazeProjectDataManager;
import com.google.idea.blaze.java.sync.model.BlazeJavaSyncData;
import com.google.idea.blaze.java.sync.model.BlazeLibrary;
import com.google.idea.blaze.java.sync.model.LibraryKey;
import com.intellij.ide.projectView.impl.nodes.NamedLibraryElementNode;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.impl.libraries.ProjectLibraryTable;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.roots.libraries.LibraryTable;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.pom.Navigatable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class LibraryActionHelper {

  static BlazeLibrary findLibraryFromIntellijLibrary(Project project, Library library) {
    LibraryKey libraryKey = LibraryKey.fromIntelliJLibrary(library);
    BlazeProjectData projectData = BlazeProjectDataManager.getInstance(project).getBlazeProjectData();
    if (projectData == null) {
      return null;
    }
    BlazeJavaSyncData syncData = projectData.syncState.get(BlazeJavaSyncData.class);
    if (syncData == null) {
      Messages.showErrorDialog(project, "Project isn't synced. Please resync project.", "Error");
      return null;
    }

    return syncData.importResult.libraries.get(libraryKey);
  }

  @Nullable
  public static Library findLibraryForAction(@NotNull AnActionEvent e) {
    Project project = e.getProject();
    if (project != null) {
      NamedLibraryElementNode node = findLibraryNode(e.getDataContext());
      if (node != null) {
        String libraryName = node.getName();
        if (StringUtil.isNotEmpty(libraryName)) {
          LibraryTable libraryTable = ProjectLibraryTable.getInstance(project);
          return libraryTable.getLibraryByName(libraryName);
        }
      }
    }
    return null;
  }

  @Nullable
  private static NamedLibraryElementNode findLibraryNode(@NotNull DataContext dataContext) {
    Navigatable[] navigatables = CommonDataKeys.NAVIGATABLE_ARRAY.getData(dataContext);
    if (navigatables != null && navigatables.length == 1) {
      Navigatable navigatable = navigatables[0];
      if (navigatable instanceof NamedLibraryElementNode) {
        return (NamedLibraryElementNode)navigatable;
      }
    }
    return null;
  }
}
