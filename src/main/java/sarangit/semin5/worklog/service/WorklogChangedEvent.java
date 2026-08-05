package sarangit.semin5.worklog.service;

import sarangit.semin5.worklog.entity.request;

public record WorklogChangedEvent(String sourceClientId, request item, Integer deletedId) { }
