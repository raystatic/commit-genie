# ✨ Commit Genie

![Build](https://github.com/raystatic/commit-genie/workflows/Build/badge.svg)
[![Version](https://img.shields.io/jetbrains/plugin/v/27496-commit-genie.svg)](https://plugins.jetbrains.com/plugin/27496-commit-genie)
[![Downloads](https://img.shields.io/jetbrains/plugin/d/27496-commit-genie.svg)](https://plugins.jetbrains.com/plugin/27496-commit-genie)

Commit Genie is an AI-powered IntelliJ plugin designed to streamline your Git workflow by generating clear, concise commit messages based on your code changes. By analyzing staged modifications, it suggests commit messages that adhere to best practices, enhancing both productivity and consistency in your development process.

## 🧠 Features
- 🔍 Analyzes staged changes to suggest commit messages.
- 🧾 Generates commit messages adhering to conventional commit standards.
- 🪄 Automatically saves your OpenAI API key securely for future use.
- 📋 Easily copy to clipboard or apply suggested messages.
- 💬 Supports fallback for API errors and invalid keys.


## 🖼️ Screenshots
🔍 1. Trigger the Action
Search for Generate Commit Message via the IntelliJ action panel.
<image src="https://github.com/user-attachments/assets/4a47ba4b-15b6-4ad5-8b19-0fc6ddb74be5" width=500px/>

🔐 2. Enter API Key
Enter your OpenAI API key on first use. It will be securely stored for future prompts.
<image src="https://github.com/user-attachments/assets/b286dedb-59ab-4ab2-8a4c-d3d052ec12f0" width=500px/>

💬 3. Suggested Commit Message
Generated commit messages are shown in a dialog with an option to copy.
<image src="https://github.com/user-attachments/assets/dfd6fc91-5c15-4caa-9428-59a511f2828d" width=500px/>

## 🚀 Getting Started
✅ Prerequisites
IntelliJ IDEA 2022.2+

OpenAI API Key (get yours from platform.openai.com)

## 🔧 Installation
Go to Settings → Plugins → Search for Commit Genie.

Click Install and restart your IDE.

Trigger via ⇧ Shift twice → type Generate Commit Message.

## 🔑 API Key Setup
On first use, you’ll be prompted to enter your OpenAI API key. It will be encrypted and stored securely for reuse.

If the key becomes invalid or returns an error, the plugin will prompt you to re-enter a new key.

## 🛠️ Development
Clone this repo and run the plugin using IntelliJ’s Plugin DevKit:
`git clone https://github.com/raystatic/commit-genie.git`
Use the `runIde` task to launch a sandbox IDE with the plugin.

🤝 Contributing
Feel free to open issues or submit pull requests to improve functionality, add features, or fix bugs.

