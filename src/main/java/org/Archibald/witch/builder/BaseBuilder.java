package org.Archibald.witch.builder;

import org.Archibald.witch.session.Configuration;

public abstract class BaseBuilder {
    protected final Configuration configuration;

    public BaseBuilder (Configuration configuration) {
        this.configuration = configuration;
    }
}
