//
// Copyright Red Hat, Inc.
//
// SPDX-License-Identifier: GPL-2.0-or-later
//
package com.netscape.cmscore.security;

import com.netscape.certsrv.base.EBaseException;
import com.netscape.cmscore.base.ConfigStorage;
import com.netscape.cmscore.base.PropConfigStore;
import com.netscape.cmscore.base.SimpleProperties;

public class JssSubsystemConfig extends PropConfigStore {

    public JssSubsystemConfig() {
    }

    public JssSubsystemConfig(ConfigStorage storage) {
        super(storage);
    }

    public JssSubsystemConfig(String name, SimpleProperties source) {
        super(name, source);
    }

    public boolean isEnabled() throws EBaseException {
        return getBoolean("enable", true);
    }
}
