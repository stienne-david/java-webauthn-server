/*
 * Copyright 2014 Yubico.
 * Copyright 2014 Google Inc. All rights reserved.
 *
 * Use of this source code is governed by a BSD-style
 * license that can be found in the LICENSE file or at
 * https://developers.google.com/open-source/licenses/bsd
 */

package com.yubico.webauthn;

/**
 * Thrown when invalid data is given to the server by an external caller.
 */
@SuppressWarnings("serial")
class U2fBadInputException extends Exception {

    public U2fBadInputException(String message) {
        super(message);
    }

    public U2fBadInputException(String message, Throwable cause) {
        super(message, cause);
    }
}
