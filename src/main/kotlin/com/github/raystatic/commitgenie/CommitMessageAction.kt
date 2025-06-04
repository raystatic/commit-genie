package com.github.raystatic.commitgenie

import com.intellij.credentialStore.CredentialAttributes
import com.intellij.ide.passwordSafe.PasswordSafe
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vcs.ProjectLevelVcsManager
import com.intellij.openapi.vcs.changes.ChangeListManager
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection

class CommitMessageAction : AnAction() {

    override fun actionPerformed(event: AnActionEvent) {
        val project = event.project ?: return

        val gitRoot = ProjectLevelVcsManager.getInstance(project).allVcsRoots.firstOrNull()?.path?.path
        println("gitroot: $gitRoot")
        if (gitRoot == null) {
            Messages.showErrorDialog(project, "Could not determine Git root.", "CommitGenius")
            return
        }

        val changes = ChangeListManager.getInstance(project).allChanges
        println("changes: $changes")
        if (changes.isEmpty()) {
            Messages.showInfoMessage(project, "No staged changes found.", "CommitGenius")
            return
        }

        val diffSummary = GitUtils.getGitDiff(gitRoot)
        println("diffSummary: $diffSummary")
        if (diffSummary.isEmpty()) {
            Messages.showInfoMessage(project, "No staged changes found.", "CommitGenius")
            return
        }

        val savedApiKey = getSavedApiKey()
        val apiKey = savedApiKey ?: promptForApiKey(project)?.also {
            saveApiKey(it)
        }

        if (apiKey.isNullOrBlank()) {
            Messages.showErrorDialog(project, "OpenAI API key is required to generate commit messages.", "CommitGenius")
            return
        }

        OpenAIClient.generateCommitMessage(apiKey, diffSummary) { result ->
            ApplicationManager.getApplication().invokeLater {
                when (result) {
                    is Result.Success -> {
                        val choice = Messages.showYesNoCancelDialog(
                            project,
                            result.message,
                            "Generated Commit Message",
                            "Copy to Clipboard",
                            "Close",
                            "Cancel", // No third button (Cancel)
                            Messages.getInformationIcon()
                        )

                        if (choice == Messages.YES) {
                            val clipboard = Toolkit.getDefaultToolkit().systemClipboard
                            val selection = StringSelection(result.message)
                            clipboard.setContents(selection, selection)
//                            Messages.showInfoMessage("Commit message copied to clipboard.", "CommitGenius")
                        }
                    }
                    is Result.Failure -> {
                        if (result.code == 401) {
                            invalidateApiKey()
                            val newApiKey = promptForApiKey(project)
                            if (!newApiKey.isNullOrBlank()) {
                                saveApiKey(newApiKey)
                                // Retry once with new key
                                OpenAIClient.generateCommitMessage(newApiKey, diffSummary) { retryResult ->
                                    ApplicationManager.getApplication().invokeLater {
                                        when (retryResult) {
                                            is Result.Success -> Messages.showInfoMessage(retryResult.message, "Generated Commit Message")
                                            is Result.Failure -> Messages.showErrorDialog(retryResult.error, "CommitGenius")
                                        }
                                    }
                                }
                            } else {
                                Messages.showErrorDialog(project, "No valid API key entered.", "CommitGenius")
                            }
                        } else {
                            Messages.showErrorDialog(result.error, "CommitGenius")
                        }
                    }
                }
            }
        }
    }

    private fun invalidateApiKey() {
        val attributes = CredentialAttributes("com.commitgenie.openai.apikey")
        PasswordSafe.instance.setPassword(attributes, null)
    }

    private fun promptForApiKey(project: Project): String? {
        return Messages.showInputDialog(
            project,
            "Enter your OpenAI API Key",
            "OpenAI API Key Required",
            Messages.getQuestionIcon()
        )
    }

    private fun saveApiKey(apiKey: String) {
        val attributes = CredentialAttributes("com.commitgenie.openai.apikey")
        PasswordSafe.instance.setPassword(attributes, apiKey)
    }

    private fun getSavedApiKey(): String? {
        val attributes = CredentialAttributes("com.commitgenie.openai.apikey")
        return PasswordSafe.instance.getPassword(attributes)
    }

}