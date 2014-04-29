/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.checker;


import org.dspace.content.Bitstream;

import java.sql.SQLException;

/**
 * <p>
 * BitstreamDispatchers are strategy objects that hand bitstream ids out to
 * workers. Implementations must be threadsafe.
 * </p>
 * 
 * <p>
 * The rationale behind the use of the Sentinel pattern (rather than the more
 * traditional iterator pattern or a cursor c.f. java.sql.ResultSet): -
 * </p>
 * <ol>
 * <li>To make it easy to make implementations thread safe - multithreaded
 * invocation of the dispatchers is seen as a next step use case. In simple
 * terms, this requires that a single method does all the work.</li>
 * <li>Shouldn't an exception as the sentinel, as reaching the end of a loop is
 * not an exceptional condition.</li>
 * </ol>
 * 
 * 
 * @author Jim Downing
 * @author Grace Carpenter
 * @author Nathan Sarr
 * 
 */
public interface BitstreamDispatcher
{
    /**
     * Returns the next id for checking, or a sentinel value if there are no
     * more to check.
     * 
     * @return the next bitstream, or NULL if there
     *         isn't another value
     * 
     */
    public Bitstream next() throws SQLException;
}
