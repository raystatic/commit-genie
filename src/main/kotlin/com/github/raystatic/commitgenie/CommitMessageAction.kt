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

class CommitMessageAction : AnAction() {

    override fun actionPerformed(event: AnActionEvent) {
        val project = event.project ?: return

        val gitRoot = ProjectLevelVcsManager.getInstance(project).allVcsRoots.firstOrNull()?.path?.path
        if (gitRoot == null) {
            Messages.showErrorDialog(project, "Could not determine Git root.", "CommitGenius")
            return
        }

        val changes = ChangeListManager.getInstance(project).allChanges
        if (changes.isEmpty()) {
            Messages.showInfoMessage(project, "No staged changes found.", "CommitGenius")
            return
        }

        val diffSummary = GitUtils.getGitDiff(gitRoot)
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
                    is Result.Success -> Messages.showInfoMessage(result.message, "Generated Commit Message")
                    is Result.Failure -> Messages.showErrorDialog(result.error, "CommitGenius")
                }
            }
        }
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