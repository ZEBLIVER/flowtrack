package com.study.FlowTrack.enums;

public enum StatusTask {
    // Задача создана, но работа не началась.
    OPEN,

    // Работа ведется.
    IN_PROGRESS,

    // Завершена разработчиком, ожидает проверки (ревью).
    IN_REVIEW,

    // Проверка пройдена, задача готова.
    DONE,

    // Принято решение не выполнять задачу.
    CANCELED
}