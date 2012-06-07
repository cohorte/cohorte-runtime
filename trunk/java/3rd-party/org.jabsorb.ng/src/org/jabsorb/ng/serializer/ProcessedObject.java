/*
 * jabsorb - a Java to JavaScript Advanced Object Request Broker
 * http://www.jabsorb.org
 *
 * Copyright 2007-2008 The jabsorb team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.jabsorb.ng.serializer;

import java.util.LinkedList;
import java.util.List;

/**
 * Represents an object that has been already processed by the JSONSerializer
 * this is stored in the SerializerState in order to detect circular references
 * and duplicates.
 */
public class ProcessedObject {
    /**
     * The processed incoming object. When marshalling, this is the java object
     * that is being marshalled to json, when unmarshalling, this is the json
     * object being marshalled to java.
     */
    private Object object;

    /**
     * The parent object of this object. It will be null if this is the root
     * object being processed.
     */
    private ProcessedObject parent;

    /**
     * The "json" reference key such that [the json representation of]
     * parent[ref] = object. this will either be a String for an object
     * reference or an Integer for an array reference.
     */
    private Object ref;

    /**
     * The serialized equivalent of the object. Only used for unmarshalling to
     * recreate circular reference equivalences in java from fixed up incoming
     * json.
     * 
     * todo: should this just be called unmarshalled? it's only used in the
     * unmarshall case at this point todo: but it might be useful later in the
     * marshall case as well...
     */
    private Object serialized;

    /**
     * Get the reference "path" location for where this object was originally
     * located.
     * 
     * @return a List of Integer | String objects representing the path to the
     *         location of this object in the JSON hierarchy.
     */
    public List<Object> getLocation() {

        ProcessedObject link = this;

        // todo: could possibly make use of the existing linked list structure
        // todo: already here and instead return a List view of that structure
        // todo: is that easy to do??

        final List<Object> path = new LinkedList<Object>();

        while (link != null) {
            path.add(0, link.ref);
            link = link.getParent();
        }

        // todo: this original location could potentially be cached here for
        // performance.
        // todo: (but only do if it becomes necessary)
        // todo: It should always be generated on demand at least the first time
        // around, because
        // todo: in many cases it's not needed! (it's only needed to cover
        // circular reference and duplicate objects)

        return path;
    }

    /**
     * Get the actual Object that this ProcessedObject wraps.
     * 
     * @return the actual Object that this ProcessedObject wraps.
     */
    public Object getObject() {

        return object;
    }

    /**
     * Get the parent ProcessedObject of this ProcessedObject. It can be null if
     * this is the root of the JSON Object being processed.
     * 
     * @return the parent ProcessedObject of this ProcessedObject or null if
     *         this is the root of the Object hierarchy being processed.
     */
    public ProcessedObject getParent() {

        return parent;
    }

    /**
     * Get the reference name String | Integer of this ProcessedObject. This is
     * the reference such that parent[ref] refers to this object.
     * 
     * @return the reference Integer|String identifying this Object in its
     *         parent.
     */
    public Object getRef() {

        return ref;
    }

    /**
     * Get the serialized java Object that this ProcessedObject represents. Only
     * used when unmarshalling, to re-connect circular references/duplicates
     * that were fixed up.
     * 
     * @return java Object that the json object represented by this
     *         ProcessedObject is being serialized to.
     */
    public Object getSerialized() {

        return serialized;
    }

    /**
     * Set the source object that is being processed. This is the java object
     * that is being marshalled into json or the json object that is being
     * unmarshalled into Java.
     * 
     * @param object
     *            the actual Object that this ProcessedObject wraps.
     */
    public void setObject(final Object object) {

        this.object = object;
    }

    /**
     * Set the parent ProcessedObject of this ProcessedObject. It can be null if
     * this is the root of the JSON Object being processed.
     * 
     * @param parent
     *            the parent ProcessedObject of this ProcessedObject, or null if
     *            this is the root of the Object hierarchy being processed.
     */
    public void setParent(final ProcessedObject parent) {

        this.parent = parent;
    }

    /**
     * Set the reference name String | Integer of this ProcessedObject. This is
     * the reference such that parent[ref] refers to this object.
     * 
     * @param ref
     *            the reference Integer|String identifying this Object in its
     *            parent.
     */
    public void setRef(final Object ref) {

        this.ref = ref;
    }

    /**
     * Set the serialized java Object that this ProcessedObject represents. Only
     * used when unmarshalling, to re-connect circular references/duplicates
     * that were fixed up.
     * 
     * @param serialized
     *            java Object that the json object represented by this
     *            ProcessedObject is being serialized to.
     */
    public void setSerialized(final Object serialized) {

        this.serialized = serialized;
    }
}
