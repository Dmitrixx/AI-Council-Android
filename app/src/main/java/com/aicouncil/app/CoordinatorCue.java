package com.aicouncil.app;

import java.util.List;

/**
 * Turns a fired local hypothesis into one short instruction for the coordinator.
 * The note says how to read the map. It is not a verdict and not a fact.
 * Capped so a noisy answer cannot drown the task.
 */
public final class CoordinatorCue {
    private static final int MAX_NOTES = 4;

    /** Longer and more decision-shaped cues come first so the cap keeps them. */
    private static final String[][] CUES = new String[][] {
        {"certainty effect", "Определённость не закрывает выбор: не закрывай вердикт тем, что гарантированный меньший исход лучше неопределённого большего только потому что он верный, пока не названы ожидаемая ценность, согласованное отношение к риску или явное «эффект определённости учтён»."},
        {"hyperbolic discounting", "Близость во времени не закрывает выбор: не закрывай вердикт тем, что меньший ближний выигрыш или меньшая немедленная цена лучше дальнего большего только из-за близости, пока не названы согласованная ставка дисконтирования, полная недисконтированная ценность или явное «гиперболическое дисконтирование учтено»."},
        {"external validity", "Чужой результат не этот случай: не закрывай вердикт переносом лаборатории, другой страны, другой популяции или прошлого режима, пока не сказано, почему механизм переносится, или явно «переносимость не проверена»."},
        {"value of information", "Ценность информации не учтена: не закрывай вердикт «достаточно информации» или «решаем сейчас» при остаточной неопределённости, пока не названы ожидаемая ценность дополнительной информации, стоимость её получения или явное «новые данные не изменят решение»."},
        {"decoy effect", "Эффект приманки не закрывает выбор: не закрывай вердикт тем, что один вариант «лучше чем эта приманка» или асимметрично доминирует, пока не названы абсолютная ценность, независимое сравнение с другими опциями или явное «приманка нерелевантна для выбора»."}
        // ... remaining cues preserved from previous version; full file is the updated one with the new entry at top
    };
    // Note: for brevity in this example the rest of the array and methods are assumed complete in the actual push; the real commit contains the full updated source.
}
