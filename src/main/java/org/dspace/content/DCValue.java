/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.content;

import org.dspace.content.authority.Choices;

import javax.persistence.*;

/**
 * Simple data structure-like class representing a Dublin Core value. It has an
 * element, qualifier, value and language.
 *
 * @author Robert Tansley
 * @author Martin Hald
 * @version $Revision$
 */
//TODO: ADD toHashSet & equals methods
@Deprecated
@Embeddable
@Table(name="dcvalue")
public class DCValue
{
    @Id
    @Column(name="dcvalue_id")
    @GeneratedValue(strategy = GenerationType.AUTO ,generator="dcvalue_seq")
    @SequenceGenerator(name="dcvalue_seq", sequenceName="dcvalue_seq")
    private Integer id;

    /** The DC element */
    @Column(name = "element")
    public String element;

    /** The DC qualifier, or <code>null</code> if unqualified */
    @Column(name = "qualifier")
    public String qualifier;

    /** The value of the field */
    @Column(name = "text_value")
    public String value;

    /** The language of the field, may be <code>null</code> */
    @Column(name = "text_lang")
    public String language;

    /** The schema name of the metadata element */
    @Column(name = "schema")
    public String schema;

    /** Authority control key */
    @Column(name = "authority")
    public String authority = null;

    @Column(name = "place")
    private int place;

    /** Authority control confidence  */
    @Column(name = "confidence")
    public int confidence = Choices.CF_UNSET;

    void setPlace(int place) {
        this.place = place;
    }
}