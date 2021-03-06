/*
// Licensed to Julian Hyde under one or more contributor license
// agreements. See the NOTICE file distributed with this work for
// additional information regarding copyright ownership.
//
// Julian Hyde licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except in
// compliance with the License. You may obtain a copy of the License at:
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
*/
package org.eigenbase.sql.validate;

import java.util.*;

import org.eigenbase.reltype.*;
import org.eigenbase.sql.*;
import org.eigenbase.util.*;


/**
 * A namespace describes the relation returned by a section of a SQL query.
 *
 * <p>For example, in the query <code>SELECT emp.deptno, age FROM emp,
 * dept</code>, the FROM clause forms a namespace consisting of two tables EMP
 * and DEPT, and a row type consisting of the combined columns of those tables.
 *
 * <p>Other examples of namespaces include a table in the from list (the
 * namespace contains the constituent columns) and a subquery (the namespace
 * contains the columns in the SELECT clause of the subquery).
 *
 * <p>These various kinds of namespace are implemented by classes {@link
 * IdentifierNamespace} for table names, {@link SelectNamespace} for SELECT
 * queries, {@link SetopNamespace} for UNION, EXCEPT and INTERSECT, and so
 * forth. But if you are looking at a SELECT query and call {@link
 * SqlValidator#getNamespace(org.eigenbase.sql.SqlNode)}, you may not get a
 * SelectNamespace. Why? Because the validator is allowed to wrap namespaces in
 * other objects which implement {@link SqlValidatorNamespace}. Your
 * SelectNamespace will be there somewhere, but might be one or two levels deep.
 * Don't try to cast the namespace or use <code>instanceof</code>; use {@link
 * SqlValidatorNamespace#unwrap(Class)} and {@link
 * SqlValidatorNamespace#isWrapperFor(Class)} instead.</p>
 *
 * @author jhyde
 * @version $Id$
 * @see SqlValidator
 * @see SqlValidatorScope
 * @since Mar 25, 2003
 */
public interface SqlValidatorNamespace
{
    //~ Methods ----------------------------------------------------------------

    /**
     * Returns the validator.
     *
     * @return validator
     */
    SqlValidator getValidator();

    /**
     * Returns the underlying table, or null if there is none.
     */
    SqlValidatorTable getTable();

    /**
     * Returns the row type of this namespace, which comprises a list of names
     * and types of the output columns. If the scope's type has not yet been
     * derived, derives it. Never returns null.
     *
     * @post return != null
     */
    RelDataType getRowType();

    /**
     * Allows RowType for the namespace to be explicitly set.
     */
    void setRowType(RelDataType rowType);

    /**
     * Returns the row type of this namespace, sans any system columns.
     *
     * @return Row type sans system columns
     */
    RelDataType getRowTypeSansSystemColumns();

    /**
     * Validates this namespace.
     *
     * <p>If the scope has already been validated, does nothing.</p>
     *
     * <p>Please call {@link SqlValidatorImpl#validateNamespace} rather than
     * calling this method directly.</p>
     */
    void validate();

    /**
     * Returns the parse tree node at the root of this namespace.
     *
     * @return parse tree node
     */
    SqlNode getNode();

    /**
     * Returns the parse tree node that at is at the root of this namespace and
     * includes all decorations. If there are no decorations, returns the same
     * as {@link #getNode()}.
     */
    SqlNode getEnclosingNode();

    /**
     * Looks up a child namespace of a given name.
     *
     * <p>For example, in the query <code>select e.name from emps as e</code>,
     * <code>e</code> is an {@link IdentifierNamespace} which has a child <code>
     * name</code> which is a {@link FieldNamespace}.
     *
     * @param name Name of namespace
     *
     * @return Namespace
     */
    SqlValidatorNamespace lookupChild(String name);

    /**
     * Returns whether this namespace has a field of a given name.
     *
     * @param name Field name
     *
     * @return Whether field exists
     */
    boolean fieldExists(String name);

    /**
     * Returns a list of expressions which are monotonic in this namespace. For
     * example, if the namespace represents a relation ordered by a column
     * called "TIMESTAMP", then the list would contain a {@link
     * org.eigenbase.sql.SqlIdentifier} called "TIMESTAMP".
     */
    List<Pair<SqlNode, SqlMonotonicity>> getMonotonicExprs();

    /**
     * Returns whether and how a given column is sorted.
     */
    SqlMonotonicity getMonotonicity(String columnName);

    /**
     * Makes all fields in this namespace nullable (typically because it is on
     * the outer side of an outer join.
     */
    void makeNullable();

    /**
     * Translates a field name to the name in the underlying namespace.
     */
    String translate(String name);

    /**
     * Returns this namespace, or a wrapped namespace, cast to a particular
     * class.
     *
     * @param clazz Desired type
     *
     * @return This namespace cast to desired type
     *
     * @throws ClassCastException if no such interface is available
     */
    <T> T unwrap(Class<T> clazz);

    /**
     * Returns whether this namespace implements a given interface, or wraps a
     * class which does.
     *
     * @param clazz Interface
     *
     * @return Whether namespace implements given interface
     */
    boolean isWrapperFor(Class<?> clazz);
}

// End SqlValidatorNamespace.java
