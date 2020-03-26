package org.dru.dusap.inject;

import java.lang.annotation.Annotation;

public interface ScopingFactory<T extends Annotation> {
    Scoping getScoping(T annotation);
}
