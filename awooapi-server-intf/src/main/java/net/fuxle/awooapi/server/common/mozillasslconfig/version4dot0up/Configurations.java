/*
 * Copyright (c) 2024 Moritz Hofmann <info@morihofi.de>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge,
 * publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE
 * FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package net.fuxle.awooapi.server.common.mozillasslconfig.version4dot0up;

/**
 * Represents the SSL configurations for different levels: modern, old, and intermediate.
 * This class contains configuration details for each level.
 */
public class Configurations {

    /**
     * Configuration object for modern SSL configurations.
     */
    private Configuration modern;

    /**
     * Configuration object for old SSL configurations.
     */
    private Configuration old;

    /**
     * Configuration object for intermediate SSL configurations.
     */
    private Configuration intermediate;

    /**
     * Gets the modern SSL configuration.
     *
     * @return the Configuration object for modern SSL settings.
     */
    public Configuration getModern() {
        return modern;
    }

    /**
     * Gets the old SSL configuration.
     *
     * @return the Configuration object for old SSL settings.
     */
    public Configuration getOld() {
        return old;
    }

    /**
     * Gets the intermediate SSL configuration.
     *
     * @return the Configuration object for intermediate SSL settings.
     */
    public Configuration getIntermediate() {
        return intermediate;
    }
}