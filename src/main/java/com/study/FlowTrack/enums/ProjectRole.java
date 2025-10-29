package com.study.FlowTrack.enums;

public enum ProjectRole {
    // Управляет проектом (настройки, участники, удаление). Создатель проекта получает эту роль.
    PROJECT_ADMIN(4),

    // Продакт-менеджер. Может создавать, редактировать, приоритизировать задачи.
    PROJECT_PRODUCT_MANAGER(3),

    // Основной исполнитель. Может брать задачи в работу, менять статус, но не администрировать.
    PROJECT_DEVELOPER(2),

    // Только просмотр задач, без возможности редактирования.
    PROJECT_VIEWER(1);

    private final int privilegeLevel;

    ProjectRole(int privilegeLevel) {
        this.privilegeLevel = privilegeLevel;
    }

    public int getPrivilegeLevel(){
        return privilegeLevel;
    }
}
