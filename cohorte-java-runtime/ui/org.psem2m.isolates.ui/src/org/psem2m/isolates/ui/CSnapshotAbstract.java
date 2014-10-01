/**
 * Copyright 2014 isandlaTech
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.psem2m.isolates.ui;

/**
 * @author ogattaz
 *
 */
public abstract class CSnapshotAbstract {

    /** The tree item name */
    private final String pName;

    /**
     * @param aState
     */
    public CSnapshotAbstract(final String aName) {

        pName = aName;
    }

    /**
     * @return
     */
    public abstract CSnapshotAbstract getChild(final int aIdx);

    /**
     * @return
     */
    public abstract int getChildCount();

    /**
     * @param aChild
     * @return
     */
    public abstract int getIndexOfChild(final CSnapshotAbstract aChild);

    /**
     * @return
     */
    public String getName() {

        return pName;
    }

    /**
     * @return
     */
    public abstract String getTextInfo();
}
