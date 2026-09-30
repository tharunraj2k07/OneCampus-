import { geminiConfig } from '../../config/gemini';
import { IGeminiStructuredAnnouncement } from './ai.types';

export class GeminiService {
  private apiKey: string;
  private model: string;
  private baseUrl: string;
  private timeoutMs: number;

  constructor(
    apiKey: string = geminiConfig.apiKey,
    model: string = geminiConfig.model,
    baseUrl: string = geminiConfig.apiBaseUrl,
    timeoutMs: number = geminiConfig.timeoutMs
  ) {
    this.apiKey = apiKey;
    this.model = model;
    this.baseUrl = baseUrl;
    this.timeoutMs = timeoutMs;
  }

  public isAvailable(): boolean {
    return !!this.apiKey && this.apiKey.trim().length > 0;
  }

  /**
   * Generates structured intelligence for an announcement using Gemini API.
   */
  public async analyzeAnnouncementContent(params: {
    title: string;
    originalContent: string;
    category: string;
    targetDepartments: string[];
    targetYears: number[];
    deadline?: Date;
    eligibility?: string;
  }): Promise<IGeminiStructuredAnnouncement> {
    if (!this.isAvailable()) {
      throw new Error('Gemini API key is not configured or unavailable');
    }

    const systemPrompt = `You are the OneCampus AI Announcement Intelligence Engine for college campus circulars.
Your job is to accurately extract structured, actionable information from college notices, circulars, and announcements.

You MUST respond strictly with a valid JSON object following this exact schema:
{
  "summary": "Short 1-2 sentence concise executive summary for students",
  "keyActions": ["Action point 1", "Action point 2"],
  "deadline": "YYYY-MM-DDTHH:mm:ssZ or null if no deadline mentioned",
  "eligibility": {
    "departments": ["CSE", "IT"],
    "years": [3, 4],
    "requirements": ["Eligible CGPA or conditions"]
  },
  "keywords": ["Keyword1", "Keyword2"],
  "eventType": "PLACEMENT | EXAMINATION | ACADEMIC | ASSIGNMENT | WORKSHOP | EVENT | COMPETITION | SCHOLARSHIP | GENERAL",
  "urgencyIndicators": ["Words indicating urgency such as 'Immediate', 'Mandatory', 'Last date'"],
  "requiredDocuments": ["Resume", "Marksheet etc if mentioned"],
  "locationOrPlatform": "Venue or online link platform",
  "confidence": 0.95
}

Rules:
1. Do not invent false facts. Extract directly from content or mark null/empty.
2. The summary must be crisp, scannable, and highlighting action for students.
3. If dates/deadlines are mentioned in text, convert to standard ISO format (UTC).
4. If no deadline is present, set "deadline" to null.
5. "confidence" must be a float between 0.0 and 1.0 representing extraction confidence.
6. Return ONLY valid JSON, with no markdown code fences or conversational text.`;

    const userPrompt = `Analyze this college announcement:
Title: ${params.title}
Category: ${params.category}
Target Departments: ${params.targetDepartments.join(', ')}
Target Academic Years: ${params.targetYears.join(', ')}
Faculty Provided Deadline: ${params.deadline ? params.deadline.toISOString() : 'None provided'}
Faculty Provided Eligibility: ${params.eligibility || 'None provided'}

Original Notice Content:
${params.originalContent}`;

    let rawJsonText: string;
    try {
      rawJsonText = await this.callGeminiApi(systemPrompt, userPrompt);
    } catch (primaryError: any) {
      console.warn(`[GeminiService] Primary API call failed: ${primaryError.message}`);
      throw primaryError;
    }

    // Validate and parse JSON
    const parsed = this.tryParseAndValidate(rawJsonText);
    if (parsed) {
      return parsed;
    }

    // If initial output is malformed, attempt one safe repair call
    console.warn('[GeminiService] Initial response malformed JSON. Initiating single repair attempt...');
    try {
      const repairPrompt = `The following text was supposed to be valid JSON matching the schema, but had formatting errors:
${rawJsonText}

Please correct and output ONLY the valid JSON object:`;
      const repairedJsonText = await this.callGeminiApi(systemPrompt, repairPrompt);
      const repairedParsed = this.tryParseAndValidate(repairedJsonText);
      if (repairedParsed) {
        return repairedParsed;
      }
    } catch (repairError: any) {
      console.warn(`[GeminiService] Repair call failed: ${repairError.message}`);
    }

    throw new Error('Gemini returned an invalid or unparseable JSON structure after repair attempt');
  }

  private async callGeminiApi(systemInstruction: string, promptText: string): Promise<string> {
    const url = `${this.baseUrl}/${this.model}:generateContent?key=${this.apiKey}`;

    const requestBody = {
      contents: [
        {
          role: 'user',
          parts: [{ text: `${systemInstruction}\n\n${promptText}` }]
        }
      ],
      generationConfig: {
        responseMimeType: 'application/json',
        temperature: geminiConfig.temperature,
        maxOutputTokens: geminiConfig.maxOutputTokens
      }
    };

    const controller = new AbortController();
    const timer = setTimeout(() => controller.abort(), this.timeoutMs);

    try {
      const response = await fetch(url, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(requestBody),
        signal: controller.signal
      });

      clearTimeout(timer);

      if (!response.ok) {
        const errorText = await response.text().catch(() => '');
        throw new Error(`Gemini API HTTP ${response.status}: ${errorText.slice(0, 300)}`);
      }

      const responseJson: any = await response.json();
      const text = responseJson?.candidates?.[0]?.content?.parts?.[0]?.text;

      if (!text || typeof text !== 'string') {
        throw new Error('Gemini API response contained no valid text in candidate content');
      }

      return text.trim();
    } catch (error: any) {
      clearTimeout(timer);
      if (error.name === 'AbortError') {
        throw new Error(`Gemini API request timed out after ${this.timeoutMs}ms`);
      }
      throw error;
    }
  }

  /**
   * Strictly parses and validates the returned JSON against expected types and ranges.
   */
  public tryParseAndValidate(rawText: string): IGeminiStructuredAnnouncement | null {
    try {
      // Strip any accidental markdown formatting ```json ... ```
      let cleanText = rawText.trim();
      if (cleanText.startsWith('```json')) {
        cleanText = cleanText.replace(/^```json\s*/i, '').replace(/\s*```$/i, '');
      } else if (cleanText.startsWith('```')) {
        cleanText = cleanText.replace(/^```\s*/, '').replace(/\s*```$/, '');
      }

      const json = JSON.parse(cleanText);
      if (!json || typeof json !== 'object') {
        return null;
      }

      // Summary
      const summary = typeof json.summary === 'string' && json.summary.trim().length > 0
        ? json.summary.trim()
        : '';

      // Key Actions
      const keyActions = Array.isArray(json.keyActions)
        ? json.keyActions.filter((a: any) => typeof a === 'string' && a.trim().length > 0).map((a: string) => a.trim())
        : [];

      // Deadline validation
      let deadlineStr: string | null = null;
      if (typeof json.deadline === 'string' && json.deadline.trim().length > 0) {
        const parsedMs = Date.parse(json.deadline.trim());
        if (!isNaN(parsedMs)) {
          deadlineStr = new Date(parsedMs).toISOString();
        }
      }

      // Eligibility
      let eligibility: any = null;
      if (json.eligibility && typeof json.eligibility === 'object') {
        eligibility = {
          departments: Array.isArray(json.eligibility.departments) ? json.eligibility.departments : [],
          years: Array.isArray(json.eligibility.years) ? json.eligibility.years : [],
          requirements: Array.isArray(json.eligibility.requirements) ? json.eligibility.requirements : []
        };
      } else if (typeof json.eligibility === 'string' && json.eligibility.trim().length > 0) {
        eligibility = json.eligibility.trim();
      }

      // Keywords
      const keywords = Array.isArray(json.keywords)
        ? json.keywords.filter((k: any) => typeof k === 'string').map((k: string) => k.trim())
        : [];

      // Event Type
      const eventType = typeof json.eventType === 'string' && json.eventType.trim().length > 0
        ? json.eventType.trim().toUpperCase()
        : 'GENERAL';

      // Urgency Indicators
      const urgencyIndicators = Array.isArray(json.urgencyIndicators)
        ? json.urgencyIndicators.filter((u: any) => typeof u === 'string').map((u: string) => u.trim())
        : [];

      // Required Documents
      const requiredDocuments = Array.isArray(json.requiredDocuments)
        ? json.requiredDocuments.filter((d: any) => typeof d === 'string').map((d: string) => d.trim())
        : [];

      // Location or Platform
      const locationOrPlatform = typeof json.locationOrPlatform === 'string' && json.locationOrPlatform.trim().length > 0
        ? json.locationOrPlatform.trim()
        : null;

      // Confidence score clamped between 0.0 and 1.0
      let confidence = 0.85;
      if (typeof json.confidence === 'number' && !isNaN(json.confidence)) {
        confidence = Math.max(0.0, Math.min(1.0, json.confidence));
      }

      return {
        summary,
        keyActions,
        deadline: deadlineStr,
        eligibility,
        keywords,
        eventType,
        urgencyIndicators,
        requiredDocuments,
        locationOrPlatform,
        confidence
      };
    } catch {
      return null;
    }
  }
}
