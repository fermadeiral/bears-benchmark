/*
 * Copyright 2013-2014 S. Webber
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
package org.projog.core.term;

import java.util.Map;

/**
 * The building blocks used to construct Prolog programs and queries.
 * <p>
 * <img src="doc-files/Term.png">
 */
public interface Term {
   /**
    * Returns a string representation of this term.
    * <p>
    * Exact value returned will vary by {@link TermType}.
    * 
    * @return a string representation of this term
    */
   String getName();

   /**
    * Returns an array of this terms's arguments.
    * <p>
    * <b>Note: for performance reasons the array returned is the same array used internally be the term instance so be
    * careful not to alter the array returned as changes will be reflected in the original term.</b>
    * 
    * @return array of this terms's arguments
    * @see #getArgument(int)
    */
   Term[] getArgs();

   /**
    * Returns the number of arguments in this term.
    * 
    * @return number of arguments in this term
    */
   int getNumberOfArguments();

   /**
    * Returns the term at the specified position in this term's arguments.
    * 
    * @param index index of the argument to return
    * @return the term at the specified position in this term's arguments
    * @throws RuntimeException if the index is out of range ({@code index < 0 || index >= getNumberOfArguments()})
    */
   Term getArgument(int index);

   /**
    * Returns the {@link TermType} represented by this term.
    * 
    * @return the {@link TermType} this term represents
    */
   TermType getType();

   /**
    * Returns a copy of this term.
    * <p>
    * The returned copy will share any immutable terms contained in this term. The returned copy will contain new
    * instances for any {@link Variable}s contained in this term. The {@code sharedVariables} parameter keeps track of
    * which {@link Variable}s have already been copied.
    * 
    * @param sharedVariables keeps track of which {@link Variable}s have already been copied (key = original version,
    * value = version used in copy)
    * @return a copy of this term
    */
   Term copy(Map<Variable, Variable> sharedVariables);

   /**
    * Returns the current instantiated state of this term.
    * <p>
    * Returns a representation of this term with all instantiated {@link Variable}s replaced with the terms they are
    * instantiated with.
    * 
    * @return a representation of this term with all instantiated {@link Variable}s replaced with the terms they are
    * instantiated with.
    */
   Term getTerm();

   /**
    * Attempts to unify this term to the specified term.
    * <p>
    * The rules for deciding if two terms are unifiable are as follows:
    * <ul>
    * <li>An uninstantiated {@link Variable} will unify with any term. As a result the {@link Variable} will become
    * instantiated to the other term. The instantiaton will be undone when {@link #backtrack()} is next called on the
    * {@link Variable}</li>
    * <li>Non-variable terms will unify with other terms that are of the same {@link TermType} and have the same value.
    * The exact meaning of "having the same value" will vary between term types but will include that the two terms
    * being unified have the same number of arguments and that all of their corresponding arguments unify.</li>
    * </ul>
    * <b>Note: can leave things in "half-state" on failure as neither List or Predicate backtrack earlier args.</b>
    * 
    * @param t the term to unify this term against
    * @return {@code true} if the attempt to unify this term to the given term was successful
    * @see #backtrack()
    */
   boolean unify(Term t);

   /**
    * Performs a strict comparison of this term to the specified term.
    * <p>
    * "Strict" equality means that an uninstantiated {@link Variable} will only be considered equal to itself or another
    * {@code Variable} that is already instantiated to it.
    * 
    * @param t the term to compare this term against
    * @return {@code true} if the given term represents a {@code Term} strictly equivalent to this term
    */
   boolean strictEquality(Term t);

   /**
    * Reverts this term back to its original state prior to any unifications.
    * <p>
    * Makes all {@link Variable}s that this term consists of uninstantiated.
    * 
    * @see #unify(Term)
    */
   void backtrack();

   /**
    * Returns {@code true} is this term is immutable.
    * <p>
    * A term is considered immutable if its value will never change as a result of executing its {@link #unify(Term)}
    * or {@link #backtrack()} methods. A term will not be considered immutable if it is a {@link Variable} or any of
    * its arguments are not immutable.
    * 
    * @return {@code true} is this term is immutable
    */
   boolean isImmutable();
}
