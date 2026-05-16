package com.example.resume_net.presentation.newanalysis

/**
 * Данные для примеров резюме
 */
data class ResumeExample(
    val id: Int,
    val title: String,
    val description: String,
    val content: String
)

object ResumeExamples {

    val examples = listOf(
        ResumeExample(
            id = 1,
            title = "Frontend Developer",
            description = "Пример качественного резюме фронтенд-разработчика",
            content = """Frontend Developer с 5-летним опытом разработки веб-приложений.

КЛЮЧЕВЫЕ НАВЫКИ:
• React, Next.js, TypeScript, Redux Toolkit
• Vue.js, Nuxt.js, Pinia
• HTML5, CSS3, SCSS, Tailwind CSS
• Webpack, Vite, Git, CI/CD

ОПЫТ РАБОТЫ:
Senior Frontend Developer | ООО «ТехноСофт» | 2022 – настоящее время
• Разработал SPA для управления клиентской базой на React, сократив время загрузки на 35%
• Внедрил TypeScript в проект, снизив количество багов на 25%
• Оптимизировал сборку Webpack, уменьшив размер бандла на 40%
• Провёл 5 код-ревью в неделю и менторил 2 джуниоров

Middle Frontend Developer | ООО «ВебСтудия» | 2019 – 2022
• Разработал 10+ адаптивных лендингов и интернет-магазинов
• Интегрировал REST API и GraphQL на проектах
• Увеличил конверсию сайтов на 20% за счёт оптимизации производительности

ОБРАЗОВАНИЕ:
МГТУ им. Баумана, факультет «Информатика и системы управления», 2019

ДОСТИЖЕНИЯ:
• Победитель хакатона Цифровой Прорыв 2023
• Сертификат Google Mobile Web Specialist"""
        ),
        ResumeExample(
            id = 2,
            title = "Product Manager",
            description = "Пример резюме продакт-менеджера с метриками",
            content = """Product Manager с 6-летним опытом в B2B и B2C продуктах.

КЛЮЧЕВЫЕ НАВЫКИ:
• Product Strategy, Roadmap Planning, Go-to-market
• User Research, A/B Testing, User Analytics
• Agile, Scrum, Kanban, OKR
• Jira, Confluence, Miro, Tableau

ОПЫТ РАБОТЫ:
Senior Product Manager | ООО «Цифровые Решения» | 2021 – настоящее время
• Запустил мобильное приложение, достиг 500k скачиваний за 6 месяцев
• Увеличил LTV пользователей на 45% за счёт внедрения персонализации
• Сократил время выхода новых фич на 30% через оптимизацию процессов
• NPS продукта вырос с 35 до 62 за год

Product Owner | ООО «Интернет-Технологии» | 2018 – 2021
• Увеличил MAU с 50k до 200k за 1.5 года
• Запустил систему подписок, увеличив MRR на 150%
• Провёл 50+ глубинных интервью с пользователями

ОБРАЗОВАНИЕ:
НИУ ВШЭ, магистратура «Управление IT-проектами», 2018

МЕТРИКИ:
• Рост выручки: +40% год к году
• Retention (D30): 35%
• Удержание после первого платежа: 78%"""
        ),
        ResumeExample(
            id = 3,
            title = "iOS Developer",
            description = "Пример резюме iOS разработчика с портфолио",
            content = """iOS Developer с 4-летним опытом разработки на Swift и SwiftUI.

КЛЮЧЕВЫЕ НАВЫКИ:
• Swift, SwiftUI, UIKit, Combine
• CoreData, Realm, Firebase
• REST API, GraphQL, URLSession
• Git, Fastlane, TestFlight, App Store Connect

ОПЫТ РАБОТЫ:
iOS Developer | ООО «Мобильные Приложения» | 2022 – настоящее время
• Разработал и опубликовал 5 приложений в App Store
• Средний рейтинг приложений: 4.8 (на основе 2000+ отзывов)
• Внедрил MVVM + Coordinator, повысив тестируемость кода
• Написал 100+ unit-тестов (покрытие 85%)

Junior iOS Developer | ООО «Студия Мобайл» | 2020 – 2022
• Участвовал в разработке приложения для интернет-магазина (50k установок)
• Оптимизировал работу с изображениями, снизив потребление памяти на 30%

ПОРТФОЛИО В APP STORE:
• «Task Manager Pro» – органайзер задач (10k+ установок)
• «Fitness Tracker» – трекер тренировок (4.9 звезды)

ОБРАЗОВАНИЕ:
СПбГУ, бакалавр «Программная инженерия», 2020

СЕРТИФИКАТЫ:
• Apple Certified iOS Developer
• SwiftUI: Building Advanced Apps"""
        )
    )
}