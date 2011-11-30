/*******************************************************************************
 * Copyright (c) 2011 www.isandlatech.com (www.isandlatech.com)
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    ogattaz  (isandlaTech) - 30 nov. 2011 - initial API and implementation
 *******************************************************************************/
package org.psem2m.composer.demo;

import java.util.List;
import java.util.Map;

import org.psem2m.utilities.CXStringUtils;

/**
 * @author ogattaz
 * 
 */
public class ComponentContextDumper {

    /**
     * @param aComponentContext
     * @return
     */
    public static String dump(final IComponentContext aComponentContext) {

        return dumpInSB(new StringBuilder(), aComponentContext).toString();
    }

    /**
     * @param aSB
     * @param aComponentContext
     * @return
     */
    public static StringBuilder dumpInSB(final StringBuilder aSB,
            final IComponentContext aComponentContext) {

        boolean wHasError = aComponentContext.hasError();

        CXStringUtils.appendFormatStrInBuff(aSB, "hasError=[%b]", wHasError);

        if (wHasError) {
            List<String> wErrors = aComponentContext.getErrors();

            CXStringUtils.appendFormatStrInBuff(aSB, " nbErrors=[%d]",
                    wErrors.size());
            if (wErrors.size() > 0) {
                int wI = 0;
                for (String wError : wErrors) {
                    CXStringUtils.appendFormatStrInBuff(aSB, " error(%d)=[%s]",
                            wI, wError);
                    wI++;
                }
            }

            aSB.append(' ');
        }

        boolean wHasResult = aComponentContext.hasResult();

        CXStringUtils.appendFormatStrInBuff(aSB, "hasResult=[%b]", wHasResult);

        if (wHasResult) {
            List<Map<String, Object>> wResults = aComponentContext.getResults();

            CXStringUtils.appendFormatStrInBuff(aSB, " nbResults=[%d]",
                    wResults.size());
            if (wResults.size() > 0) {
                for (Map<String, Object> wResult : wResults) {
                    CXStringUtils.appendFormatStrInBuff(aSB, " result=[%s]",
                            wResult.toString());
                }
            }

        }
        return aSB;
    }
}
