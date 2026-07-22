package pas.tmdb.data.tmdb.api.model

fun String.toDepartmentLabel(): String =
    when (this) {
        "Acting" -> "Актёрское искусство"
        "Directing" -> "Режиссура"
        "Writing" -> "Сценарий"
        "Production" -> "Продюсирование"
        "Sound" -> "Звук"
        "Camera" -> "Камера"
        "Editing" -> "Монтаж"
        "Art" -> "Художественный отдел"
        "Costume & Make-Up" -> "Костюмы и грим"
        "Visual Effects" -> "Визуальные эффекты"
        "Crew" -> "Съёмочная группа"
        "Lighting" -> "Свет"
        "Creator" -> "Создатель"
        else -> this
    }
