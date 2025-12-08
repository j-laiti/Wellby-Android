package com.rcsi.wellby

/**
 * Private API Keys Configuration Template
 *
 * This is a template file for PrivateKeys.kt
 *
 * SETUP INSTRUCTIONS:
 * 1. Copy this file and rename it to PrivateKeys.kt
 * 2. Replace all placeholder values with your actual API keys
 * 3. Never commit PrivateKeys.kt to version control (it's in .gitignore)
 *
 * WHERE TO GET YOUR API KEYS:
 *
 * OpenAI API Key:
 * - Sign up at https://platform.openai.com/
 * - Navigate to API Keys section
 * - Create a new secret key
 * - Note: This will incur costs based on usage
 *
 * YouTube Data API Key:
 * - Go to https://console.cloud.google.com/
 * - Create a new project or select existing
 * - Enable YouTube Data API v3
 * - Create credentials (API Key)
 */
object PrivateKeys {
    /**
     * OpenAI API Key
     * Format: sk-proj-...
     */
    const val OPENAI_API_KEY = "your-openai-api-key-here"

    /**
     * YouTube Data API Key
     * Format: AIza...
     */
    const val YOUTUBE_API_KEY = "your-youtube-api-key-here"
}