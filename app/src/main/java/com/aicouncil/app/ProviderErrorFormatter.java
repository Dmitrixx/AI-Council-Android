package com.aicouncil.app;


/** Converts provider HTTP failures into actionable, user-facing Russian messages. */
public final class ProviderErrorFormatter {
    private static final int MAX_DETAIL_CHARS = 1200;

    private ProviderErrorFormatter() { }

    public static String formatHttpError(int statusCode, String responseBody, String modelName, String apiKey) {
        String guidance;
        switch (statusCode) {
            case 400:
                guidance = "Провайдер отклонил параметры запроса. Проверьте ID модели, формат API и доступный лимит токенов.";
                break;
            case 401:
                guidance = "Не пройдена авторизация. Проверьте API-ключ и убедитесь, что он сохранён для этого провайдера.";
                break;
            case 403:
                guidance = "Доступ запрещён. Проверьте права ключа, доступность модели для аккаунта и ограничения провайдера.";
                break;
            case 404:
                guidance = "Не найден API endpoint или модель. Проверьте базовый URL и точный идентификатор модели.";
                break;
            case 408:
                guidance = "Провайдер сообщил о тайм-ауте. Проверьте соединение или повторите запрос позже.";
                break;
            case 413:
                guidance = "Запрос слишком большой для провайдера. Уменьшите размер задачи или объём импортированного текста.";
                break;
            case 422:
                guidance = "Провайдер не смог обработать параметры запроса. Проверьте модель и поддерживаемые поля API.";
                break;
            case 429:
                guidance = "Сработал лимит запросов или квоты. Проверьте баланс/квоту и подождите перед повторным запуском.";
                break;
            case 504:
                guidance = "Шлюз провайдера не дождался ответа модели. Попробуйте позже или уменьшите объём задачи.";
                break;
            default:
                if (statusCode >= 500 && statusCode <= 599) {
                    guidance = "На стороне провайдера произошла серверная ошибка. Проверьте его статус и повторите запрос позже.";
                } else if (statusCode >= 300 && statusCode < 400) {
                    guidance = "Сервер перенаправил запрос. Проверьте базовый URL: автоматическое перенаправление отключено.";
                } else {
                    guidance = "Провайдер отклонил запрос. Проверьте настройки подключения и ответ сервера.";
                }
        }

        StringBuilder result = new StringBuilder();
        result.append("Ошибка API HTTP ").append(statusCode).append(": ").append(guidance);
        String safeModel = modelName == null ? "" : modelName.trim();
        if (statusCode == 404 && !safeModel.isEmpty()) {
            result.append("\nПроверяемая модель: ").append(safeModel);
        }
        String detail = sanitizeDetail(responseBody, apiKey);
        if (!detail.isEmpty()) result.append("\nДетали провайдера: ").append(detail);
        return result.toString();
    }

    static String sanitizeDetail(String responseBody, String apiKey) {
        if (responseBody == null) return "";
        String detail = SecretRedactor.redact(responseBody, apiKey).trim();
        if (detail.isEmpty()) return "";
        if (detail.length() > MAX_DETAIL_CHARS) detail = detail.substring(0, MAX_DETAIL_CHARS) + "…";
        return detail;
    }
}
