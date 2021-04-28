package com.pluralsight.pension.setup;

public interface AccountOpeningEventPublisher {

    boolean notify(String accountId);
}
