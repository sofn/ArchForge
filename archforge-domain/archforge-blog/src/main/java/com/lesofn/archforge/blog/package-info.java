@NullMarked
@ApplicationModule(id = "blog", type = ApplicationModule.Type.OPEN, allowedDependencies = {
        "common", "infrastructure"
})
package com.lesofn.archforge.blog;

import org.jspecify.annotations.NullMarked;
import org.springframework.modulith.ApplicationModule;
