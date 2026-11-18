#!/usr/bin/env python3
"""Insert 3.71.0 institutional coordinator cues after adverse-selection."""
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
CUE = ROOT / "app/src/main/java/com/aicouncil/app/CoordinatorCue.java"

NEEDLE = '''            rule("adverse-selection", 20,
                    "adverse selection", "неблагоприятный отбор",
                    "Кто сам входит в сделку — не случайная выборка: назови, чем скрытая информация смещает пул до контракта, отдельно от поведения после защиты."),'''

INSERT = NEEDLE + '''
            rule("winners-curse", 20,
                    "winner's curse", "проклятие победителя",
                    "Победа в аукционе или конкурсе не доказывает справедливую цену: назови, условно на победу, не переоценена ли ставка относительно общей ценности."),
            rule("information-cascade", 20,
                    "information cascade", "информационный каскад",
                    "Массовое следование не равно независимым сигналам: назови, сохраняют ли поздние агенты частную информацию или копируют ранних."),
            rule("free-rider", 20,
                    "free rider", "безбилетник", "трагедия общин",
                    "Общее благо не закрывается добровольным вкладом по умолчанию: назови вклад, исключение или принуждение, иначе устойчивость гипотеза."),
            rule("path-dependence", 20,
                    "path dependence", "зависимость от пути", "lock-in",
                    "Доминирование из-за истории не равно превосходству: назови стоимость переключения и альтернативу, которую путь мог заблокировать."),
            rule("lucas-critique", 20,
                    "lucas critique", "критика лукаса",
                    "Прошлое соотношение не обязано удержать после смены правил: назови, переоптимизируют ли агенты поведение под новый режим."),'''

def main() -> int:
    t = CUE.read_text(encoding="utf-8")
    if "winners-curse" in t:
        print("CoordinatorCue already has 3.71.0 rules")
        return 0
    if NEEDLE not in t:
        raise SystemExit("adverse-selection needle missing")
    CUE.write_text(t.replace(NEEDLE, INSERT, 1), encoding="utf-8")
    print("CoordinatorCue 3.71.0 rules applied")
    return 0

if __name__ == "__main__":
    raise SystemExit(main())
