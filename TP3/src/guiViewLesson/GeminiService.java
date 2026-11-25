package guiViewLesson;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class GeminiService {

    private static final String API_KEY =
            System.getenv("GEMINI_API_KEY");

    /**
     * Uses the Gemini API to generate a clearer version of the provided
     * lesson-learned text while preserving its original meaning.
     *
     * The method validates the input and API key, sends the lesson text
     * to Gemini, handles temporary service failures and timeouts, and
     * returns either the refined lesson text or an error message.
     *
     * This functionality is validated through manual testing of the
     * AI refinement workflow, including successful refinement, applying
     * or canceling a suggestion, saving the refined lesson, and handling
     * API errors.
     *
     * @param originalText the original lesson text to be refined
     * @return the AI-refined lesson text, an empty string for blank input,
     *         or an error message if the Gemini request fails
     */
    
    public static String refineLessonWithAI(String originalText) {

        // Do not send a request if there is no lesson text.
        if (originalText == null || originalText.isBlank()) {
            return "";
        }

        // Make sure the Gemini API key is available.
        if (API_KEY == null || API_KEY.isBlank()) {
            return "ERROR: Gemini API key is not configured.";
        }

        // Ask Gemini to improve the lesson while keeping its meaning.
        String prompt =
                "Rewrite the following lesson learned so it is clearer and easier "
                + "to understand. Keep the original meaning. "
                + "Return only the revised lesson text.\n\n"
                + originalText;

        // Build the JSON request body.
        String requestBody =
                "{"
                + "\"contents\":[{"
                + "\"parts\":[{"
                + "\"text\":\"" + escapeJson(prompt) + "\""
                + "}]"
                + "}]"
                + "}";

        // Build the Gemini API request.
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(
                        "https://generativelanguage.googleapis.com/v1beta/models/"
                        		+ "gemini-3.5-flash-lite:generateContent"))
                .timeout(Duration.ofSeconds(20))
                .header("Content-Type", "application/json")
                .header("x-goog-api-key", API_KEY)
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        try {
            HttpClient client = HttpClient.newHttpClient();

            HttpResponse<String> response = client.send(
                    request,
                    HttpResponse.BodyHandlers.ofString());

            System.out.println("Gemini status: " + response.statusCode());
            System.out.println("Gemini response: " + response.body());

            // If Gemini is temporarily unavailable,
            // wait two seconds and try one more time.
            if (response.statusCode() == 503) {

                System.out.println(
                        "Gemini unavailable. Retrying in 2 seconds...");

                Thread.sleep(2000);

                response = client.send(
                        request,
                        HttpResponse.BodyHandlers.ofString());

                System.out.println(
                        "Gemini retry status: " + response.statusCode());
                System.out.println(
                        "Gemini retry response: " + response.body());
            }

            // Successful request.
            if (response.statusCode() == 200) {
                return extractGeminiText(response.body());
            }

            // Gemini is still unavailable after the retry.
            if (response.statusCode() == 503) {
                return "ERROR: Gemini is temporarily unavailable. "
                        + "Please try again later.";
            }

            // Handle other HTTP errors.
            return "ERROR: Gemini request failed with status "
                    + response.statusCode() + ".";

        } catch (java.net.http.HttpTimeoutException e) {

            return "ERROR: Gemini request timed out. Please try again.";

        } catch (Exception e) {

            return "ERROR: " + e.getMessage();
        }
    }

    /**
     * Escapes special characters so the lesson text can safely
     * be included in the JSON request.
     */
    private static String escapeJson(String text) {

        return text
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }

    /**
     * Extracts the generated lesson text from the Gemini response.
     */
    private static String extractGeminiText(String responseBody) {

        String marker = "\"text\": \"";
        int start = responseBody.indexOf(marker);

        if (start == -1) {
            return "ERROR: Could not read Gemini response.";
        }

        start += marker.length();

        int end = responseBody.indexOf("\"", start);

        if (end == -1) {
            return "ERROR: Could not read Gemini response.";
        }

        String text = responseBody.substring(start, end);

        return text
                .replace("\\n", "\n")
                .replace("\\\"", "\"")
                .replace("\\\\", "\\");
    }
}