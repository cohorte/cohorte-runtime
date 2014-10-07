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

package org.psem2m.isolates.base.activators;

import org.apache.felix.ipojo.Pojo;
import org.osgi.framework.BundleException;
import org.psem2m.utilities.CXObjectBase;
import org.psem2m.utilities.CXStringUtils;

/**
 * @author isandlatech (www.isandlatech.com) - ogattaz
 */
public abstract class CPojoBase extends CXObjectBase implements IPojoBase {

    public static final String LIB_POJO_ID = "PojoId";

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.psem2m.utilities.IXDescriber#addDescriptionInBuffer(java.lang.Appendable
     * )
     */
    @Override
    public Appendable addDescriptionInBuffer(final Appendable aBuffer) {

        super.addDescriptionInBuffer(aBuffer);
        CXStringUtils.appendKeyValInBuff(aBuffer, LIB_POJO_ID, getPojoId());
        return aBuffer;
    }

    /**
     * @return the id of the bundle
     */
    @Override
    public String getPojoId() {

        try {
            return ((Pojo) this).getComponentInstance().getInstanceName();
        } catch (final Exception e) {
            return "not a pojo";
        }
    }

    /**
     * @throws Exception
     */
    @Override
    public abstract void invalidatePojo() throws BundleException;

    /**
     * @throws BundleException
     * @throws Exception
     */
    @Override
    public abstract void validatePojo() throws BundleException;
}
