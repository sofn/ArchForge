@NullMarked
@ApplicationModule(id = "server-web", type = ApplicationModule.Type.CLOSED, allowedDependencies = {
        "common", "infrastructure", "admin-user::*", "blog::*"
})
package com.lesofn.archforge.server.web;

import org.jspecify.annotations.NullMarked;
import org.springframework.modulith.ApplicationModule;
