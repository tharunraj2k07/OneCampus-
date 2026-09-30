import { env } from './env';

export interface IGeminiConfig {
  apiKey: string;
  model: string;
  apiBaseUrl: string;
  maxOutputTokens: number;
  temperature: number;
  timeoutMs: number;
  isAvailable: boolean;
}

const geminiApiKey = env.GEMINI_API_KEY || process.env.GEMINI_API_KEY || '';

export const geminiConfig: IGeminiConfig = {
  apiKey: geminiApiKey,
  // Default to gemini-3.6-flash which is the active high-speed multimodal standard
  model: process.env.GEMINI_MODEL || 'gemini-3.6-flash',
  apiBaseUrl: 'https://generativelanguage.googleapis.com/v1beta/models',
  maxOutputTokens: 2048,
  temperature: 0.2, // Low temperature for deterministic, structured factual extraction
  timeoutMs: 15000,
  isAvailable: !!geminiApiKey && geminiApiKey.trim().length > 0
};
