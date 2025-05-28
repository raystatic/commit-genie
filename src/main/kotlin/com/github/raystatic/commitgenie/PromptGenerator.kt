package com.github.raystatic.commitgenie

object PromptGenerator {

    fun generatePrompt(diff: String): String {
        return buildString {
            appendLine("You are an AI assistant that writes professional Git commit messages based on diffs.")
            appendLine()
            appendLine("## Rules:")
            appendLine("- Follow the Conventional Commits specification: <type>(<scope>): <description>")
            appendLine("- Type must be one of: feat, fix, chore, docs, style, refactor, perf, test")
            appendLine("- Scope is optional. If unclear, omit it.")
            appendLine("- Description must be concise and written in the imperative mood (e.g., 'add', 'fix', 'update').")
            appendLine("- No need to include anything besides the commit message itself.")
            appendLine()
            appendLine("## Example:")
            appendLine("fix(ui): correct button alignment on mobile")
            appendLine()
            appendLine("## Git Diff:")
            appendLine("```diff")
            appendLine(diff.trim())
            appendLine("```")
        }
    }
}
