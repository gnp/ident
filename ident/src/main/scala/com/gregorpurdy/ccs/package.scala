/*
 * Copyright 2023-2025 Gregor Purdy
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

package com.gregorpurdy

/** This package is for implementations of Check Character Systems used by identifiers in the Ident module.
  *
  * Some of the identifiers--such as [[ident.Cusip]] and [[ident.Isin]]--use a single decimal Check Character (aka a
  * "Check Digit"), while others use multiple characters and/or character(s) that may include non-digits. Therefore, we
  * adopt the generic term "Check Character Systems" of the ISO/IEC 7064 standard (which is used for [[ident.Lei]] check
  * character calculation).
  */
package object ccs {}
