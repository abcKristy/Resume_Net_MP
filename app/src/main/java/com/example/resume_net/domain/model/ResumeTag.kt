package com.example.resume_net.domain.model

enum class ResumeTag(val displayName: String) {
    NO_NUMBERS("Нет цифр и метрик"),
    TOO_MUCH_WATER("Много воды"),
    NO_SKILLS("Нет навыков"),
    BAD_STRUCTURE("Плохая структура"),
    NO_ACHIEVEMENTS("Нет достижений"),
    TOO_SHORT("Слишком короткое"),
    BAD_GRAMMAR("Ошибки и опечатки"),
    NO_ABOUT("Нет раздела «Обо мне»"),
    WEAK_LANGUAGE("Слабый язык"),
    TOO_VAGUE("Слишком расплывчато"),
    WEAK_ACHIEVEMENTS("Слабые достижения"),
    LOW_EXPERIENCE("Мало опыта"),
    NO_EDUCATION("Не указано образование"),
    GENERIC_TEMPLATE("Шаблонное резюме"),
    NO_CONTACTS("Нет контактов"),
    TOO_MANY_JOBS("Частая смена работы"),
    BAD_FORMATTING("Плохое форматирование"),
    WRONG_TONE("Неподходящий тон"),
    SALARY_FOCUS("Фокус на зарплате"),
    IRRELEVANT_INFO("Лишняя информация")
}