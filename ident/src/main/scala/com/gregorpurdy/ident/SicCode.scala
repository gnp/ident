/*
 * Copyright 2025 Gregor Purdy
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

package com.gregorpurdy.ident

/** An identifier from the standard documented in the 1987 version of the [Standard Industrial Classification
  * manual](https://catalog.hathitrust.org/Record/003843673):
  *
  * United States. National Technical Information Service., United States. Office of Management and Budget., United
  * States. Bureau of the Budget. Office of Statistical Standards., United States. Technical Committee on Industrial
  * Classification. (1987). _Standard industrial classification manual_. Springfield, Va.: For sale by National
  * Technical Information Service.
  *
  * The link to the viewer for this version is: https://babel.hathitrust.org/cgi/pt?id=mdp.39015015189189&view=1up&seq=7
  *
  * These identifiers are commonly known as "SIC Codes".
  *
  * This trait and the related classes are intended to represent valid formatted codes -- valid structure, not valid
  * values. The idea is that when you have a valid identifier, you could look it up in a reference file or database and
  * if the value is valid you would get corresponding information.
  *
  * Identifiers are represented as `String`s because as `Int`s they would be ambiguous. For example, integer `13` could
  * be a reference to Major Group `"13"` ("Oil and Gas Extraction") or Industry Group `"013"` ("Field Crops, Except Cash
  * Grains"). You would need a "level" _and_ an `Int` identifier to avoid ambiguity.
  *
  * These classes treat the string value from the standard as the `code` attribute. Each level of identifier also has an
  * integer attribute `id` unique only within that level.
  *
  * For that purpose, each subtype of `SIC` _does_ have an `apply(id: Int): SIC` method in its companion object. But,
  * because the identifer referenced could still be for another level the return type is the trait `SIC` not the
  * specific type corresponding to the companion object. For example, calling `SICIndustry(100)` would return you a
  * `SICMajorGroup` for code "01" since four-digit code "0100" ends in two zeroes and thus refers to a Major Group.
  *
  * If you have an `Int` that is supposed to be a (four digit) "SIC Industry Code", then you would use `SICIndustry(x)`
  * to turn the `Int` into a `SIC`. But, the SIC you get could actually be any of the subtypes of SIC trait (except
  * Division).
  *
  * There are variants of these codes both public and private. For example, Bureau of Labor Statitics provides a PDF
  * document of their codes, which omits the "J" and "K" Divisions, adds a non-standard "*" Division, and may have other
  * differences to the 1987 version.
  *
  * # Bureau of Labor Statistics
  *
  * There is a document "1987 Standard Industrial Classification (SIC) System", freely available as a PDF from the
  * Bureau of Labor Statistics.
  *
  * There are variants of SIC:
  *   - BLS version has a pseudo Division '*' consisting of non-standard Major Group '90' and three non-standard
  *     Industry Groups '901', '902' and '903'.
  *   - OSHA has a SIC Manual that does not have Division '*' but does have a Division 'J' not in the standard document.
  *   - SEC has a SIC list that includes Industry 9721 "International Affiars" that is also present at OSHA and Industry
  *     9995 "NON-OPERATING ESTABLISHMENTS" that is not. This list also associates each Industry listed to an "Office"
  *     in the SEC's Division of Corporation Finance charged with overseeing companies in that Industry.
  *   - Other parties may define their own non-standard "extended" codes.
  *
  * @see
  *   https://catalog.hathitrust.org/Record/003843673
  * @see
  *   https://babel.hathitrust.org/cgi/pt?id=mdp.39015015189189&view=1up&seq=7
  * @see
  *   https://guides.loc.gov/industry-research/classification-sic
  * @see
  *   https://www.bls.gov/oes/special.requests/oessic87.pdf
  * @see
  *   https://www.osha.gov/data/sic-manual
  * @see
  *   https://www.sec.gov/corpfin/division-of-corporation-finance-standard-industrial-classification-sic-code-list
  * @see
  *   https://www.bls.gov/ces/naics/home.htm
  */
sealed trait SicCode {
  def code: String
  override def toString: String = code
  def toIdentifierString: String = s"sic:$code"
}

/** A Standard Industry Classification (SIC) code for the topmost level: "Division". An identifier at this level is one
  * of:
  *
  *   - a single capital letter in the range "A" to "I" (inclusive)
  *   - the asterisk ("*")
  *
  * parsing is done loosely, ignoring leading and trailing whitespace and allowing lowercase letters.
  */
case class SicDivisionCode private (val id: Int) extends SicCode {
  override def code: String = id match {
    case 27                     => "*"
    case l if l >= 1 && l <= 26 => Character.toString('A' + (l - 1))
  }
}

object SicDivisionCode {
  val FORMAT = "([A-K*])".r

  def apply(value: String): SicDivisionCode = value.replaceAll("""[\v\h]+""", " ").trim.toUpperCase match {
    case FORMAT(d) =>
      d(0) match {
        case '*'                       => new SicDivisionCode(27)
        case l if l >= 'A' && l <= 'Z' => new SicDivisionCode(1 + (l - 'A'))
      }
    case _ => throw new IllegalArgumentException(s"Not a SIC Division code: '$value'")
  }
}

/** A Standard Industry Classification (SIC) code for the second level from top: "Major Group". An identifier at this
  * level is one of:
  *
  *   - a two-digit value (but not "00")
  *   - a two-digit value (but not "00") followed by a single zero (total: three digits)
  *   - a two-digit value (but not "00") followed by two zeroes (total: four digits)
  *
  * parsing is done loosely, ignoring leading and trailing whitespace.
  */
case class SicMajorGroupCode private (val id: Int) extends SicCode {
  override def code: String = "%02d".format(id)
}

object SicMajorGroupCode {
  val FORMAT = "([0-9]{2})(?:0(?:0)?)?".r

  def apply(value: String): SicMajorGroupCode = value.replaceAll("""[\v\h]+""", " ").trim.toUpperCase match {
    case FORMAT(mg) if mg != "00" => new SicMajorGroupCode(mg.toInt)
    case _                        => throw new IllegalArgumentException(s"Not a SIC Major Group code: '$value'")
  }

  def apply(id: Int): SicCode = {
    if (id < 1 || id > 99) {
      throw new IllegalArgumentException(s"SIC Major Group id $id is outside allowed range (0, 99]")
    } else {
      val s = id.toString match {
        case t if t.length == 1 => "0" + t
        case t if t.length == 2 => t
        case _                  => throw new IllegalStateException(s"Can't happen: $id is not 1-2 digits in length")
      }
      SicCode(s)
    }
  }
}

/** A Standard Industry Classification (SIC) code for the third level from top: "Industry Group". An identifier at this
  * level is one of:
  *
  *   - a three-digit value (but not starting with "00")
  *   - a three-digit value (but not starting with "00") followed by a single zero (total: four digits)
  *
  * parsing is done loosely, ignoring leading and trailing whitespace.
  */
case class SicIndustryGroupCode private (val id: Int) extends SicCode {
  override def code: String = "%03d".format(id)
  def majorGroup: SicMajorGroupCode = SicMajorGroupCode(code.substring(0, 2))
}

object SicIndustryGroupCode {
  val FORMAT = "([0-9]{3})(?:0)?".r

  def apply(value: String): SicIndustryGroupCode = value.replaceAll("""[\v\h]+""", " ").trim.toUpperCase match {
    case FORMAT(ig) if !ig.startsWith("00") => new SicIndustryGroupCode(ig.toInt)
    case _ => throw new IllegalArgumentException(s"Not a SIC Industry Group code: '$value'")
  }

  def apply(id: Int): SicCode = {
    if (id < 1 || id > 999) {
      throw new IllegalArgumentException(s"SIC Industry Group id $id is outside allowed range (0, 999]")
    } else {
      val s = id.toString match {
        case t if t.length == 1 => "00" + t // won't be a legal SIC because starts with "00"
        case t if t.length == 2 => "0" + t
        case t if t.length == 3 => t
        case _                  => throw new IllegalStateException(s"Can't happen: $id is not 1-3 digits in length")
      }
      SicCode(s)
    }
  }
}

/** A Standard Industry Classification (SIC) code for the fourth level from the top (also the bottom level): "Industry".
  * An identifier at this level is a four-digit integer:
  *
  *   - not starting with "00"
  *   - not ending with "0" or "00"
  */
case class SicIndustryCode private (val id: Int) extends SicCode {
  override def code: String = "%04d".format(id)
  def industryGroup: SicIndustryGroupCode = SicIndustryGroupCode(code.substring(0, 3))
}

object SicIndustryCode {
  val FORMAT = "([0-9]{4})".r

  def apply(value: String): SicIndustryCode = value.replaceAll("""[\v\h]+""", " ").trim.toUpperCase match {
    case FORMAT(i) if !i.startsWith("00") && !i.endsWith("0") && !i.endsWith("00") => new SicIndustryCode(i.toInt)
    case _ => throw new IllegalArgumentException(s"Not a SIC Industry code: '$value'")
  }

  def apply(id: Int): SicCode = {
    if (id < 1 || id > 9999) {
      throw new IllegalArgumentException(s"SIC Industry id $id is outside allowed range (0, 9999]")
    } else {
      val s = id.toString match {
        case t if t.length == 1 => "000" + t // won't be a legal SIC because starts with "00"
        case t if t.length == 2 => "00" + t // won't be a legal SIC because starts with "00"
        case t if t.length == 3 => "0" + t
        case t if t.length == 4 => t
        case _                  => throw new IllegalStateException(s"Can't happen: $id is not 1-4 digits in length")
      }
      SicCode(s)
    }
  }
}

object SicCode {

  def isValidFormatStrict(string: String): Boolean =
    SicDivisionCode.FORMAT.matches(string)
      || SicMajorGroupCode.FORMAT.matches(string)
      || SicIndustryGroupCode.FORMAT.matches(string)
      || SicIndustryCode.FORMAT.matches(string)

  def isValidFormatLoose(string: String): Boolean = isValidFormatStrict(string.trim.toUpperCase)

  def apply(value: String): SicCode = value.replaceAll("""[\v\h]+""", " ").trim.toUpperCase match {
    case SicDivisionCode.FORMAT(d)                               => SicDivisionCode(d)
    case SicMajorGroupCode.FORMAT(mg) if mg != "00"              => SicMajorGroupCode(mg)
    case SicIndustryGroupCode.FORMAT(ig) if !ig.startsWith("00") => SicIndustryGroupCode(ig)
    case SicIndustryCode.FORMAT(i) if !i.startsWith("00")        => SicIndustryCode(i)
    case _ => throw new IllegalArgumentException(s"Not a SIC code: '$value'")
  }

}
