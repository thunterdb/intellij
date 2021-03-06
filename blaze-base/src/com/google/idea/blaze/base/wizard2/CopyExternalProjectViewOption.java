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
package com.google.idea.blaze.base.wizard2;

import com.google.idea.blaze.base.model.primitives.WorkspacePath;
import com.google.idea.blaze.base.projectview.ProjectViewStorageManager;
import com.google.idea.blaze.base.ui.BlazeValidationResult;
import com.google.idea.blaze.base.ui.UiUtil;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserDialog;
import com.intellij.openapi.fileChooser.FileChooserFactory;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.components.panels.HorizontalLayout;

import javax.annotation.Nullable;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

class CopyExternalProjectViewOption implements BlazeSelectProjectViewOption {
  private static final String LAST_WORKSPACE_PATH = "copy-external.last-project-view-path";

  final BlazeWizardUserSettings userSettings;
  final JComponent component;
  final JTextField projectViewPathField;

  CopyExternalProjectViewOption(BlazeNewProjectBuilder builder) {
    this.userSettings = builder.getUserSettings();

    String defaultWorkspacePath = userSettings.get(LAST_WORKSPACE_PATH, "");

    JPanel panel = new JPanel(new HorizontalLayout(10));
    JLabel label = new JLabel("Project view:");
    UiUtil.setPreferredWidth(label, HEADER_LABEL_WIDTH);
    panel.add(label);
    this.projectViewPathField = new JTextField();
    projectViewPathField.setText(defaultWorkspacePath);
    UiUtil.setPreferredWidth(projectViewPathField, MAX_INPUT_FIELD_WIDTH);
    panel.add(projectViewPathField);
    JButton button = new JButton("...");
    button.addActionListener(action -> chooseWorkspacePath());
    int buttonSize = projectViewPathField.getPreferredSize().height;
    button.setPreferredSize(new Dimension(buttonSize, buttonSize));
    panel.add(button);
    this.component = panel;
  }

  @Override
  public String getOptionName() {
    return "copy-external";
  }

  @Override
  public String getOptionText() {
    return "Copy external";
  }

  @Override
  public JComponent getUiComponent() {
    return component;
  }

  @Override
  public BlazeValidationResult validate() {
    if (getProjectViewPath().isEmpty()) {
      return BlazeValidationResult.failure("Path to project view file cannot be empty.");
    }
    File file = new File(getProjectViewPath());
    if (!file.exists()) {
      return BlazeValidationResult.failure("Project view file does not exist.");
    }
    return BlazeValidationResult.success();
  }

  @Nullable
  @Override
  public WorkspacePath getSharedProjectView() {
    return null;
  }

  @Nullable
  @Override
  public String getInitialProjectViewText() {
    try {
      byte[] bytes = Files.readAllBytes(Paths.get(getProjectViewPath()));
      return new String(bytes, StandardCharsets.UTF_8);
    }
    catch (IOException e) {
      return null;
    }
  }

  @Override
  public void commit() {
    userSettings.put(LAST_WORKSPACE_PATH, getProjectViewPath());
  }

  private String getProjectViewPath() {
    return projectViewPathField.getText().trim();
  }

  private void chooseWorkspacePath() {
    FileChooserDescriptor descriptor = new FileChooserDescriptor(true, false, false, false, false, false)
      .withShowHiddenFiles(true) // Show root project view file
      .withHideIgnored(false)
      .withTitle("Select Project View File")
      .withDescription("Select a project view file to import.")
      .withFileFilter(virtualFile -> ProjectViewStorageManager.isProjectViewFile(new File(virtualFile.getPath())));
    FileChooserDialog chooser = FileChooserFactory.getInstance().createFileChooser(descriptor, null, null);

    File startingLocation = null;
    String projectViewPath = getProjectViewPath();
    if (!projectViewPath.isEmpty()) {
      File fileLocation = new File(projectViewPath);
      if (fileLocation.exists()) {
        startingLocation = fileLocation;
      }
    }
    final VirtualFile[] files;
    if (startingLocation != null) {
      VirtualFile toSelect = LocalFileSystem.getInstance().refreshAndFindFileByPath(startingLocation.getPath());
      files = chooser.choose(null, toSelect);
    } else {
      files = chooser.choose(null);
    }
    if (files.length == 0) {
      return;
    }
    VirtualFile file = files[0];
    projectViewPathField.setText(file.getPath());
  }
}
